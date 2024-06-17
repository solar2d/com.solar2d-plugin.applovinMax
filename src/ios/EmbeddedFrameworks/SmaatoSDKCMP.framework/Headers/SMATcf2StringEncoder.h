//
//  SMATcf2StringEncoder.h
//  SmaatoSDKCMP
//
//  Created by Stefan Meyer on 09.03.21.
//  Copyright © 2021 Smaato Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface SMATcf2StringEncoder: NSObject

@property (nonatomic, copy) NSString *consentLanguage;
@property (nonatomic, copy) NSString *publisherCC;
@property (nonatomic, strong) NSDate *created;
@property (nonatomic, strong) NSDate *lastUpdated;

@property (nonatomic) NSUInteger version;
@property (nonatomic) NSUInteger cmpId;
@property (nonatomic) NSUInteger cmpVersion;
@property (nonatomic) NSUInteger consentScreen;
@property (nonatomic) NSUInteger vendorListVersion;
@property (nonatomic) NSUInteger tcfPolicyVersion;
@property (nonatomic) BOOL isServiceSpecific;
@property (nonatomic) BOOL useNonStandardStacks;
@property (nonatomic) BOOL purposeOneTreatment;

- (void)addAllowedVendors:(NSSet *)allowedVendors;
- (void)addCustomPurposesConsent:(NSSet *)customPurposesConsent;
- (void)addCustomPurposesLITransparency:(NSSet *)customPurposesLITransparency;
- (void)addDisclosedVendors:(NSSet *)disclosedVendors;
- (void)addPublisherRestrictions:(NSArray *)publisherRestrictions;
- (void)addPubPurposesConsent:(NSSet *)pubPurposesConsent;
- (void)addPubPurposesLITransparency:(NSSet *)pubPurposesLITransparency;
- (void)addPurposesConsent:(NSSet *)purposesConsent;
- (void)addPurposesLITransparency:(NSSet *)purposesLITransparency;
- (void)addSpecialFeatureOptIns:(NSSet *)specialFeatureOptIns;
- (void)addVendorConsent:(NSSet *)vendorsConsent;
- (void)addVendorLegitimateInterest:(NSSet *)vendorLegitimateInterest;
- (void)clearAllowedVendors;
- (void)clearCustomPurposesConsent;
- (void)clearCustomPurposesLITransparency;
- (void)clearDisclosedVendors;
- (void)clearPublisherRestriction;
- (void)clearPubPurposesConsent;
- (void)clearPubPurposesLITransparency;
- (void)clearPurposesConsent;
- (void)clearPurposesLITransparency;
- (void)clearSpecialFeatureOptIns;
- (void)clearVendorConsent;
- (void)clearVendorLegitimateInterest;

- (NSString *)encode;
- (NSString *)toTCString;

+ (NSSet *)numbersFrom:(NSUInteger)from to:(NSUInteger)to;

@end

NS_ASSUME_NONNULL_END
