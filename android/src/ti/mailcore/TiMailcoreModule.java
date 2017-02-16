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

import java.util.HashMap;

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
	public void createSession(KrollDict arguments) {
		String email = (String)arguments.get("email");
		String password = (String)arguments.get("password");
		String host = (String)arguments.get("host");
		int port = arguments.containsKey("port") ? arguments.getInt("port") : 993;
		int ctype = arguments.containsKey("ctype") ? arguments.getInt("ctype") : ConnectionType.ConnectionTypeTLS;

		if(arguments.containsKey("onerror")) {
			KrollFunction fail = (KrollFunction)arguments.get("onerror");
			Object args[] = new Object[1];
			args[0] = new String("Could not authenticate.");
			fail.call((KrollObject)fail, args);
		}
	}
}
