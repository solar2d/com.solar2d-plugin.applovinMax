//
//  SMACMPConsentLanguagesList.h
//  SmaatoSDKCMP
//
//  Created by Rushikesh Dhule on 22/07/21.
//  Copyright © 2021 Smaato Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 Below is the list of languages from GVL.
 Refer - https://register.consensu.org/translation
 */

typedef NS_ENUM(NSInteger, SMACMPConsentLanguageType) {
    EN,
    BG,
    CA,
    CS,
    DA,
    DE,
    EL,
    ES,
    ET,
    EUS,
    FI,
    FR,
    GL,
    HR,
    HU,
    IT,
    JA,
    LT,
    LV,
    MT,
    NL,
    NORWEGIAN,
    PL,
    PT,
    RO,
    SRCYRL,
    SRLATN,
    RU,
    SK,
    SL,
    SV,
    TR,
    ZH
};

NS_ASSUME_NONNULL_BEGIN

@interface SMACMPConsentLanguagesList: NSObject

+ (NSString *)getCountryCodeFromEnum:(SMACMPConsentLanguageType)languageCode;
+ (SMACMPConsentLanguageType)getConsentCountryTypeFromString:(NSString *)countryCode;

@end

NS_ASSUME_NONNULL_END
