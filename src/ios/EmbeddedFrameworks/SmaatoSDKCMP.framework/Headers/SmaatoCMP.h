//
//  SmaatoCMP.h
//  SmaatoSDKCMP
//
//  Created by Stefan Meyer on 09.07.21.
//  Copyright © 2021 Smaato Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SmaatoSDKCMP/SMACMPConfiguration.h>
#import <SmaatoSDKCMP/UserConsentData.h>

NS_ASSUME_NONNULL_BEGIN

typedef enum: NSUInteger {
    SmaatoCMPConsentStatusUndefined = 0,
    SmaatoCMPConsentStatusReady,
    SmaatoCMPConsentStatusExpired,
} SmaatoCMPConsentStatus;

@protocol SMACMPFileLoader <NSObject>
- (void)loadFileForUrl:(NSURL *)url response:(void (^)(NSData *__nullable fileData, NSError *__nullable error))response;
@end

@interface SmaatoCMP: NSObject <SMACMPFileLoader>

+ (UserConsentData *)getUserConsentData NS_SWIFT_NAME(userConsentData());

/// Access a configured instance of SmaatoCMP after calling initCMPWithConfig:
+ (instancetype)sharedInstance NS_SWIFT_NAME(shared());

- (instancetype)init NS_UNAVAILABLE;
- (instancetype)initCMPWithConfig:(id<SMACMPConfiguration>)config NS_DESIGNATED_INITIALIZER NS_SWIFT_NAME(init(withConfig:));

/**
 Enable GDPR if you are not using the Core module and your app is used in a GDPR region.
 BOOL enableGDPR: default = NO
 */
- (void)setEnableGDPR:(BOOL)enableGDPR;

- (void)requestWelcomeScreenOn:(UIViewController *)viewController completion:(void (^__nullable)(void))completion;
- (void)requestWelcomeScreenOn:(UIViewController *)viewController;

- (void)showPrivacyCenter;
- (void)showPrivacyCenterWithCompletion:(void (^_Nullable)(void))completion;
- (SmaatoCMPConsentStatus)consentStatus;

- (void)resetAllCMPData;

/// This Api will be used to check if User is in GDPR region or isConsentRequiredWorldwide and consent given is not older than 13 months. If
/// consent is older than 13 months then Welcome Screen will be displayed and users will be asked again for consents.
- (void)checkGDPR:(UIViewController *_Nonnull)viewController onCompletion:(void (^)(BOOL needNewUserConsent))completion;

@end

NS_ASSUME_NONNULL_END
