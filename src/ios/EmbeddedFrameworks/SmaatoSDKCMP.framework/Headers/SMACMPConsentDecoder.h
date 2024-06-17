//
//  SMAConsentParser.h
//  SmaatoSDKCMP
//
//  Created by Beniamin Muntean on 26/02/2020.
//  Copyright © 2020 Smaato Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface SMACMPConsentDecoder: NSObject

+ (instancetype)decodeConsentString:(NSString *)consentString;
- (instancetype)initWithConsent:(NSString *)consentString;

- (NSNumber *)parseConsentVersion;
- (NSString *)parseCreated;
- (NSString *)parseUpdated;
- (NSNumber *)parseCMPID;
- (NSNumber *)parseCMPVersion;
- (NSNumber *)parseConsentScreen;
- (NSString *)parseConsentLanguage;
- (NSNumber *)parseVendorListVersion;
- (NSNumber *)parseTcfPolicyVersion;
- (BOOL)parseIsServerSpecific;
- (BOOL)parseUseNonStandardStacks;
- (NSString *)parseSpecialFeatureOptIns;
- (NSString *)parsePurposesConsent;
- (NSString *)parsePurposesLITransparency;
- (NSNumber *)parsePurposeOneTreatment;
- (NSString *)parsePublisherCC;
- (NSString *)parseVendorConsents;
- (NSString *)parseVendorConsentLi;
- (NSArray *)parsePublisherRestrictions;

@end

NS_ASSUME_NONNULL_END
