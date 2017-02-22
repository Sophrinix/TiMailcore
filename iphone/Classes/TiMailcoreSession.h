/**
 * TiMailcore Session Proxy
 */

#import "TiProxy.h"
#import "MailCore/MailCore.h"
#import "TiMailcoreRenderingDelegate.h"


@interface TiMailcoreSession : TiProxy {
    MCOIMAPSession * session;
    TiMailcoreRenderingDelegate * rendering_delegate;
}

- (id)init: (NSString*)email
    password:(NSString*)pass
    atHost:(NSString*)host
    atPort:(int)port
    withCtype:(MCOConnectionType)ctype;

- (bool)checkAccount: (void (^)(bool))cb;
- (void)getFolders:(id)args;
- (void)getMail:(id)args;
- (id)compose: (id)args;

@end
