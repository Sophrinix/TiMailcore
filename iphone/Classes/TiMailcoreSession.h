/**
 * TiMailcore Session Proxy
 */

#import "TiProxy.h"
#import "MailCore/MailCore.h"


@interface TiMailcoreSession : TiProxy {
    MCOIMAPSession * session;
}

- (id)init: (NSString*)email
    password:(NSString*)pass
    atHost:(NSString*)host
    atPort:(int)port
    withCtype:(MCOConnectionType)ctype
    cb:(void (^)(bool))cb;
/*


- (Boolean)checkAccount;

- (id)getFolders: (id)args;
- (id)getMailCount:(id)args;
- (id)getMailInfo: (id)args;
- (id)getMail: (id)args;

- (id)compose: (id)args;
- (id)send: (id)args;
*/
@end
