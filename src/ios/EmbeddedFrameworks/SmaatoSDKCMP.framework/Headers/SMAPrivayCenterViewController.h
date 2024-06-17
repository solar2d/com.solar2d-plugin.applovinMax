//
//  SMAPrivayCenterViewController.h
//  SmaatoSDKCMP
//
//  Created by Smaato Inc on 20/07/2021.
//  Copyright © 2021 Smaato Inc. All rights reserved.￼
//  Licensed under the Smaato SDK License Agreement￼
//  https://www.smaato.com/sdk-license-agreement/

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface SMAPrivayCenterViewController: UIViewController
+ (SMAPrivayCenterViewController *)instatiateFromNib;

/// To show site vendors tab directly
@property (nonatomic, assign) BOOL shouldShowSiteVendorsTab;

/// To see if presented from the Welcome screen
@property (nonatomic, assign) BOOL isPresentedFromWelcomeScreen;

/// To get Welcome screen VC access, to dismiss Splash screen on accept all, save & exit button taps
@property (nonatomic, strong) UIViewController *welcomeScreenVC;

@end

NS_ASSUME_NONNULL_END
