//
//  SMAPublisherPurposesConfiguration.h
//  SmaatoSDKCMP
//
//  Created by Rushikesh Dhule on 19/07/21.
//  Copyright © 2021 Smaato Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SmaatoSDKCMP/SMAPublisherPurposes.h>
#import <SmaatoSDKCMP/SMAGDPRTypes.h>

NS_ASSUME_NONNULL_BEGIN

/**
 All properties can not be nil of this class. Publisher has to send at-least a Purpose / Stack / Special Purpose / CustomPurpose / Feature /
 SpecialFeature. Otherwise a run time error will be thrown from cmpConfigurationDidFailWithError delegate method.
 */

@interface SMAPublisherPurposesConfiguration: NSObject

/// An integer array of purposes to fetch the purposes from GVL list.
@property (nonatomic, retain) NSArray *_Nullable purposes;

/// An integer array of stacks to fetch the stacks from GVL list.
@property (nonatomic, retain) NSArray *_Nullable stacks;

/// An array(of type SMAPublisherPurposes) of customPurposes to be used for showing publisher's custom purposes.
@property (nonatomic, retain) NSArray<SMAPublisherPurposes *> *_Nullable customPurposes;

/// An integer array of specialFeatures to fetch the specialFeatures from GVL list.
@property (nonatomic, retain) NSArray *_Nullable specialFeatures;

/// A standard option to select a list of predefined purposes, stacks and special features
/// enum values:
/// monitization: purpose 1, stack 39 & special feature 1
/// middleGround: purpose 1, stack 39
/// optimizedPrivacy: purpose 1, stack 6
@property (nonatomic, assign) CMPStandardOption standardPurposeOption;

/// A message for empty user consent tab that a publisher can set for the privacy screen
@property (nonatomic, retain) NSString *_Nullable emptyMessageForUserConsent;

/// A message for empty features tab that a publisher can set for the privacy screen
@property (nonatomic, retain) NSString *_Nullable emptyMessageForFeatures;

@end

NS_ASSUME_NONNULL_END
