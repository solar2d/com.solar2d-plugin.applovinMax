//
//  PublisherPurposes.h
//  SmaatoSDKCMP
//
//  Created by ashu on 12/07/21.
//

#import <Foundation/Foundation.h>
#import <SmaatoSDKCMP/SMAElement.h>

NS_ASSUME_NONNULL_BEGIN

typedef enum: NSUInteger { STACK, PURPOSE, SPECIAL_PURPOSE, FEATURE, SPECIAL_FEATURE, NON_IAB_PURPOSE } PurposesTypes;

typedef enum: NSUInteger { LEGITIMATE_INTEREST, CONSENT } purposesSubTypes;

@interface SMAPublisherPurposes: NSObject <SMAElement>

- (instancetype)init NS_UNAVAILABLE;

/// An identifier for Publisher's  purpose.
@property (nonatomic) int identifier;

/// An string for Publisher's  purpose description.
@property (nonatomic, retain) NSString *descriptionText;

/// An string for Publisher's  purpose legal description.
@property (nonatomic, retain) NSString *descriptionLegalText;

/// An string for Publisher's  purpose name.
@property (nonatomic, retain) NSString *name;

/// An enum for Publisher's  purpose type.
@property (nonatomic, assign) PurposesTypes purposeType;

@property (nonatomic, assign) purposesSubTypes purposeSubType;

@property (nonatomic, assign) BOOL isConsentSelected;

@property (nonatomic, assign) BOOL isLegitimateInterestSelected;

@property (nonatomic, assign) BOOL isUserConsentPurpose;

@property (nonatomic, assign) BOOL isLegIntPurpose;

- (id)initWithName:(NSString *)name
       description:(NSString *)desc
  descriptionLegal:(NSString *_Nullable)descLegal
    subPurposeType:(purposesSubTypes)subPurpose
        identifier:(int)Id;
@end

NS_ASSUME_NONNULL_END
