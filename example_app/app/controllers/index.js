
var test = require("ti.mailcore");
var s = test.createSession({
	email: "ereynolds@codexlabs.com",
	password: "F00b4rm4n",
	host: "imap.gmail.com"
}, function() {
	Ti.API.info("yes");
}); 

function doClick(e) {
}

$.index.open();
