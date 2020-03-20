//
//  KeychainHelper.m
//  Boilerplate
//
//  Created by Hussein Mkwizu on 2/6/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import "KeychainHelper.h"
#import <UICKeyChainStore/UICKeyChainStore.h>

@interface KeychainHelper ()
@property (nonatomic, strong) UICKeyChainStore *keychain;

@end

@implementation KeychainHelper

+(instancetype) sharedInstance{
  
  static dispatch_once_t onceToken;
  static KeychainHelper *sharedInstance;
  
  dispatch_once(&onceToken, ^{
    sharedInstance = [[super alloc] init];
  });
  
  return sharedInstance;
  
}

-(id) init{
  self = [super init];
  if (self != nil) {
    
    //keychain
    self.keychain = [UICKeyChainStore keyChainStoreWithService:[[NSBundle mainBundle] bundleIdentifier]];

  }
  return self;
}

-(BOOL)setString:(NSString *)value forKey:(NSString *)key
{
    return [self.keychain setString:value forKey:key];
}
-(NSString *)stringForKey:(NSString *)key
{
    return [self.keychain stringForKey:key];
}

-(BOOL)setData:(NSData *)data forKey:(NSString *)key
{
    return [self.keychain setData:data forKey:key];
}

-(NSData *)dataForKey:(NSString *)key
{
    return [self.keychain dataForKey:key];
}
@end
