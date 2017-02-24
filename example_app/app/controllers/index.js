
var test = require("ti.mailcore");
var imap = {
		email: "ereynolds@codexlabs.com",
		password: "F00b4rm4n",
		host: "imap.gmail.com",
		port: 993
};
var smpt = {
		email: "ereynolds@codexlabs.com",
		password: "F00b4rm4n",
		host: "smtp.gmail.com",
		port: 465
};

function doClick() {
	test.getFolders(imap, function(error, folders) {
		if(error) {
			Ti.API.info(error);
		} else {
			Ti.API.info("Folders:");
			Ti.API.info(folders);
			
			// note: pass nil for range to get all
			test.getMail(imap, "INBOX", [105, 108], function(error, mail) {
				if(error) {
					Ti.API.info("ERROR: " + error);
				} else {
					Ti.API.info("Some of your basic email data:");
					mail.forEach(function(item) {
						Ti.API.info(JSON.stringify(item, null, '\t'));
					});
					
					test.getMailInfo(imap, "INBOX", 107, function(error, email) {
						if(error) {
							Ti.API.info("ERROR: " + error);
							Ti.API.info(error);
						} else {
							Ti.API.info("Specific message:");
							Ti.API.info(JSON.stringify(email, null, '\t'));
		
							var new_email = {
								subject: "test",
								body: "<div>testbody</div>",
								addresses: {
									from: {
										name: "Joseph in disguise",
										mailbox: "ereynolds@codexlabs.com"
									},
									to: [{
										name: "Andrew",
										mailbox: "sophrinix@gmail.com"
									}],
									cc: [{
										name: "Me",
										mailbox: "joseph@blackgategames.com"
									}]
								},
								headers: {
									"x-test": "blah"
								}
							};
							test.send(smpt, new_email, function(error, what_sent) {
								if(error) {
									Ti.API.info("ERROR: " + error);
								} else {
									Ti.API.info("WHAT SENT:");
									Ti.API.info(JSON.stringify(what_sent, null, '\t'));
								}
							});
						}
					});
				}
			});
		}
	});
}

$.index.open();
