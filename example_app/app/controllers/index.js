

function doClick(e) {
	var test = require("ti.mailcore");
	var session = test.open("ereynolds@codexlabs.com", "F00b4rm4n", "imap.gmail.com");

	session.getMail(function(r){
		session.getMailInfo(function(r2){
			Ti.API.info(r2);
		}, r.uid);
	});
/*
 * 
 
 */
}

$.index.open();
