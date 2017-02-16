/**
 * TiMailcore Session Proxy
 */

#import "TiMailcoreSession.h"
#import "TiUtils.h"


@implementation TiMailcoreSession

- (id)init: (NSString*)email
    password:(NSString*)pass
    atHost:(NSString*)host
    atPort:(int)port
    withCtype:(MCOConnectionType)ctype
{
    self = [super init];
    if (self != nil) {
        session = [[MCOIMAPSession alloc] init];
        [session setHostname:host];
        [session setPort:port];
        [session setUsername:email];
        [session setPassword:pass];
        [session setConnectionType:ctype];
    }
    return self;
}

- (void)dealloc {
    [session release];
    [super dealloc];
}

// probably cant call this from titanium
- (bool)checkAccount: (void (^)(bool))cb {
    MCOIMAPOperation * op = [session checkAccountOperation];
    [op start:^(NSError * error) {
        if(error) {
            cb(false);
        } else {
            cb(true);
        }
    }];
}

- (void)getFolders:(id)args {
    NSInteger nargs = [args count];
    
    if (nargs >= 1) {
        MCOIMAPFetchFoldersOperation * op = [session fetchAllFoldersOperation];
        [op start:^(NSError * error, NSArray *folders) {
            if(error) {
                [[args objectAtIndex:0] call:@[error, @[]] thisObject:nil];
            } else {
                NSMutableArray * result = [[NSMutableArray alloc] init];
                for(MCOIMAPFolder * folder in folders) {
                    [result addObject: folder.path];
                }
                [[args objectAtIndex:0] call:@[[NSNull null], result] thisObject:nil];
            }
        }];

    } else {
        NSLog(@"[ERROR] Too few arguments to getFolders: callback(error, [folders])");
    }
}

- (void)getMail:(id)args {
    NSInteger nargs = [args count];
    
    if (nargs >= 1) {
        NSString * folder = @"INBOX";
        
        if(nargs >= 2 && [args objectAtIndex:1]) {
            folder = [args objectAtIndex:1];
        }
        
        MCOIndexSet *uids;
        if(nargs >= 3 && [args objectAtIndex:2]) {
            NSArray * range = [args objectAtIndex:2];
            uids = [MCOIndexSet indexSetWithRange:MCORangeMake([TiUtils intValue:range[0]], [TiUtils intValue:range[1]] - [TiUtils intValue:range[0]])];
        } else {
            uids = [MCOIndexSet indexSetWithRange:MCORangeMake(1, UINT64_MAX)];
        }
        
        MCOIMAPMessagesRequestKind requestKind = (MCOIMAPMessagesRequestKind)
        (MCOIMAPMessagesRequestKindHeaders | MCOIMAPMessagesRequestKindHeaderSubject);
        
        MCOIMAPFetchMessagesOperation *fetchOperation = [session fetchMessagesOperationWithFolder:folder requestKind:requestKind uids:uids];
        
        [fetchOperation start:^(NSError * error, NSArray * fetchedMessages, MCOIndexSet * vanishedMessages) {
            if(error) {
                [[args objectAtIndex:0] call:@[error, @[]] thisObject:nil];
            } else {
                NSMutableArray * result = [[NSMutableArray alloc] init];
                for(MCOIMAPMessage * message in fetchedMessages) {
                    [result addObject: @{
                                          @"uid": [NSNumber numberWithInt:message.uid],
                                          @"sender": message.header.sender.displayName,
                                          @"subject": message.header.subject
                                          }];
                }
                [[args objectAtIndex:0] call:@[[NSNull null], result] thisObject:nil];
            }
        }];
    } else {
        NSLog(@"[ERROR] Too few arguments to getFolders: callback(error, [mail items]), <folder>, <[uid min, uid max]>");
    }
}
/*

- (id)getMail: (id)args {
 
    } else {
        NSLog(@"[ERROR] Too few arguments: callback, <folder>");
    }
}

- (id)getMailInfo:(id)args {
    if([args count] >= 2) {
        KrollCallback* callback = [args objectAtIndex:0];
        
        NSNumber * uid = [args objectAtIndex:1];
        
        NSString * folder = ([args count] >= 3) ? [[TiUtils stringValue:[args objectAtIndex:2]] retain] : @"INBOX";
        
        MCOIMAPMessagesRequestKind requestKind = (MCOIMAPMessagesRequestKind)
        (MCOIMAPMessagesRequestKindHeaders | MCOIMAPMessagesRequestKindHeaderSubject | MCOIMAPMessagesRequestKindStructure | MCOIMAPMessagesRequestKindExtraHeaders | MCOIMAPMessagesRequestKindFullHeaders);
        
        MCOIndexSet *uids = [MCOIndexSet indexSetWithRange:MCORangeMake(uid, uid)];
        
        MCOIMAPFetchMessagesOperation *fetchOperation = [session fetchMessagesOperationWithFolder:folder requestKind:requestKind uids:uids];
        
        [fetchOperation start:^(NSError * error, NSArray * fetchedMessages, MCOIndexSet * vanishedMessages) {
            if(error) {
                
            } else {
                if(fetchedMessages.count >= 1) {
                    MCOIMAPMessage * message = [fetchedMessages firstObject];
                    
                    NSMutableDictionary * email = [self compose: nil];
                    [email setObject:message.header.subject forKey:@"subject"];
                } else {
                    NSLog(@"[ERROR] Could not find email message.");
                    [callback call:nil thisObject:nil];
                }
            }
        }];
        
    } else {
        NSLog(@"[ERROR] Too few arguments: callback, uid, <folder>");
    }
}

****
 - (void)getMailHeaders: (id)args {
 if([args count] >= 1) {
 KrollCallback* callback = [args objectAtIndex:0];
 
 NSString * folder_name = ([args count] >= 2) ? [[TiUtils stringValue:[args objectAtIndex:1]] retain] : @"INBOX";
 
 
 MCOIMAPMessagesRequestKind requestKind = (MCOIMAPMessagesRequestKind)
 (MCOIMAPMessagesRequestKindHeaders | MCOIMAPMessagesRequestKindHeaderSubject);
 
 
 MCOIndexSet *uids = [MCOIndexSet indexSetWithRange:MCORangeMake(1, UINT64_MAX)];
 
 MCOIMAPFetchMessagesOperation *fetchOperation = [session fetchMessagesOperationWithFolder:folder requestKind:requestKind uids:uids];
 
 [fetchOperation start:^(NSError * error, NSArray * fetchedMessages, MCOIndexSet * vanishedMessages) {
 
 if(error) {
 NSLog(@"Error downloading message headers:%@", error);
 } else {
 NSMutableArray * results = [[NSMutableArray alloc] init];
 /
 for each email in folder
 NSMutableDictionary * result = [[NSMutableDictionary alloc] init];
 
 NSMutableDictionary * header = [self _getHeaderStructure];
 NSMutableDictionary * addresses = [self _getAddressStructure];
 
 from header info, fill out header and address data
 
 [self _applyHeader: header to:&result];
 [self _applyAddresses: addresses to:&result];
 [results addObject: result]'
 *
 
 if(callback){
 [callback call:results thisObject:nil];
 }
 }
 }];
 } else {
 NSLog(@"[ERROR] Too few arguments: callback, <folder>");
 }
 }
 *****

- (id)compose: (id)args {
    NSLog(@"[INFO] compose");
    NSMutableDictionary * email_data = [self _getEmailStructure];
    NSMutableDictionary * email_header = [self _getHeaderStructure];
    // prepare header
    
    [self _applyHeader: email_header to:&email_data];
    
    return email_data;
    
}

- (id)send: (id)args {
    NSLog(@"[INFO] send");
    
    NSInteger nargs = [args count];
    if (nargs >= 1) {
        NSMutableDictionary * email = [args objectAtIndex:0];
        
        if(nargs >= 2) {
            NSMutableDictionary * addresses;
            if([[args objectAtIndex:1] isMemberOfClass:[NSString class]]) {
                addresses = [self _getAddressStructure];
                [[addresses valueForKey: @"to"] addObject:[args objectAtIndex:1]];
            } else {
                addresses = [args objectAtIndex:1];
            }
            [self _applyAddresses:addresses to:&email];
        }
        [self _send:email];
    } else {
        NSLog(@"[ERROR] Too few arguments: email, <target>");
    }
}

- (NSMutableDictionary*)_getEmailStructure {
    return [@{
              @"subject": @"",
              @"body": @""
              } mutableCopy];
}

- (NSMutableDictionary*)_getHeaderStructure {
    return [@{
              } mutableCopy];
}

- (NSMutableDictionary*)_getAddressStructure {
    return [@{
              @"to": [[NSMutableArray alloc] init],
              @"cc": [[NSMutableArray alloc] init],
              @"bcc": [[NSMutableArray alloc] init],
              @"from": @""
              } mutableCopy];
}

- (void)_applyHeader:(NSMutableDictionary*)header to:(NSMutableDictionary**)email {
    [*email setObject:header forKey:@"header"];
}

- (void)_applyAddresses:(NSMutableDictionary*)address to:(NSMutableDictionary**)email {
    [*email setObject:address forKey:@"address"];
}

-(void)_send:(NSMutableDictionary*)email {
    
}
*/
@end
