//
//  SMAGVListViewModel.h
//  SmaatoSDKCMP
//
//  Created by ashu on 12/07/21.
//

#import <Foundation/Foundation.h>
#import <SmaatoSDKCMP/SMAPublisherPurposesConfiguration.h>
#import <SmaatoSDKCMP/SMAPublisherVendorsConfiguration.h>
#import <SmaatoSDKCMP/SMACMPVendors.h>

@class SMAGlobalVendorList;

/// Completion handler to receive SMAPublisherPurposes array
typedef void (^PublisherSelectedListLoadingOnDidLoad)(NSArray<SMAPublisherPurposes *> *_Nullable list, NSError *_Nullable error);
/// Completion handler to receive SMACMPVendors array
typedef void (^PublisherSelectedVendorsLoadingOnDidLoad)(NSArray<SMACMPVendors *> *_Nullable list, NSError *_Nullable error);
NS_ASSUME_NONNULL_BEGIN

@interface SMAGVListViewModel: NSObject

- (instancetype)init NS_UNAVAILABLE;
- (instancetype)initWithVendorList:(SMAGlobalVendorList *_Nullable)list NS_DESIGNATED_INITIALIZER;

- (void)objConverterWithCompletion:(void (^)(void))block;

/// To get SMAPublisherPurposes object array from publisher configuration integer array to display on welcome  screen
- (void)puposesToDisplayOnWelcomeScreen:(SMAPublisherPurposesConfiguration *)publisherConfig
                  publisherVendorConfig:(SMAPublisherVendorsConfiguration *)publisherVendorConfig
                      completionHandler:(PublisherSelectedListLoadingOnDidLoad)completion;
/// To get SMACMPVendors object array from publisher vendor integer array to display on privacy screen
- (void)vendorsToDisplayOnPrivacyCenter:(SMAPublisherVendorsConfiguration *)publisherVendorConfig
                       pubPurposeConfig:(SMAPublisherPurposesConfiguration *)publisherConfig
                      completionHandler:(PublisherSelectedVendorsLoadingOnDidLoad)completion;
/// Handle AcceptAll/RejectAll consent puposes and vendors in WelcomeScreen and PrivacyCenter screen
- (void)createTCStringFromWelcomeScreenWithSelectedLanguage:(NSString *)langStr
                                         isAcceptAllConsent:(BOOL)isAcceptAll
                                     publisherConfiguration:(SMAPublisherPurposesConfiguration *)pubConfig
                                        vendorConfiguration:(SMAPublisherVendorsConfiguration *)vendorConfig;
/// Handle selected consent puposes and vendors to store in TCString
- (void)createTCStringWith:(NSArray<SMAPublisherPurposes *> *)selectedPurposes
                andVendors:(NSArray<SMACMPVendors *> *)selectedVendors
          selectedLanguage:(NSString *)langStr;
// Handle cancel tap on Privacy Center screen
- (void)handlingTCStringOnCancelTapWithSelectedLanguage:(NSString *)langStr
                                        publisherConfig:(SMAPublisherPurposesConfiguration *)pubConfig
                                    vendorConfiguration:(SMAPublisherVendorsConfiguration *)vendorConfig;
// To get SMAPublisherPurposes object array from publisher configuration integer array to display on welcome  screen
- (void)getPurposesAndFeatureListForPrivacyCenterWithVendorConfig:(SMAPublisherVendorsConfiguration *)publisherVendorConfig
                                                 pubPurposeConfig:(SMAPublisherPurposesConfiguration *)publisherPurposeConfig
                                                completionHandler:(PublisherSelectedListLoadingOnDidLoad)completion;
@end

NS_ASSUME_NONNULL_END
