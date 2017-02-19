/**
 * TiMailcore
 */

package ti.mailcore;

import org.appcelerator.titanium.TiApplication;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.common.TiConfig;

import com.libmailcore.MailException;
import com.libmailcore.OperationCallback;
import com.libmailcore.ConnectionType;

@Kroll.module(name="TiMailcore", id="ti.mailcore")
public class TiMailcoreModule extends KrollModule implements OperationCallback {
	private static final String LCAT = "TiMailcoreModule";
	private static final boolean DBG = TiConfig.LOGD;

	private KrollFunction win;
	private KrollFunction fail;
	private TiMailcoreSession session;

	public TiMailcoreModule() {
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {
	}

	// Methods
	@Kroll.method
	public void createSession(KrollDict arguments) {
		String email = (String)arguments.get("email");
		String password = (String)arguments.get("password");
		String host = (String)arguments.get("host");
		int port = arguments.containsKey("port") ? arguments.getInt("port") : 993;
		int ctype = arguments.containsKey("ctype") ? arguments.getInt("ctype") : ConnectionType.ConnectionTypeTLS;

		win = null;
		fail = null;
		if(arguments.containsKey("onsuccess")) {
			win = (KrollFunction)arguments.get("onsuccess");
		}
		if(arguments.containsKey("onerror")) {
			fail = (KrollFunction)arguments.get("onerror");
		}

		session = new TiMailcoreSession(this, email, password, host, port, ctype);
		session.checkAccount();
	}

  public void succeeded() {
		if(win != null) {
			win.call(getKrollObject(), new Object[]{session});
		}
  }

  public void failed(MailException exception) {
		if(fail != null) {
			fail.call(getKrollObject(), new Object[]{"Could not authenticate"});
		}
  }
}
