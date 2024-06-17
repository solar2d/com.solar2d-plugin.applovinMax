//
//  SMABitWriter.h
//  SmaatoSDKCMP
//
//  Created by Stefan Meyer on 09.03.21.
//  Copyright © 2021 Smaato Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface SMABitWriter: NSObject

- (void)writeInt:(int)value withLength:(unsigned long)length;
- (void)writeLong:(NSUInteger)value withLength:(unsigned long)length;
- (void)writeString:(NSString *)value withLength:(unsigned long)length;
- (void)writeBool:(BOOL)value;
- (void)writeSet:(NSSet *)value withLength:(unsigned long)length;
- (void)writeVariableSet:(NSSet *)value;
- (void)writeRanges:(NSArray<NSArray *> *)allRanges;

- (NSString *)toBase64;

@end

@interface NSSet (rangeSupport)
@property (nonatomic, readonly) BOOL isRangeEncoding;
- (NSArray *)allRanges;
@end

@interface NSArray (rangeSupport)
@property (nonatomic, readonly) BOOL isRangeEncoding;
@end

NS_ASSUME_NONNULL_END
