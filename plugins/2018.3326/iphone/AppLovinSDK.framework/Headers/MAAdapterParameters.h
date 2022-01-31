//
//  MAAdapterParameters.h
//  AppLovinSDK
//
//  Created by Thomas So on 8/27/18.
//

NS_ASSUME_NONNULL_BEGIN

/**
 * This protocol defines basic parameters passed to all adapter events.
 */
@protocol MAAdapterParameters<NSObject>

/**
 * *********************
 * AVAILABLE IN v11.0.0+
 * *********************
 * <p>
 * The MAX Ad Unit ID the adapter operation with these parameters are being performed for.
 *
 * @return The MAX Ad Unit ID to perform the adapter operation for. Guaranteed not to be null.
 */
@property (nonatomic, copy, readonly) NSString *adUnitIdentifier;

/**
 * *********************
 * AVAILABLE IN v11.0.0+
 * *********************
 * <p>
 * Local extra parameters to passed in from the integration code.
 *
 * @return Local extra parameters. Guaranteed not to be @c nil.
 */
@property (nonatomic, strong, readonly) NSDictionary<NSString *, id> *localExtraParameters;

/**
 * Get parameters passed from AppLovin server to the current adapter.
 *
 * @return Server parameters. Guaranteed not to be nil.
 */
@property (nonatomic, strong, readonly) NSDictionary<NSString *, id> *serverParameters;

/**
 * Current state of user consent.
 *
 * @return @c 1 if the user has provided consent for information sharing. @c nil if not set.
 */
@property (nonatomic, strong, readonly, nullable, getter=hasUserConsent) NSNumber *userConsent;

/**
 * Current state of user age restrictions.
 *
 * @return @c 1 if the user is age restricted (i.e. under 16). @c nil if not set.
 */
@property (nonatomic, strong, readonly, nullable, getter=isAgeRestrictedUser) NSNumber *ageRestrictedUser;

/**
 * Current state of whether ot not the user has opted out of the sale of their personal information.
 *
 * @return @c 1 if the user has opted out of the sale of their personal information. @c nil if not set.
 */
@property (nonatomic, strong, readonly, nullable, getter=isDoNotSell) NSNumber *doNotSell;

/**
 * Check if this request is made for testing.
 *
 * @return @c YES if the ads should be retrieved for testing.
 */
@property (nonatomic, assign, readonly, getter=isTesting) BOOL testing;

@end

NS_ASSUME_NONNULL_END
