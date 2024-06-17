//
//  SMACMPLanguage.h
//  SmaatoSDKCMP
//
//  Created by Stefan Meyer on 22.07.21.
//

#import <Foundation/Foundation.h>
#import <SmaatoSDKCMP/SMACMPConsentLanguagesList.h>

NS_ASSUME_NONNULL_BEGIN

static const SMACMPConsentLanguageType DEFAULT_LANGUAGE = EN;

@interface SMACMPLanguage: NSObject

- (instancetype)initWithLocale:(NSLocale *__nullable)locale;

+ (instancetype)defaultLanguage;
+ (NSString *)languageShortcut:(SMACMPConsentLanguageType)lang;
+ (SMACMPConsentLanguageType)languageFromLocale:(NSLocale *)locale;

- (NSString *)shortcut;
- (SMACMPConsentLanguageType)currentLanguage;

@end

NS_ASSUME_NONNULL_END
