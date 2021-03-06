//
//  MAAdWaterfallInfo.h
//  AppLovinSDK
//
//  Created by Thomas So on 10/30/21.
//

#import <Foundation/Foundation.h>
#import "MANetworkResponseInfo.h"

@class MAAd;

NS_ASSUME_NONNULL_BEGIN

/**
 * This class represents an ad waterfall, encapsulating various metadata such as total latency, underlying ad responses, etc.
 */
@interface MAAdWaterfallInfo : NSObject

/**
 * The loaded ad, if any, for this waterfall.
 */
@property (nonatomic, weak, readonly, nullable) MAAd *loadedAd;

/**
 * The list of @c MAAdapterResponseInfo info objects relating to each ad in the waterfall, ordered by their position.
 */
@property (nonatomic, strong, readonly) NSArray<MANetworkResponseInfo *> *networkResponses;

/**
 * The total latency in seconds for this waterfall to finish processing.
 */
@property (nonatomic, assign, readonly) NSTimeInterval latency;

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
