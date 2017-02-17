/**
 * TiMailcore
 */

package ti.mailcore;

import org.appcelerator.kroll.KrollObject;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.kroll.KrollFunction;

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

import java.util.HashMap;

@Kroll.module(name="TiMailcore", id="ti.mailcore")
public class TiMailcoreModule extends KrollModule implements OperationCallback
{
	// Standard Debugging variables
	private static final String LCAT = "TiMailcoreModule";
	private static final boolean DBG = TiConfig.LOGD;

	private KrollFunction win;
	private KrollFunction fail;
	private TiMailcoreSession session;

	// You can define constants with @Kroll.constant, for example:
	// @Kroll.constant public static final String EXTERNAL_NAME = value;

	public TiMailcoreModule()
	{
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		// put module init code that needs to run when the application is created
	}

	// Methods
	@Kroll.method
	public void createSession(KrollDict arguments) {
		String email = (String)arguments.get("email");
		String password = (String)arguments.get("password");
		String host = (String)arguments.get("host");
		int port = arguments.containsKey("port") ? arguments.getInt("port") : 993;
		int ctype = arguments.containsKey("ctype") ? arguments.getInt("ctype") : ConnectionType.ConnectionTypeTLS;

		session = new TiMailcoreSession(this, email, password, host, port, ctype);

		if(arguments.containsKey("onsuccess")) {
			win = (KrollFunction)arguments.get("onsuccess");
		}

		if(arguments.containsKey("onerror")) {
			fail = (KrollFunction)arguments.get("onerror");
		}
	}

  public void succeeded() {
		Object args[] = new Object[1];
		args[0] = session;
		win.call(getKrollObject(), args);
  }

  public void failed(MailException exception) {
		Object args[] = new Object[1];
		args[0] = new String("Could not authenticate.");;
		fail.call(getKrollObject(), args);
  }
}
