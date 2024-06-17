//
//  UserConsentData.h
//  SmaatoSDKCMP
//
//  Created by ashu on 23/08/21.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface UserConsentData: NSObject
@property (nonatomic, retain) NSNumber *gvlSpecificationVersion;
@property (nonatomic, retain) NSNumber *tcfPolicyVersion;
@property (nonatomic, retain) NSNumber *vendorListVersion;
@property (nonatomic, assign) NSString *lastUpdated;
@property (nonatomic, retain) NSArray *purposeConsent;
@property (nonatomic, retain) NSArray *customPurposeConsent;
@property (nonatomic, retain) NSArray *vendorConsent;
@property (nonatomic, retain) NSArray *customVendorConsent;
@property (nonatomic, retain) NSArray *purposesLegitimateInterestConsent;
@property (nonatomic, retain) NSArray *customPurposesLegitimateInterestConsent;
@property (nonatomic, retain) NSArray *vendorLegitimateInterestConsent;
@property (nonatomic, retain) NSArray *customVendorLegitimateInterestConsent;
@property (nonatomic, retain) NSArray *specialFeatureOptIns;
@end

NS_ASSUME_NONNULL_END
