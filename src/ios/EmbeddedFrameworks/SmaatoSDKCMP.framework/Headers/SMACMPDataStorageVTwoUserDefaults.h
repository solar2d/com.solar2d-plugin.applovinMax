//
//  SMAIABDataStorageVTwoUserDefaults.h
//  SmaatoSDKCMP
//
//  Created by muhammad on 22.04.20.
//  Copyright © 2020 Smaato Inc. All rights reserved.
//

#import <SmaatoSDKCMP/SMACMPDataStorageProtocol.h>

@interface SMACMPDataStorageVTwoUserDefaults: NSObject <SMACMPDataStorageProtocol>

+ (instancetype)sharedInstance;

- (void)parseConsentString:(NSString *)consentString;

@end
