/**
 * TiMailcore
 */

#import "TiMailcoreModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"
#import "TiMailcoreSession.h"

@implementation TiMailcoreModule

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"094010ee-228c-46ba-bac9-0667e650ed5b";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"ti.mailcore";
}

#pragma mark Lifecycle

-(void)startup
{
	// this method is called when the module is first loaded
	// you *must* call the superclass
	[super startup];

	NSLog(@"[INFO] %@ loaded",self);
}

-(void)shutdown:(id)sender
{
	// this method is called when the module is being unloaded
	// typically this is during shutdown. make sure you don't do too
	// much processing here or the app will be quit forceably

	// you *must* call the superclass
	[super shutdown:sender];
}

#pragma mark Cleanup

-(void)dealloc
{
	// release any resources that have been retained by the module
	[super dealloc];
}

#pragma mark Internal Memory Management

-(void)didReceiveMemoryWarning:(NSNotification*)notification
{
	// optionally release any resources that can be dynamically
	// reloaded once memory is available - such as caches
	[super didReceiveMemoryWarning:notification];
}

#pragma mark Listener Notifications

-(void)_listenerAdded:(NSString *)type count:(int)count
{
	if (count == 1 && [type isEqualToString:@"my_event"])
	{
		// the first (of potentially many) listener is being added
		// for event named 'my_event'
	}
}

-(void)_listenerRemoved:(NSString *)type count:(int)count
{
	if (count == 0 && [type isEqualToString:@"my_event"])
	{
		// the last listener called for event named 'my_event' has
		// been removed, we can optionally clean up any resources
		// since no body is listening at this point for that event
	}
}

#pragma Public APIs

-(id)createSession:(id)args {
    NSInteger nargs = [args count];
    
    if (nargs >= 1) {
        TiMailcoreSession * session = nil;
        id params = [args objectAtIndex:0];
        
        NSString * email = [params objectForKey:@"email"];
        NSString * password = [params objectForKey:@"password"];
        NSString * atHost = [params objectForKey:@"host"];
        int port = [params objectForKey:@"port"] ? [TiUtils intValue:[params objectForKey:@"port"]] : 993;
        MCOConnectionType ctype = [params objectForKey:@"ctype"] ? [TiUtils intValue:[params objectForKey:@"ctype"]] : MCOConnectionTypeTLS;
        
        session = [[TiMailcoreSession alloc]
           init: email
           password: password
           atHost: atHost
           atPort: port
           withCtype: ctype
        ];
        
        [session checkAccount: ^(bool success)
           {
               if(success) {
                   if([params objectForKey:@"onsuccess"]) {
                       [[params objectForKey:@"onsuccess"] call: @[session] thisObject:nil];
                   }
               } else {
                   if([params objectForKey:@"onerror"]) {
                       [[params objectForKey:@"onerror"] call: @[@"Could not authenticate."] thisObject:nil];
                   }
               }
           }
        ];
    } else {
        NSLog(@"[ERROR] Too few arguments to createSession: {email, password, host, <port>, <ctype>, <onsuccess(session)>, <onerror(error)>}");
    }
    
    return nil;
}


@end
