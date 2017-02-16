
var test = require("ti.mailcore");

function doClick(e) {
	var s = test.createSession({
		email: "ereynolds@codexlabs.com",
		password: "F00b4rm4n",
		host: "imap.gmail.com",
		onsuccess: doSession,
		onerror: function(error) {
			Ti.API.info(error);
		}
	});
}

function doSession(session) {
	session.getFolders(function(error, folders) {
		if(error) {
			Ti.API.info(error);
		} else {
			Ti.API.info(folders);
			session.getMail(function(error, mail) {
				if(error) {
					Ti.API.info(error);
				} else {
					Ti.API.info(mail);
				}
			}, "INBOX", [105, 108]);
		}
	});
}

$.index.open();
