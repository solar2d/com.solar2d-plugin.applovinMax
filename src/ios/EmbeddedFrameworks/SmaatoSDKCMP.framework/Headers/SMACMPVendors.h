//
//  SMACMPVendors.h
//  SmaatoSDKCMP
//
//  Created by ashu on 30/07/21.
//

#import <Foundation/Foundation.h>
#import <SmaatoSDKCMP/SMAElement.h>
@class SMAPurpose;
@class SMAFeature;

typedef enum {
    IAB_VENDOR,
    NON_IAB_VENDOR
} VendorType;

NS_ASSUME_NONNULL_BEGIN

@interface SMACMPVendors: NSObject <SMAElement>
/// An identifier for Publisher's Custom Vendor.
@property (nonatomic) int identifier;

/// A name for Publisher's Custom Vendor.
@property (nonatomic, retain) NSString *_Nonnull name;

/// An privacyPolicyLink for Publisher's Custom Vendor.
@property (nonatomic, retain) NSString *_Nonnull privacyPolicyLink;

/// An integer array of purposes to fetch the purposes from GVL list.
@property (nonatomic, retain) NSArray<SMAPurpose *> *_Nullable purposes;

/// An integer array of specialPurposes to fetch the specialPurposes from GVL list.
@property (nonatomic, retain) NSArray<SMAPurpose *> *_Nullable specialPurposes;

/// An integer array of specialPurposes to fetch the specialPurposes from GVL list.
@property (nonatomic, retain) NSArray<SMAPurpose *> *_Nullable flexiblePurpose;

/// An integer array of features to fetch the features from GVL list.
@property (nonatomic, retain) NSArray<SMAFeature *> *_Nullable features;

/// An integer array of specialFeatures to fetch the specialFeatures from GVL list.
@property (nonatomic, retain) NSArray<SMAFeature *> *_Nullable specialFeatures;
/// An integer array of legitimateInterest purpose to fetch the legitimateInterest purpose from GVL list.
@property (nonatomic, retain) NSArray<SMAPurpose *> *_Nullable legitimateIntPurposes;
/// enum for Vendor type.
@property (nonatomic, assign) VendorType vendorType;

/// Longest potential duration for cookie storage
@property (nonatomic) NSUInteger cookieMaxAgeSeconds;

/// Bool to determine if cookie refresh is available
@property (nonatomic, assign) BOOL cookieRefresh;

/// Bool to determine if vendor uses non cookie access
@property (nonatomic, assign) BOOL usesNonCookieAccess;

/// Bool to determine if user has given consent to vendor
@property (nonatomic, assign) BOOL isConsentSelected;

/// Bool to determine if user has given Legitimate interest consent to vendor
@property (nonatomic, assign) BOOL isLegitimateInterestSelected;

@end

NS_ASSUME_NONNULL_END
