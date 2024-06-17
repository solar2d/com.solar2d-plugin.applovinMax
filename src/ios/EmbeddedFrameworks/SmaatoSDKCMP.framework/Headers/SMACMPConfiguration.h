//
//  SMACMPConfiguration.h
//  SmaatoSDKCMP
//
//  Created by Stefan Meyer on 12.07.21.
//

#import <Foundation/Foundation.h>
#import <SmaatoSDKCMP/SMAPublisherPurposesConfiguration.h>
#import <SmaatoSDKCMP/SMAPublisherVendorsConfiguration.h>
#import <SmaatoSDKCMP/SMAPublisherWelcomeScreenConfiguration.h>
#import <SmaatoSDKCMP/SMACMPConsentLanguagesList.h>

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol SMACMPConfiguration <NSObject>

@optional

/// A delegate to set user language so that GVL list can be shown in the assigned langugae. Default value is user's current language
/// preference.
- (SMACMPConsentLanguageType)userLanguage;

/// A delegate which tells if the configuration is set correctly. If it is not, then the delegate will return which values are missing from
/// the publisher.
- (void)cmpConfigurationDidFailWithError:(NSError *)error;

/// A delegate which tells if the configuration is set correctly. This delegate will get triggered when CMP viewControllers are dismissed.
- (void)cmpConfigurationViewDidDismiss;

/// A delegate which tell the SDK that the consent is allowed to any region in the world. Not just restricted to GDPR. By default the value
/// is False.
- (BOOL)isConsentRequiredWorldwide;

@required

/// A delegate to set welcome screen UI as per publisher's request. The only mandatory paramater in SMAPublisherWelcomeScreenConfiguration
/// is privacyPolicyLink.
- (SMAPublisherWelcomeScreenConfiguration *)publisherWelcomeScreenConfiguration;

/// A delegate to set publisher specific purpose choices from GVL list.
- (SMAPublisherPurposesConfiguration *)publisherPurposesConfiguration;

/// A delegate to set publisher specific vendor choices from GVL list.
- (SMAPublisherVendorsConfiguration *)publisherVendorsConfiguration;

/// A delegate to set publisher country code. The country code of the country that determines legislation of reference and is mandatory and
/// has no default.
- (NSString *)publisherCountryCode;

@end

@interface SMACMPDefaultConfiguration: NSObject <SMACMPConfiguration>

@end

NS_ASSUME_NONNULL_END
