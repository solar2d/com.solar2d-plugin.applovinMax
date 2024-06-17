//
//  SMAGlobalVendorList.h
//  SmaatoSDKCMP
//
//  Created by Stefan Meyer on 29.03.21.
//  Copyright © 2021 Smaato Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SmaatoSDKCMP/SMAGVL.h>

typedef void (^SMAGVLLoadingOnDidLoad)(SMAGVL *_Nullable gvl, NSError *_Nullable error);

NS_ASSUME_NONNULL_BEGIN

@interface SMAGlobalVendorList: NSObject

@property (nonatomic, strong, nullable) NSURLSessionDataTask *task;
@property (nonatomic, strong) SMAGVL *objSMAGVL;

- (void)fetchGVL:(SMAGVLLoadingOnDidLoad)completion language:(NSString *)lang;

/**
 Force a reload of the file from network.
 */
- (void)fetchGVL:(BOOL)force language:(NSString *)lang withCompletion:(SMAGVLLoadingOnDidLoad)completion;

@end

NS_ASSUME_NONNULL_END
