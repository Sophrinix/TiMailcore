/**
 * TiMailcore session proxy
 */
package ti.mailcore;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiCompositeLayout.LayoutArrangement;
import org.appcelerator.titanium.view.TiUIView;

import com.libmailcore.IMAPSession;
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
//			session.setHostname(host);
	//		session.setPort(port);
		//	session.setUsername(email);
		//	session.setPassword(password);
		//	session.setConnectionType(ctype);
	}
/*

	// Constructor
	public ExampleProxy()
	{
		super();
	}


	// Handle creation options
	@Override
	public void handleCreationDict(KrollDict options)
	{
		super.handleCreationDict(options);

		if (options.containsKey("message")) {
			Log.d(LCAT, "example created with message: " + options.get("message"));
		}
	}

	// Methods
	@Kroll.method
	public void printMessage(String message)
	{
		Log.d(LCAT, "printing message: " + message);
	}


	@Kroll.getProperty @Kroll.method
	public String getMessage()
	{
        return "Hello World from my module";
	}

	@Kroll.setProperty @Kroll.method
	public void setMessage(String message)
	{
	    Log.d(LCAT, "Tried setting module message to: " + message);
	}
	*/
}
