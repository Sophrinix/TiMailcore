/**
 * TiMailcore delegate for rendering HTML
 */

#import <Foundation/Foundation.h>
#import "TiMailcoreRenderingDelegate.h"

@implementation TiMailcoreRenderingDelegate

- (BOOL) MCOAbstractMessage:(MCOAbstractMessage *)msg shouldShowPart:(MCOAbstractPart *)part {
    NSLog(@"[INFO]yes");
    return YES;
}

- (BOOL) MCOAbstractMessage:(MCOAbstractMessage *)msg canPreviewPart:(MCOAbstractPart *)part {
    NSLog(@"[INFO]yes");
    return YES;
}

@end