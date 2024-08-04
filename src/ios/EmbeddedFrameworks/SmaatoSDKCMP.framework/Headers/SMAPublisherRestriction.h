//
//  SMAPublisherRestriction.h
//  SmaatoSDKCMP
//
//  Created by Stefan Meyer on 09.03.21.
//  Copyright © 2021 Smaato Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SmaatoSDKCMP/SMAGDPRTypes.h>

NS_ASSUME_NONNULL_BEGIN

@interface SMAPublisherRestriction: NSObject <NSCoding>

@property (nonatomic, assign) NSUInteger purposeId;
@property (nonatomic, assign) SMARestrictionType restrictionType;

+ (NSArray<NSDictionary *> *)convertToDictionary:(NSArray *)restrictions;
+ (NSArray<SMAPublisherRestriction *> *)convertFromDictionary:(NSArray<NSDictionary *> *)restrictions;

- (instancetype)init NS_UNAVAILABLE;
- (instancetype)initWithPurposeID:(NSUInteger)purposeId andType:(SMARestrictionType)type NS_DESIGNATED_INITIALIZER;
// old initializer from core module
- (instancetype)initWithPurposeID:(NSUInteger)purposeID restrictionType:(NSUInteger)restrictionType andEntries:(NSString *)entries;

- (NSSet *)vendorIds;
- (void)addVendors:(NSSet *)vendors;

@end

NS_ASSUME_NONNULL_END
