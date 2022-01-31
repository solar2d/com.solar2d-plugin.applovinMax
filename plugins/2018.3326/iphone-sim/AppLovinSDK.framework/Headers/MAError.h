//
//  MAError+Internal.h
//  AppLovinSDK
//
//  Created by Thomas So on 5/3/21.
//

#import <Foundation/Foundation.h>
#import "MAErrorCode.h"
#import "MAAdWaterfallInfo.h"

NS_ASSUME_NONNULL_BEGIN

/**
 * This class encapsulates various data for MAX load and display errors.
 */
@interface MAError : NSObject

/**
 * The error code for the error. Will be one of the codes listed under the @c MAErrorCode enum.
 */
@property (nonatomic, assign, readonly) MAErrorCode code;

/**
 * The error message for the error.
 */
@property (nonatomic, copy, readonly) NSString *message;

/**
 * The underlying waterfall of ad responses.
 */
@property (nonatomic, strong, readonly) MAAdWaterfallInfo *waterfall;

- (instancetype)init NS_UNAVAILABLE;
- (instancetype)new NS_UNAVAILABLE;

@end

@interface MAError(ALDeprecated)
@property (nonatomic, assign, readonly) NSInteger errorCode __deprecated_msg("This property is deprecated and removed in a future SDK version. Please use `-[MAError code]` instead.");
@property (nonatomic, copy, readonly) NSString *errorMessage __deprecated_msg("This property is deprecated and removed in a future SDK version. Please use `-[MAError message]` instead.");
@property (nonatomic, copy, readonly, nullable) NSString *adLoadFailureInfo __deprecated_msg("The ad load failure info string is deprecated and removed in a future SDK version. Please use `-[MAError waterfall]` instead.");
@end

NS_ASSUME_NONNULL_END
