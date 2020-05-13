//
//  QuickMarkView.h
//  QuickMark
//
//  Created by Nicolas Basset on 17/05/2018.
//  Copyright © 2019 nexguard. All rights reserved.
//

#ifndef QuickMarkView_h
#define QuickMarkView_h
#import <WebKit/WebKit.h>

#pragma mark -

/// QuickMark view error
 typedef enum QMError {
    no_error = 0,
    /// Token is not valid
    invalid_token = 1,
    /// Url or Tenant is not valid
    invalid_url = 2,
    /// Server request failed
    cannot_contact_server = 3,
    /// invalid server answer
    invalid_server_answer = 4,
    /// invalid secret
    invalid_secret = 5,
} QMErrorId;

/// This class create a QuickMarkView
@class QuickMarkView;
#pragma mark -

/// QuickMark protocol : send messages and errors
@protocol QuickMarkViewDelegate <NSObject>
@optional
/// Status message management
-(void)didReceiveMessage:(QuickMarkView*)sender : (NSString *)message;
/// Application share cache data
-(void)didReceiveCacheData:(QuickMarkView*)sender : (NSString *)cacheData;
/// Status error management
-(void)didReceiveError:(QuickMarkView*)sender :(QMErrorId) errorId : (NSString *)message;
@end
#pragma mark -

@interface QuickMarkView: WKWebView <WKScriptMessageHandler>
/// Optional Delegate to get message or/and Error
@property (nonatomic, weak) id <QuickMarkViewDelegate> quickMarkViewDelegate;
// Quickmark state: true if started
@property (nonatomic, readonly) BOOL isStarted;

// init with a cecret key and different parameter
// if secret key is invalid (nil or enpty string), an error occurs
-(id)initWithSecret:(NSString *)sigKey;
-(id)initWithCoder:(NSCoder *)coder andSecretKey:(NSString *)sigKey;
-(id)initWithFrame:(CGRect)frame configuration:(WKWebViewConfiguration *)configuration andSecret:(NSString *)sigKey;
-(id)initWithFrame:(CGRect)frame andSecret:(NSString *)sigKey;

/// Enable log of the QuickMark Web view.
///  @param enable True to enable QuickMark log.
+(void) enableLogWithEnable:(BOOL) enable;
/// Get the version of the QuickMark web view.
///  @return A version string
+(NSString *) getVersion;

/// Load a QuickMark web view initialized with a specified url, a specified tenant and a tenant.
///  @param url The url for the quickmark web view.
///  @param token The token for the quickmark web view.
///  @param tenant The tenant for the quickmark web view.
///  @return An initialized  quickmark web view
- (id) loadQuickMarkViewWithUrl:(NSString * )url :(NSString * )token :(NSString *)tenant;

/// Load a QuickMark web view initialized with a specified url, a specified tenant and a tenant.
/// @result An initialized  QuickMark web view
/// @warning: the url, the token and the tenant must be set outside.
- (id) loadQuickMarkView;

/// Set the specified url used by QuickMark core
/// @param url The url for the QuickMark web view.
/// @return An initialized QuickMark web view
- (id) setUrl: (NSString * )url;

/// Set the specified token used by QuickMark core
/// @param token The token for the QuickMark web view.
/// @return An initialized QuickMark web view
- (id) setToken: (NSString * )token;

/// set the specified tenant used by QuickMark core
/// @param tenant The tenant for the QuickMark web view.
/// @return An initialized QuickMark web view
- (id) setTenant: (NSString * )tenant;

/// Set the specified api key used by QuickMark core
/// @param apiKey The API key for the QuickMark web view.
/// @return An initialized QuickMark web view
/// @note: The API key is optionnal.
- (id) setApiKey: (NSString * )apiKey;

/// Set the specified cache to be used if QM is in offline mode
/// @param cacheData The data previously cached
/// @return An initialized QuickMark web view
- (id) setCacheData: (NSString * )cacheData;

/// Get the specified url used by QuickMark core
/// @return the url string
- (NSString * ) getUrl;

/// Get the specified token used by QuickMark core
/// @return the token string 
- (NSString * ) getToken;

/// Get the specified tenant used by QuickMark core
/// @return the tenant string
- (NSString * ) getTenant;

/// Get the specified api key used by QuickMark core
/// @return the Api Key string
- (NSString * ) getApiKey;

/// Start the watermark process
/// if url, token or tenant is null, an error occurs
- (void) startWatermark;

/// Stop the watermark process
- (void) stopWatermark;

- (NSString *) getCoreVersion;
/// Get the desctiption of the quickmark web view
/// @return the decription of the quickmark web view
- (NSString * ) description;
@end

#endif /* QuickMarkView_h */
