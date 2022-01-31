//
//  MARewardedInterstitialAd.h
//  AppLovinSDK
//
//  Created by Thomas So on 6/3/20.
//

#import "MAAdRevenueDelegate.h"

NS_ASSUME_NONNULL_BEGIN

/**
 * This class represents a fullscreen ad that the user can skip or be granted a reward upon successful completion of the ad.
 */
@interface MARewardedInterstitialAd : NSObject

/**
 * Create a new MAX rewarded interstitial.
 *
 * @param adUnitIdentifier Ad unit ID to load ads for.
 */
- (instancetype)initWithAdUnitIdentifier:(NSString *)adUnitIdentifier;

/**
 * Create a new MAX rewarded interstitial.
 *
 * @param adUnitIdentifier Ad unit ID to load ads for.
 * @param sdk              SDK to use. You can obtain an instance of the SDK by calling @code +[ALSdk shared] @endcode.
 */
- (instancetype)initWithAdUnitIdentifier:(NSString *)adUnitIdentifier sdk:(ALSdk *)sdk;

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

/**
 * A delegate that will be notified about ad events.
 */
@property (nonatomic, weak, nullable) id<MARewardedAdDelegate> delegate;

/**
 * A delegate that will be notified about ad revenue events.
 */
@property (nonatomic, weak, nullable) id<MAAdRevenueDelegate> revenueDelegate;

/**
 * Load the current rewarded interstitial. Set @code [MARewardedInterstitialAd delegate] @endcode to assign a delegate that should be notified about ad load
 * state.
 */
- (void)loadAd;

/**
 * Show the loaded interstitial.
 * <ul>
 * <li>Use @code [MARewardedInterstitialAd delegate] @endcode to assign a delegate that should be notified about display events.</li>
 * <li>Use @code [MARewardedInterstitialAd ready] @endcode to check if an ad was successfully loaded.</li>
 * </ul>
 */
- (void)showAd;

/**
 * Show the loaded interstitial for a given placement name that you have assigned, for granular reporting in events.
 * <ul>
 * <li>Use @code [MARewardedInterstitialAd delegate] @endcode to assign a delegate that should be notified about display events.</li>
 * <li>Use @code [MARewardedInterstitialAd ready] @endcode to check if an ad was successfully loaded.</li>
 * </ul>
 *
 * @param placement The placement to tie the showing ad’s events to.
 *
 * @see <a href="https://dash.applovin.com/documentation/mediation/ios/getting-started/advanced-settings#ad-placements">MAX Integration Guide ⇒ iOS ⇒ Advanced Settings ⇒ Ad Placements</a>
 */
- (void)showAdForPlacement:(nullable NSString *)placement;

/**
 * Whether or not this ad is ready to be shown.
 */
@property (nonatomic, assign, readonly, getter=isReady) BOOL ready;

/**
 * Set an extra key/value parameter for the ad.
 *
 * @param key   Parameter key.
 * @param value Parameter value.
 */
- (void)setExtraParameterForKey:(NSString *)key value:(nullable NSString *)value;

/**
 * Set a local extra parameter to pass to the adapter instances. Will not be available in the @code -[MAAdapter initializeWithParameters:withCompletionHandler:] @endcode method.
 *
 * @param key   Parameter key. Must not be null.
 * @param value Parameter value. May be null.
 */
- (void)setLocalExtraParameterForKey:(NSString *)key value:(nullable id)value;

/**
 * Set custom data to be set in the ILRD postbacks via the @c {CUSTOM_DATA}  macro.
 */
@property (nonatomic, copy, nullable) NSString *customPostbackData;

@end

NS_ASSUME_NONNULL_END
