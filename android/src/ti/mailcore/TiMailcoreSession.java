/**
 * TiMailcore session proxy
 */
package ti.mailcore;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.kroll.common.Log;

import com.libmailcore.IMAPOperation;
import com.libmailcore.IMAPSession;
import com.libmailcore.IMAPMessage;
import com.libmailcore.IMAPFetchContentOperation;
import com.libmailcore.IMAPFetchMessagesOperation;
import com.libmailcore.IMAPFetchFoldersOperation;
import com.libmailcore.IMAPMessagesRequestKind;
import com.libmailcore.IMAPFolder;
import com.libmailcore.IndexSet;
import com.libmailcore.MailException;
import com.libmailcore.OperationCallback;
import com.libmailcore.Range;
import com.libmailcore.Address;
import com.libmailcore.MessageHeader;
import com.libmailcore.MessageParser;

import java.util.ArrayList;

@Kroll.proxy()
public class TiMailcoreSession extends KrollProxy
{
	// Standard Debugging variables
	private static final String LCAT = "SessionProxy";
	private static final boolean DBG = TiConfig.LOGD;

	private IMAPSession session;
	private TiMailcoreModule parent;

	public TiMailcoreSession(TiMailcoreModule p, String email, String password, String host, int port, int ctype) {
			parent = p;
			session = new IMAPSession();
			session.setHostname(host);
			session.setPort(port);
			session.setUsername(email);
			session.setPassword(password);
			session.setConnectionType(ctype);
	}

	// probably cant call this from titanium.
	public void checkAccount() {
		session.checkAccountOperation().start(parent);
	}

	// Abstract base class of all callback job handlers. Just have to handle
	// creation of the operation and formatting of its results.
	private abstract class CallbackCaller implements OperationCallback {
			private IMAPOperation operation;
			protected KrollFunction callback;

			public CallbackCaller(KrollFunction cb) {
				callback = cb;
			}

			abstract protected IMAPOperation createOperation();
			abstract protected Object formatResult(IMAPOperation operation);

			public void start() {
				operation = createOperation();
				operation.start(this);
			}

			public void succeeded() {
				callback.call(getKrollObject(), new Object[]{null, formatResult(operation)});
			}

			public void failed(MailException exception) {
				callback.call(getKrollObject(), new Object[]{exception.getMessage(), null});
			}
	}


	// Get folder listing
	@Kroll.method
	public void getFolders(KrollFunction cb) {
		CallbackCaller caller = new GetFoldersCaller(cb);
		caller.start();
	}
	private class GetFoldersCaller extends CallbackCaller {
		public GetFoldersCaller(KrollFunction cb) {
			super(cb);
		}

		protected IMAPOperation createOperation() {
			return session.fetchAllFoldersOperation();
		}

		protected Object formatResult(IMAPOperation operation) {
			java.util.List<IMAPFolder> folders = ((IMAPFetchFoldersOperation)operation).folders();
			ArrayList result = new ArrayList();
			for(IMAPFolder folder : folders) {
				result.add(folder.path());
			}
			return result.toArray(new String[result.size()]);
		}
	}


	// Get basic info of a mail folder with optional uid range
	@Kroll.method
	public void getMail(KrollFunction cb, String folder, long range[]) {
		IndexSet uids = IndexSet.indexSetWithRange(new Range(range[0], range[1] - range[0]));
		CallbackCaller caller = new GetMailCaller(cb, folder, uids);
		caller.start();
	}
	@Kroll.method
	public void getMail(KrollFunction cb, String folder) {
		getMail(cb, folder, new long[]{1, Long.MAX_VALUE});
	}
	@Kroll.method
	public void getMail(KrollFunction cb) {
		getMail(cb, "INBOX");
	}
	private class GetMailCaller extends CallbackCaller {
		private String folder;
		private IndexSet uids;

		public GetMailCaller(KrollFunction cb, String f, IndexSet u) {
			super(cb);
			folder = f;
			uids = u;
		}

		protected IMAPOperation createOperation() {
			int requestKind = IMAPMessagesRequestKind.IMAPMessagesRequestKindHeaders |
				IMAPMessagesRequestKind.IMAPMessagesRequestKindHeaderSubject;

			return session.fetchMessagesByUIDOperation(folder, requestKind, uids);
		}

		protected Object formatResult(IMAPOperation operation) {
			java.util.List<IMAPMessage> messages = ((IMAPFetchMessagesOperation)operation).messages();
			ArrayList result = new ArrayList();
			for(IMAPMessage message : messages) {
				KrollDict data = new KrollDict();
				data.put("uid", message.uid());
				data.put("sender", message.header().sender().displayName());
				data.put("subject", message.header().subject());

				result.add(data);
			}

			return result.toArray(new KrollDict[result.size()]);
		}
	}


	// Get detailed info about one specific email by its uid
	@Kroll.method
	public void getMailInfo(long uid, KrollFunction cb, String folder) {
		CallbackCaller caller = new GetMailInfoCaller(cb, uid, folder);
		caller.start();
	}
	@Kroll.method
	public void getMailInfo(long uid, KrollFunction cb) {
		getMailInfo(uid, cb, "INBOX");
	}

	private class GetMailInfoCaller extends CallbackCaller {
		private String folder;
		private long uid;

		public GetMailInfoCaller(KrollFunction cb, long u, String f) {
			super(cb);
			uid = u;
            folder = f;
		}

		protected IMAPOperation createOperation() {
			return session.fetchMessageByUIDOperation(folder, uid);
		}

        protected Object formatResult(IMAPOperation operation) {
            byte[] data = ((IMAPFetchContentOperation)operation).data();
            KrollDict email = compose();
            KrollDict email_headers = email.getKrollDict("headers");
            KrollDict email_addresses = email.getKrollDict("addresses");
            
            MessageHeader header = new MessageHeader(data);
            MessageParser parser = MessageParser.messageParserWithData(data);
            
            if(header != null) {
                // Basic data and headers
                if(header.subject() != null) {
                    email.put("subject", header.subject());
                }
                if(header.date() != null) {
                    email.put("date", header.date().toString());
                }
                if(header.receivedDate() != null) {
                    email.put("receivedDate", header.receivedDate().toString());
                }
                if(header.allExtraHeadersNames() != null) {
                    for(String extra : header.allExtraHeadersNames()) {
                        String extra_data = header.extraHeaderValueForName(extra);
                        if(extra_data != null) {
                            email_headers.put(extra, extra_data);
                        }
                    }
                }
                // 'From' address
                if(header.from() != null) {
                    KrollDict from = email_addresses.getKrollDict("from");
                    String display_name = header.from().displayName();
                    String mailbox = header.from().mailbox();
                    if(display_name != null) {
                        from.put("name", display_name);
                    }
                    if(mailbox != null) {
                        from.put("mailbox", mailbox);
                    }
                }
                
                // Remainder of the address types
                KrollDict address_types = new KrollDict();
                address_types.put("to", header.to());
                address_types.put("cc", header.cc());
                address_types.put("bcc", header.bcc());
                address_types.put("replyTo", header.replyTo());
                
                for(Object address_section_obj : address_types.keySet()) {
                    String address_section = (String)address_section_obj;
                    
                    java.util.List<Address> addresses = (java.util.List<Address>)address_types.get(address_section);
                    KrollDict new_addresses = new KrollDict();
                    
                    for(Address address : addresses) {
                        String display_name = address.displayName();
                        String mailbox = address.mailbox();
                        if(display_name != null) {
                            new_addresses.put("name", display_name);
                        }
                        if(mailbox != null) {
                            new_addresses.put("mailbox", mailbox);
                        }
                    }
                    email_addresses.put(address_section, new_addresses);
                }
            }
            if(parser != null) {
                email.put("body", parser.htmlBodyRendering());
            }
            return email;
		}
	}


	// Get a basic skeleton of an email
	@Kroll.method
	public KrollDict compose() {
		KrollDict email_data = _getEmailStructure();
        KrollDict email_header = _getHeaderStructure();
        KrollDict email_address = _getAddressStructure();
		// prepare header

        _applyHeader(email_header, email_data);
        _applyAddress(email_address, email_data);

		return email_data;
	}


	// Private methods defining email json structure
	private KrollDict _getEmailStructure() {
		KrollDict structure = new KrollDict();
		structure.put("subject", "");
		structure.put("body", "");
		return structure;
	}
	private KrollDict _getHeaderStructure() {
		KrollDict structure = new KrollDict();
		return structure;
	}
	private KrollDict _getAddressStructure() {
		KrollDict structure = new KrollDict();
		structure.put("to", new Object[0]);
		structure.put("cc", new Object[0]);
        structure.put("bcc", new Object[0]);
        structure.put("from", new Object[0]);
        structure.put("replyTo", new Object[0]);
		return structure;
	}

	private void _applyHeader(KrollDict header, KrollDict email) {
		email.put("headers", header);
	}

	private void _applyAddress(KrollDict address, KrollDict email) {
		email.put("addresses", address);
	}
}
