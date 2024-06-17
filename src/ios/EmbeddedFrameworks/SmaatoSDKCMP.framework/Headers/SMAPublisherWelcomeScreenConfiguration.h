//
//  SMAPublisherWelcomeScreenConfiguration.h
//  SmaatoSDKCMP
//
//  Created by Suraj Potphode on 20/07/21.
//  Copyright © 2021 Smaato Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef enum backgroundTypes { BACKGROUND_BLUR, BACKGROUND_TRANSPARENT, BACKGROUND_IMAGE } WelcomeScreenBackground;

@interface SMAPublisherWelcomeScreenConfiguration: NSObject

/// A welcome screen title that a publisher can set on the welcome screen
@property (nonatomic, retain) NSString *_Nullable welcomeScreenTitle;

/// A welcome screen boilerPlate description that a publisher can set on the welcome screen
@property (nonatomic, retain) NSString *_Nullable boilerDescriptionText;

/// A privacy policy link string that a publisher should set to redirect user when user needs to see the privacy policy
@property (nonatomic, retain, readonly) NSString *_Nonnull privacyPolicyLink;

/// A boolean value to show/hide reject all button on the welcome screen
@property (nonatomic) BOOL shouldShowRejectAllButton;

/// A double value to set the content view percentage to be shown on the welcome screen
@property (nonatomic) double percentageOfScreen;

/// To set the type of background that publisher wants for the welcome screen from below options
/// BACKGROUND_BLUR (default),
/// BACKGROUND_TRANSPARENT,
/// BACKGROUND_IMAGE
@property (nonatomic) WelcomeScreenBackground welcomeScreenbackground;

/// To set the background image if the WelcomeScreenBackground type is set as BACKGROUND_IMAGE for the welcome screen
@property (nonatomic, retain) UIImage *_Nullable backgroundImage;

- (instancetype)init NS_UNAVAILABLE;

/// To initialize the SMAPublisherWelcomeScreenConfiguration with mandatory parameter privacyPolicyLink
- (instancetype)initWithPrivacyPolicyLink:(NSString *)privacyPolicyLink;

@end

NS_ASSUME_NONNULL_END
