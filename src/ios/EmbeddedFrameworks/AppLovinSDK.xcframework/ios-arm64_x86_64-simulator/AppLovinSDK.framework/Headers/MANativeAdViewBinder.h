//
//  MANativeAdViewBinder.h
//  AppLovinSDK
//
//  Created by Santosh Bagadi on 11/26/21.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@class MANativeAdViewBinderBuilder;

typedef void (^MANativeAdViewBinderBuilderBlock) (MANativeAdViewBinderBuilder *builder);

@interface MANativeAdViewBinderBuilder : NSObject

@property (nonatomic, assign) NSInteger titleLabelTag;
@property (nonatomic, assign) NSInteger advertiserLabelTag;
@property (nonatomic, assign) NSInteger bodyLabelTag;
@property (nonatomic, assign) NSInteger iconImageViewTag;
@property (nonatomic, assign) NSInteger optionsContentViewTag;
@property (nonatomic, assign) NSInteger mediaContentViewTag;
@property (nonatomic, assign) NSInteger callToActionButtonTag;

@end

@interface MANativeAdViewBinder : NSObject

/**
 * The tag for the title label view to be rendered.
 */
@property (nonatomic, assign, readonly) NSInteger titleLabelTag;

/**
 * The tag for advertiser label view to be rendered.
 */
@property (nonatomic, assign, readonly) NSInteger advertiserLabelTag;

/**
 * The tag for body label view to be rendered.
 */
@property (nonatomic, assign, readonly) NSInteger bodyLabelTag;

/**
 * The tag for icon image view to be rendered.
 */
@property (nonatomic, assign, readonly) NSInteger iconImageViewTag;

/**
 * The tag for options content view to be rendered.
 */
@property (nonatomic, assign, readonly) NSInteger optionsContentViewTag;

/**
 * The tag for media content view to be rendered.
 */
@property (nonatomic, assign, readonly) NSInteger mediaContentViewTag;

/**
 * The tag for call to action button view to be rendered.
 */
@property (nonatomic, assign, readonly) NSInteger callToActionButtonTag;

/**
 * Instantiates a @c MANativeAdViewBinder
 */
- (instancetype)initWithBuilderBlock:(MANativeAdViewBinderBuilderBlock)builder;
- (instancetype)init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
