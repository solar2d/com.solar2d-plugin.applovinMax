//
//  SMATcf2DataStorageUserDefaults.h
//  SmaatoSDKCMP
//
//  Created by Stefan Meyer on 18.03.21.
//  Copyright © 2021 Smaato Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SmaatoSDKCMP/SMACMPDataStorageVTwoUserDefaults.h>

NS_ASSUME_NONNULL_BEGIN

@interface SMATcf2DataStorageUserDefaults: SMACMPDataStorageVTwoUserDefaults

+ (instancetype)shared;
- (NSString *)generateConsentString;
- (void)storeConsentString;

@end

@interface NSString (IAB)
+ (NSString *)fromSet:(NSSet *)set withLength:(NSUInteger)length;
- (NSSet *)asSet;
@end

NS_ASSUME_NONNULL_END
