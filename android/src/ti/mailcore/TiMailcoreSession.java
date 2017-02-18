/**
 * TiMailcore session proxy
 */
package ti.mailcore;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.common.TiConfig;

import com.libmailcore.IMAPOperation;
import com.libmailcore.IMAPSession;
import com.libmailcore.IMAPMessage;
import com.libmailcore.IMAPFetchMessagesOperation;
import com.libmailcore.IMAPFetchFoldersOperation;
import com.libmailcore.IMAPMessagesRequestKind;
import com.libmailcore.IMAPFolder;
import com.libmailcore.IndexSet;
import com.libmailcore.MailException;
import com.libmailcore.OperationCallback;
import com.libmailcore.Range;

import java.util.HashMap;
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
		CheckAccountCaller caller = new CheckAccountCaller(parent);
		caller.start();
	}
	private class CheckAccountCaller implements OperationCallback {
		private KrollFunction callback;

		public CheckAccountCaller(TiMailcoreModule p) {
			parent = p;
		}

		public void start() {
			IMAPOperation op = session.checkAccountOperation();
			op.start(this);
		}

    public void succeeded() {
			parent.succeeded();
    }

    public void failed(MailException exception) {
			parent.failed(exception);
    }
	}

	// Abstract base class of all callback job handlers. Just have to handle
	// creation of the operation and formatting of its results.
	private abstract class CallbackCaller implements OperationCallback {
			private IMAPOperation operation;
			protected KrollFunction callback;

			public CallbackCaller(KrollFunction cb) {
				callback = cb;
				operation = createOperation();
			}

			abstract protected IMAPOperation createOperation();
			abstract protected Object[] formatResult(IMAPOperation operation);

			public void start() {
				operation.start(this);
			}

			public void succeeded() {
				callback.call(getKrollObject(), formatResult(operation));
			}

			public void failed(MailException exception) {
				callback.call(getKrollObject(), new Object[]{exception.getMessage()});
			}
	}


	@Kroll.method
	public void getFolders(KrollFunction cb) {
		GetFoldersCaller caller = new GetFoldersCaller(cb);
		caller.start();
	}
	private class GetFoldersCaller extends CallbackCaller {
		public GetFoldersCaller(KrollFunction cb) {
			super(cb);
		}

		protected IMAPOperation createOperation() {
			return session.fetchAllFoldersOperation();
		}

		protected Object[] formatResult(IMAPOperation operation) {
			java.util.List<IMAPFolder> folders = ((IMAPFetchFoldersOperation)operation).folders();
			ArrayList result = new ArrayList();
			for(IMAPFolder folder : folders) {
				result.add(folder.path());
			}
			return new Object[]{result};
		}
	}


	@Kroll.method
	public void getMail(KrollFunction cb, String folder, int range[]) {
		IndexSet uids = IndexSet.indexSetWithRange(new Range(range[0], range[1] - range[0]));
		GetMailCaller caller = new GetMailCaller(cb, folder, uids);

		caller.start();
	}
	@Kroll.method
	public void getMail(KrollFunction cb, String folder) {
		getMail(cb, folder, new int[]{1, Integer.MAX_VALUE});
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
			int requestKind = IMAPMessagesRequestKind.IMAPMessagesRequestKindHeaders | IMAPMessagesRequestKind.IMAPMessagesRequestKindHeaderSubject;

			return session.fetchMessagesByNumberOperation(folder, requestKind, uids);
		}

		protected Object[] formatResult(IMAPOperation operation) {
			java.util.List<IMAPMessage> messages = ((IMAPFetchMessagesOperation)operation).messages();
			ArrayList result = new ArrayList();
			for(IMAPMessage message : messages) {
				HashMap data = new HashMap();
				data.put("uid", message.uid());
				data.put("sender", message.header().sender().displayName());
				data.put("subject", message.header().subject());

				result.add(data);
			}
			return new Object[]{result};
		}
	}


	@Kroll.method
	public void getMailInfo(int uid, KrollFunction cb, String folder) {

	}
	@Kroll.method
	public void getMailInfo(int uid, KrollFunction cb) {
		getMailInfo(uid, cb, "INBOX");
	}

	private class GetMailInfoCaller extends CallbackCaller {
		private String folder;
		private int uid;

		public GetMailInfoCaller(KrollFunction cb, int u, String f) {
			super(cb);
			uid = u;
			folder = f;
		}

		protected IMAPOperation createOperation() {
			int requestKind = IMAPMessagesRequestKind.IMAPMessagesRequestKindHeaders;
			requestKind |= IMAPMessagesRequestKind.IMAPMessagesRequestKindHeaderSubject;
			requestKind |= IMAPMessagesRequestKind.IMAPMessagesRequestKindStructure;
			requestKind |= IMAPMessagesRequestKind.IMAPMessagesRequestKindExtraHeaders;
			requestKind |= IMAPMessagesRequestKind.IMAPMessagesRequestKindFullHeaders;

			IndexSet uids = IndexSet.indexSetWithRange(new Range(uid, 1));

			return session.fetchMessagesByNumberOperation(folder, requestKind, uids);
		}

		protected Object[] formatResult(IMAPOperation operation) {
			java.util.List<IMAPMessage> messages = ((IMAPFetchMessagesOperation)operation).messages();

			if(messages.size() >= 1) {
				IMAPMessage message = messages.get(0);
				HashMap email = compose();
				email.put("subject", message.header().subject());
				return new Object[]{email};
			} else {
				return new Object[]{};
			}
		}
	}

	@Kroll.method
	public HashMap compose() {
		HashMap email_data = _getEmailStructure();
		HashMap email_header = _getHeaderStructure();
		// prepare header

		_applyHeader(email_header, email_data);

		return email_data;
	}

	private HashMap _getEmailStructure() {
		HashMap structure = new HashMap();
		structure.put("subject", "");
		structure.put("body", "");
		return structure;
	}
	private HashMap _getHeaderStructure() {
		HashMap structure = new HashMap();
		return structure;
	}
	private HashMap _getAddressStructure() {
		HashMap structure = new HashMap();
		structure.put("to", new ArrayList());
		structure.put("cc", new ArrayList());
		structure.put("bcc", new ArrayList());
		return structure;
	}

	private void _applyHeader(HashMap header, HashMap email) {
		email.put("header", header);
	}

	private void _applyAddress(HashMap address, HashMap email) {
		email.put("address", address);
	}
}
