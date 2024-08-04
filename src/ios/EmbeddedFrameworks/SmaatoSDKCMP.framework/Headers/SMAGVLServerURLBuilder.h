//
//  SMAGVLServerURLBuilder.h
//  SmaatoSDKCMP
//
//  Created by Stefan Meyer on 23.04.21.
//  Copyright © 2021 Smaato Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface SMAGVLServerURLBuilder: NSObject

+ (NSURL *)baseURLForLocale:(NSLocale *)locale;
+ (NSURL *)localizedPurposeURLForLocale:(NSLocale *)locale;

@end

NS_ASSUME_NONNULL_END
