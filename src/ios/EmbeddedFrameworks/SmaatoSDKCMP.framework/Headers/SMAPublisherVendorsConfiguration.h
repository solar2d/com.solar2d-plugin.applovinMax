//
//  SMAPublisherVendorsConfiguration.h
//  SmaatoSDKCMP
//
//  Created by Rushikesh Dhule on 27/07/21.
//  Copyright © 2021 Smaato Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SmaatoSDKCMP/SMACMPPublisherVendors.h>
#import <SmaatoSDKCMP/SMAGDPRTypes.h>

NS_ASSUME_NONNULL_BEGIN

/**
 All properties can not be nil of this class. Publisher has to send at-least a Vendor / Custom Vendor. Otherwise a run time error will be
 thrown from cmpConfigurationDidFailWithError delegate method.
 */

@interface SMAPublisherVendorsConfiguration: NSObject

/// An integer array of consent vendors which will fetch the vendors from GVL list.
@property (nonatomic, retain) NSMutableArray *_Nullable vendors;

/// An array(of type SMACMPPublisherVendors) of customVendors which will be used for showing publisher's custom vendors.
@property (nonatomic, retain) NSArray<SMACMPPublisherVendors *> *_Nullable customVendors;

/// A standard option to select a list of vendors
/// enum values:
/// monitization: vendors with ids 1, 2, 4, 6, 7, 8, 9, 10, 11, 12
/// middleGround: vendors with ids 82, 512
/// optimizedPrivacy: vendors with ids 82
@property (nonatomic, assign) CMPStandardOption standardVendorOption;
/// Add Vendors ID in Range
- (NSArray *)addVendorIdsRangeFrom:(NSUInteger)from to:(NSUInteger)to;
@end

NS_ASSUME_NONNULL_END
