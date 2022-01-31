//
//  MAAdapterError.h
//  AppLovinSDK
//
//  Created by Thomas So on 11/13/18.
//

#import "MAError.h"

NS_ASSUME_NONNULL_BEGIN

/**
 * This enum contains possible error codes that should be returned by the mediation adapter.
 */
@interface MAAdapterError : MAError

/**
 * The mediation adapter can not load an ad because of no fill.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeNoFill;
@property (nonatomic, class, readonly) MAAdapterError *noFill;

/**
 * The mediation adapter failed to load an ad for an unspecified reason.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeUnspecified;
@property (nonatomic, class, readonly) MAAdapterError *unspecified;

/**
 * The mediation adapter can not load an ad because it is currently in an invalid state.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeInvalidLoadState;
@property (nonatomic, class, readonly) MAAdapterError *invalidLoadState;

/**
 * The mediation adapter can not load an ad because it is currently not configured correctly.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeInvalidConfiguration;
@property (nonatomic, class, readonly) MAAdapterError *invalidConfiguration;

/**
 * The mediation adapter can not load an ad because of a bad request.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeBadRequest;
@property (nonatomic, class, readonly) MAAdapterError *badRequest;

/**
 * The mediation adapter can not load an ad because the SDK is not initialized yet.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeNotInitialized;
@property (nonatomic, class, readonly) MAAdapterError *notInitialized;

/**
 * The mediation adapter can not load an ad because of a timeout.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeTimeout;
@property (nonatomic, class, readonly) MAAdapterError *timeout;

/**
 * The mediation adapter can not load an ad because it can not detect an active internet connection.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeNoConnection;
@property (nonatomic, class, readonly) MAAdapterError *noConnection;

/**
 * The mediation adapter did not have an ad ready in time for showing.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeAdNotReady;
@property (nonatomic, class, readonly) MAAdapterError *adNotReady;

/**
 * The mediation adapter ran into a remote server error.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeServerError;
@property (nonatomic, class, readonly) MAAdapterError *serverError;

/**
 * The mediation adapter ran into an unspecified internal error.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeInternalError;
@property (nonatomic, class, readonly) MAAdapterError *internalError;

/**
 * The mediation adapter has timed out while collecting a signal.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeSignalCollectionTimeout;
@property (nonatomic, class, readonly) MAAdapterError *signalCollectionTimeout;

/**
 * The mediation adapter does not support signal collection.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeSignalCollectionNotSupported;
@property (nonatomic, class, readonly) MAAdapterError *signalCollectionNotSupported;

/**
 * The mediation adapter ran into a WebView-related error.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeWebViewError;
@property (nonatomic, class, readonly) MAAdapterError *webViewError;

/**
 * The mediation adapter ran into an expired ad.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeAdExpired;
@property (nonatomic, class, readonly) MAAdapterError *adExpiredError;

/**
 * The mediation ad frequency capped.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeAdFrequencyCapped;
@property (nonatomic, class, readonly) MAAdapterError *adFrequencyCappedError;

/**
 * The mediation adapter ran into an error while displaying rewarded ad.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeRewardError;
@property (nonatomic, class, readonly) MAAdapterError *rewardError;

/**
 * The mediation adapter failed to load a native ad because of missing required assets.
 */
@property (nonatomic, class, readonly) NSInteger errorCodeMissingRequiredNativeAdAssets;
@property (nonatomic, class, readonly) MAAdapterError *missingRequiredNativeAdAssets;

/**
 * The error code provided by the third party SDK.
 */
@property (nonatomic, assign, readonly) NSInteger thirdPartySdkErrorCode;

/**
 * The error message provided by the third party SDK.
 */
@property (nonatomic, copy, readonly) NSString *thirdPartySdkErrorMessage;

+ (instancetype)errorWithCode:(NSInteger)code;
+ (instancetype)errorWithCode:(NSInteger)code adapterErrorCode:(NSInteger)adapterErrorCode;
+ (instancetype)errorWithCode:(NSInteger)code errorString:(NSString *)errorString;
+ (instancetype)errorWithNSError:(NSError *)error;
+ (instancetype)errorWithAdapterError:(MAAdapterError *)error thirdPartySdkErrorCode:(NSInteger)thirdPartySdkErrorCode thirdPartySdkErrorMessage:(NSString *)thirdPartySdkErrorMessage;
+ (instancetype)errorWithCode:(NSInteger)code errorString:(NSString *)errorString thirdPartySdkErrorCode:(NSInteger)thirdPartySdkErrorCode thirdPartySdkErrorMessage:(NSString *)thirdPartySdkErrorMessage;
- (instancetype)init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
