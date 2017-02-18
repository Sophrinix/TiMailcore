/**
 * TiMailcore session proxy
 */
package ti.mailcore;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiCompositeLayout.LayoutArrangement;
import org.appcelerator.titanium.view.TiUIView;

import com.libmailcore.IMAPFolder;
import com.libmailcore.IMAPFetchFoldersOperation;
import com.libmailcore.IMAPSession;
import com.libmailcore.IMAPOperation;
import com.libmailcore.ConnectionType;
import com.libmailcore.OperationCallback;
import com.libmailcore.MailException;
import com.libmailcore.IMAPFetchMessagesOperation;
import com.libmailcore.IMAPMessage;
import com.libmailcore.MailException;
import com.libmailcore.OperationCallback;
import com.libmailcore.IndexSet;
import com.libmailcore.IMAPMessagesRequestKind;
import com.libmailcore.Range;
import com.libmailcore.IMAPSession;
import com.libmailcore.MailException;
import com.libmailcore.OperationCallback;
import com.libmailcore.IMAPMessage;
import com.libmailcore.IMAPMessageRenderingOperation;

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

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
			private KrollFunction callback;

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
			return new Object[]{result}; // temp
		}
	}

	@Kroll.method
	public void getMail(KrollFunction cb, String folder, int range[]) {
		GetMailCaller caller = new GetMailCaller(cb);
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
		public GetMailCaller(KrollFunction cb) {
			super(cb);
		}

		protected IMAPOperation createOperation() {
			return session.fetchAllFoldersOperation(); // temp
		}

		protected Object[] formatResult(IMAPOperation operation) {
			return new Object[]{}; // temp
		}
	}
	/*

- (void)getMail:(id)args {
        MCOIndexSet *uids;
        if(nargs >= 3 && [args objectAtIndex:2]) {
            NSArray * range = [args objectAtIndex:2];
            uids = [MCOIndexSet indexSetWithRange:MCORangeMake([TiUtils intValue:range[0]], [TiUtils intValue:range[1]] - [TiUtils intValue:range[0]])];
        } else {
            uids = [MCOIndexSet indexSetWithRange:MCORangeMake(1, UINT64_MAX)];
        }

        MCOIMAPMessagesRequestKind requestKind = (MCOIMAPMessagesRequestKind)
        (MCOIMAPMessagesRequestKindHeaders | MCOIMAPMessagesRequestKindHeaderSubject);

        MCOIMAPFetchMessagesOperation *fetchOperation = [session fetchMessagesOperationWithFolder:folder requestKind:requestKind uids:uids];

        [fetchOperation start:^(NSError * error, NSArray * fetchedMessages, MCOIndexSet * vanishedMessages) {
            if(error) {
                [[args objectAtIndex:0] call:@[error, @[]] thisObject:nil];
            } else {
                NSMutableArray * result = [[NSMutableArray alloc] init];
                for(MCOIMAPMessage * message in fetchedMessages) {
                    [result addObject: @{
                                          @"uid": [NSNumber numberWithInt:message.uid],
                                          @"sender": message.header.sender.displayName,
                                          @"subject": message.header.subject
                                          }];
                }
                [[args objectAtIndex:0] call:@[[NSNull null], result] thisObject:nil];
            }
        }];
}

	*/


		@Kroll.method
		public void getMailInfo(int uid, KrollFunction cb, String folder) {

		}
		@Kroll.method
		public void getMailInfo(int uid, KrollFunction cb) {
			getMailInfo(uid, cb, "INBOX");
		}

		private class GetMailInfoCaller extends CallbackCaller {
			public GetMailInfoCaller(KrollFunction cb) {
				super(cb);
			}

			protected IMAPOperation createOperation() {
				return session.fetchAllFoldersOperation(); // temp
			}

			protected Object[] formatResult(IMAPOperation operation) {
				return new Object[]{}; // temp
			}
		}

	/*

	- (void)getMailInfo:(id)args {
	        MCOIMAPMessagesRequestKind requestKind = (MCOIMAPMessagesRequestKind)
	        (MCOIMAPMessagesRequestKindHeaders | MCOIMAPMessagesRequestKindHeaderSubject | MCOIMAPMessagesRequestKindStructure | MCOIMAPMessagesRequestKindExtraHeaders | MCOIMAPMessagesRequestKindFullHeaders);

	        MCOIndexSet *uids = [MCOIndexSet indexSetWithRange:MCORangeMake([TiUtils intValue:[args objectAtIndex:0]], 1)];

	        MCOIMAPFetchMessagesOperation *fetchOperation = [session fetchMessagesOperationWithFolder:folder requestKind:requestKind uids:uids];

	        [fetchOperation start:^(NSError * error, NSArray * fetchedMessages, MCOIndexSet * vanishedMessages) {
	            if(error) {
	                [[args objectAtIndex:1] call:@[error, @{}] thisObject:nil];
	            } else {
	                if(fetchedMessages.count >= 1) {
	                    MCOIMAPMessage * message = [fetchedMessages firstObject];
	                    NSMutableDictionary * email = [self compose: nil];
	                    [email setObject:message.header.subject forKey:@"subject"];

	                    [[args objectAtIndex:1] call:@[[NSNull null], email] thisObject:nil];
	                } else {
	                    [[args objectAtIndex:1] call:@[@"No message found.", @{}] thisObject:nil];
	                }
	            }
	        }];
	}
	*/

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
