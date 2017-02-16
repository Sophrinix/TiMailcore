/**
 * TiMailcore
 */

package ti.mailcore;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.kroll.KrollFunction;

import com.libmailcore.ConnectionType;

@Kroll.module(name="TiMailcore", id="ti.mailcore")
public class TiMailcoreModule extends KrollModule
{
	// Standard Debugging variables
	private static final String LCAT = "TiMailcoreModule";
	private static final boolean DBG = TiConfig.LOGD;

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
	public void createSession(String email, String password, String host,
		int port, int ctype, KrollFunction onsuccess, KrollFunction onfail) {

	}
	@Kroll.method
	public void createSession(String email, String password, String host,
		int port, int ctype, KrollFunction onsuccess) {
			createSession(email, password, host, port, ctype, onsuccess, null);
	}
	@Kroll.method
	public void createSession(String email, String password, String host,
		int port, int ctype) {
			createSession(email, password, host, port, ctype, null, null);
	}
	@Kroll.method
	public void createSession(String email, String password, String host, int port) {
			createSession(email, password, host, port, ConnectionType.ConnectionTypeTLS, null, null);
	}
	@Kroll.method
	public void createSession(String email, String password, String host) {
			createSession(email, password, host, 993, ConnectionType.ConnectionTypeTLS, null, null);
	}
}
