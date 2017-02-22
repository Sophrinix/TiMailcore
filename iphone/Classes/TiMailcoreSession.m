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
        rendering_delegate = [[TiMailcoreRenderingDelegate alloc] init];
        
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
    [rendering_delegate release];
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
                [[args objectAtIndex:0] call:@[[error description], @[]] thisObject:nil];
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
                [[args objectAtIndex:0] call:@[[error description], @[]] thisObject:nil];
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
        NSLog(@"[ERROR] Too few arguments to getMail: callback(error, [mail items]), <folder>, <[uid min, uid max]>");
    }
}

- (void)getMailInfo:(id)args {
    NSInteger nargs = [args count];
    
    if (nargs >= 2) {
        NSString * folder = @"INBOX";
    
        if(nargs >= 3 && [args objectAtIndex:2]) {
            folder = [args objectAtIndex:2];
        }

        MCOIMAPFetchContentOperation * op = [session fetchMessageOperationWithFolder:folder uid:[TiUtils intValue:[args objectAtIndex:0]]];
        
        [op start:^(NSError * error, NSData * data) {
            if(error) {
                [[args objectAtIndex:1] call:@[[error description], @{}] thisObject:nil];
            } else {
                NSMutableDictionary * email = [self compose: nil];
                NSMutableDictionary * email_headers = [email valueForKey:@"headers"];
                NSMutableDictionary * email_addresses = [email valueForKey:@"addresses"];
                
                MCOMessageHeader * header = [MCOMessageHeader headerWithData: data];
                MCOMessageParser * parser = [MCOMessageParser messageParserWithData:data];
                
                if(header) {
                    // Basic data and headers
                    if(header.subject) {
                        [email setObject:header.subject forKey:@"subject"];
                    }
                    if(header.date) {
                        [email_headers setObject:[header.date description] forKey:@"date"];
                    }
                    if(header.receivedDate) {
                        [email_headers setObject:[header.receivedDate description] forKey:@"received_date"];
                    }
                    if(header.allExtraHeadersNames) {
                        for(NSString * extra in [header allExtraHeadersNames]) {
                            NSString * extra_data = [header extraHeaderValueForName:extra];
                            if(extra_data) {
                                [email_headers setObject:extra_data forKey:extra];
                            }
                        }
                    }
                    
                    // 'From' address
                    if(header.from) {
                        NSMutableDictionary * from = [email_addresses valueForKey:@"from"];
                        NSString * display_name = header.from.displayName;
                        NSString * mailbox = header.from.mailbox;
                        if(display_name) {
                            [from setObject:display_name forKey:@"name"];
                        }
                        if(mailbox) {
                            [from setObject:mailbox forKey:@"mailbox"];
                        }
                    }
                    
                    // Remainder of the address types
                    NSDictionary * address_sections = @{
                                                     @"to": header.to ? header.to : @[],
                                                     @"cc": header.cc ? header.cc : @[],
                                                     @"bcc": header.bcc ? header.bcc : @[],
                                                     @"replyTo": header.replyTo ? header.replyTo : @[]
                                                     };
                    
                    for(NSString * address_section in address_sections.allKeys) {
                        NSArray * addresses = [address_sections valueForKey:address_section];
                        
                        if(addresses) {
                            NSMutableArray * my_addresses = [[NSMutableArray alloc] init];
                            for(MCOAddress * address in addresses) {
                                NSMutableDictionary * new_address = [[NSMutableDictionary alloc] init];
                                
                                NSString * display_name = address.displayName;
                                NSString * mailbox = address.mailbox;
                                if(display_name) {
                                    [new_address setObject:display_name forKey:@"name"];
                                }
                                if(mailbox) {
                                    [new_address setObject:mailbox forKey:@"mailbox"];
                                }
                                [my_addresses addObject:new_address];
                            }
                            [email_addresses setObject:my_addresses forKey:address_section];
                        }
                    }
                }
                if(parser) {
                    [email setObject:[parser htmlBodyRendering] forKey:@"body"];
                }
                
                [[args objectAtIndex:1] call:@[[NSNull null], email] thisObject:nil];
            }
        }];
    } else {
        NSLog(@"[ERROR] Too few arguments to getMailInfo: uid, callback(error, {email}), <folder>");
    }
}

- (id)compose: (id)args {
    NSMutableDictionary * email_data = [self _getEmailStructure];
    NSMutableDictionary * email_header = [self _getHeaderStructure];
    NSMutableDictionary * email_address = [self _getAddressStructure];
    
    [self _applyHeader: email_header to:&email_data];
    [self _applyAddresses: email_address to:&email_data];
    
    return email_data;
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
              @"from": [[NSMutableDictionary alloc] init],
              @"replyTo": [[NSMutableDictionary alloc] init]
              } mutableCopy];
}

- (void)_applyHeader:(NSMutableDictionary*)header to:(NSMutableDictionary**)email {
    [*email setObject:header forKey:@"headers"];
}

- (void)_applyAddresses:(NSMutableDictionary*)address to:(NSMutableDictionary**)email {
    [*email setObject:address forKey:@"addresses"];
}

@end
