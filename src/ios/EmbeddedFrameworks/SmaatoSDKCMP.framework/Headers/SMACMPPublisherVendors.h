//
//  SMACMPPublisherVendors.h
//  SmaatoSDKCMP
//
//  Created by Rushikesh Dhule on 26/07/21.
//  Copyright © 2021 Smaato Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SmaatoSDKCMP/SMAPublisherPurposes.h>

NS_ASSUME_NONNULL_BEGIN

@interface SMACMPPublisherVendors: NSObject

/// An identifier for Publisher's Custom Vendor.
@property (nonatomic, readonly) int identifier;

/// A name for Publisher's Custom Vendor.
@property (nonatomic, retain, readonly) NSString *_Nonnull name;

/// An privacyPolicyLink for Publisher's Custom Vendor.
@property (nonatomic, retain, readonly) NSString *_Nonnull privacyPolicyLink;

/// Longest potential duration for cookie storage
@property (nonatomic) NSUInteger cookieMaxAgeSeconds;

/// Bool to determine if cookie refresh is available
@property (nonatomic, assign) BOOL cookieRefresh;

/// Bool to determine if vendor uses non cookie access
@property (nonatomic, assign) BOOL usesNonCookieAccess;

/**
 All below properties can not be nil of this class. Publisher has to send at-least a Purpose / Stack / Special Purpose / CustomPurpose /
 Feature / SpecialFeature. Otherwise a run time error will be thrown from cmpConfigurationDidFailWithError delegate method.
 */

/// An integer array of purposes to fetch the purposes from GVL list.
@property (nonatomic, retain) NSArray *_Nullable purposes;

/// An integer array of specialPurposes to fetch the specialPurposes from GVL list.
@property (nonatomic, retain) NSArray *_Nullable specialPurposes;

/// An array(of type SMAPublisherPurposes) of customPurposes to be used for showing publisher's custom purposes.
@property (nonatomic, retain) NSArray<SMAPublisherPurposes *> *_Nullable customPurposes;

/// An integer array of purposes to fetch the purposes from GVL list.
@property (nonatomic, retain) NSArray *_Nullable flexiblePurpose;

/// An integer array of features to fetch the features from GVL list.
@property (nonatomic, retain) NSArray *_Nullable features;

/// An integer array of specialFeatures to fetch the specialFeatures from GVL list.
@property (nonatomic, retain) NSArray *_Nullable specialFeatures;

/// An integer array of legitimateInterest purpose to fetch the legitimateInterest from GVL list.
@property (nonatomic, retain) NSArray *_Nullable legitimateIntPurposes;

- (instancetype)init NS_UNAVAILABLE;

- (instancetype)initWithIdentifier:(int)identifier
                              name:(NSString *)name
                 privacyPolicyLink:(NSString *)privacyPolicyLink
               cookieMaxAgeSeconds:(NSUInteger)cookieMaxAgeSeconds
                     cookieRefresh:(BOOL)cookieRefresh
               usesNonCookieAccess:(BOOL)usesNonCookieAccess;

@end

NS_ASSUME_NONNULL_END
