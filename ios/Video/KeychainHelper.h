//
//  KeychainHelper.h
//  Boilerplate
//
//  Created by Hussein Mkwizu on 2/6/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface KeychainHelper : NSObject
+(KeychainHelper *) sharedInstance;

-(BOOL)setString:(NSString *)value forKey:(NSString *)key;
-(NSString *)stringForKey:(NSString *)key;

-(BOOL)setData:(NSData *)data forKey:(NSString *)key;
-(NSData *)dataForKey:(NSString *)key;

@end

NS_ASSUME_NONNULL_END
