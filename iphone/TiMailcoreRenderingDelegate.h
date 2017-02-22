/**
 * TiMailcore delegate for rendering HTML
 */

#import "MailCore/MailCore.h"

@interface TiMailcoreRenderingDelegate : NSObject <MCOHTMLRendererDelegate> {
}

- (BOOL) MCOAbstractMessage:(MCOAbstractMessage *)msg shouldShowPart:(MCOAbstractPart *)part;
- (BOOL) MCOAbstractMessage:(MCOAbstractMessage *)msg canPreviewPart:(MCOAbstractPart *)part;

@end