//----------------------------------------------------------------------------
// ApplovinLibrary.mm
//
// Copyright (c) 2021 Solar2D All rights reserved.
//----------------------------------------------------------------------------

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "CoronaRuntime.h"
#import "CoronaAssert.h"
#import "CoronaEvent.h"
#import "CoronaLua.h"
#import "CoronaLuaIOS.h"
#import "CoronaLibrary.h"

#import "ApplovinLibrary.h"
#import <ApplovinSDK/ALAdSize.h>
#import <AppLovinSDK/AppLovinSDK.h>
#import <AppTrackingTransparency/ATTrackingManager.h>

// some macros to make life easier, and code more readable
#define UTF8StringWithFormat(format, ...) [[NSString stringWithFormat:format, ##__VA_ARGS__] UTF8String]
#define MsgFormat(format, ...) [NSString stringWithFormat:format, ##__VA_ARGS__]
#define UTF8IsEqual(utf8str1, utf8str2) (strcmp(utf8str1, utf8str2) == 0)

// ----------------------------------------------------------------------------
// Plugin Constants
// ----------------------------------------------------------------------------

#define PLUGIN_NAME        "plugin.applovinMax"
#define PLUGIN_VERSION     "1.0.0"
#define PLUGIN_SDK_VERSION [ALSdk version]

static const char EVENT_NAME[]    = "adsRequest";
static const char PROVIDER_NAME[] = "applovinMax";

// ad types
static const char TYPE_BANNER[]        = "banner";
static const char TYPE_INTERSTITIAL[]  = "interstitial";
static const char TYPE_REWARDEDVIDEO[] = "rewardedVideo";

// valid ad types
static const NSArray *validAdTypes = @[
  @(TYPE_BANNER),
  @(TYPE_INTERSTITIAL),
  @(TYPE_REWARDEDVIDEO)
];

// banner ad sizes
static const char BANNER_STANDARD[] = "standard";
static const char BANNER_LEADER[]   = "leader";
static const char BANNER_MREC[]     = "mrec";

// valid banner ad sizes
static const NSArray *validBannerSizes = @[
  @(BANNER_STANDARD),
  @(BANNER_LEADER),
  @(BANNER_MREC)
];

// banner alignment
static const char BANNER_ALIGN_TOP[]    = "top";
static const char BANNER_ALIGN_CENTER[] = "center";
static const char BANNER_ALIGN_BOTTOM[] = "bottom";

// valid banner positions
static const NSArray *validBannerPositions = @[
  @(BANNER_ALIGN_TOP),
  @(BANNER_ALIGN_CENTER),
  @(BANNER_ALIGN_BOTTOM)
];

// event phases
static NSString * const PHASE_INIT                      = @"init";
static NSString * const PHASE_DISPLAYED                 = @"displayed";
static NSString * const PHASE_EXPANDED                  = @"expanded";
static NSString * const PHASE_COLLAPSE                  = @"collapse";
static NSString * const PHASE_LOADED                    = @"loaded";
static NSString * const PHASE_FAILED                    = @"failed";
static NSString * const PHASE_HIDDEN                    = @"hidden";
static NSString * const PHASE_CLICKED                   = @"clicked";
static NSString * const PHASE_PLAYBACK_BEGAN            = @"playbackBegan";
static NSString * const PHASE_PLAYBACK_ENDED            = @"playbackEnded";
static NSString * const PHASE_RECEIVED_REWARD           = @"userReceivedReward";

// message constants
static NSString * const ERROR_MSG   = @"ERROR: ";
static NSString * const WARNING_MSG = @"WARNING: ";

// missing keys
static const char CORONA_EVENT_DATA_KEY[] = "data";

// saved objects (apiKey, ad state, etc)
static NSMutableDictionary *applovinObjects;

//
static int retryAttempt;
// ad dictionary keys
static NSString * const USER_SDK_KEY                      = @"userSdk";
static NSString * const USER_INTERSTITIAL_INSTANCE_KEY    = @"userInterstitial";
static NSString * const USER_REWARDEDVIDEO_INSTANCE_KEY   = @"userRewardedVideo";
static NSString * const USER_BANNER_INSTANCE_KEY          = @"userBanner";
static NSString * const Y_RATIO_KEY                       = @"yRatio";    // used to calculate Corona -> UIKit coordinate ratio

// ----------------------------------------------------------------------------
// plugin class and delegate definitions
// ----------------------------------------------------------------------------

@interface CoronaApplovinAdStatus: NSObject

@property (nonatomic, strong) MAAd *ad;
@property (nonatomic, assign) BOOL isLoaded;
@property (nonatomic, assign) BOOL usingCoronaKey;
@property (nonatomic, assign) BOOL bannerIsVisible;

- (instancetype)initWithCoronaKey:(BOOL)usingCoronaKey;

@end

// ----------------------------------------------------------------------------

@interface CoronaApplovinDelegate : UIViewController <MAAdViewAdDelegate, MARewardedAdDelegate, MAAdDelegate, MAAdRevenueDelegate>

@property (nonatomic, assign) CoronaLuaRef      coronaListener;
@property (nonatomic, weak)   id<CoronaRuntime> coronaRuntime;
@property (nonatomic, copy)   NSString          *adType;

- (instancetype)initWithAdType:(NSString *)adType;
- (void)dispatchLuaEvent:(NSDictionary *)dict;

@end

// ----------------------------------------------------------------------------

class ApplovinLibrary
{
  public:
    typedef ApplovinLibrary Self;
    
  public:
    static const char kName[];
    
  public:
    static int Open(lua_State *L);
    static int Finalizer(lua_State *L);
    static Self *ToLibrary(lua_State *L);
    
  protected:
    ApplovinLibrary();
    bool Initialize(CoronaLuaRef listener);
    
  public:
    static int init(lua_State *L);
    static int load(lua_State *L);
    static int show(lua_State *L);
    static int hide(lua_State *L);
    static int isLoaded(lua_State *L);
    static int setUserDetails(lua_State *L);
    static int setHasUserConsent(lua_State *L);
    static int setIsAgeRestrictedUser(lua_State *L);
    static int showDebugger(lua_State *L);
    
    
  private: // internal helper functions
    static void logMsg(lua_State *L, NSString *msgType,  NSString *errorMsg);
    static bool isSDKInitialized(lua_State *L);
    
  private:
    NSString *functionSignature;                                  // used in logMsg to identify function
    UIViewController *coronaViewController;
    UIWindow *coronaWindow;
};

// ----------------------------------------------------------------------------

const char ApplovinLibrary::kName[] = PLUGIN_NAME;
CoronaApplovinDelegate *applovinInterstitialDelegate = nil;      // need to have multiple instances since some delegates
CoronaApplovinDelegate *applovinRewardedDelegate = nil;          // cannot detect adtype
CoronaApplovinDelegate *applovinBannerDelegate = nil;            //

// ----------------------------------------------------------------------------
// helper functions
// ----------------------------------------------------------------------------

// log message to console
void
ApplovinLibrary::logMsg(lua_State *L, NSString* msgType, NSString* errorMsg)
{
  Self *context = ToLibrary(L);
  
  if (context) {
    Self& library = *context;
    
    NSString *functionID = [library.functionSignature copy];
    if (functionID.length > 0) {
      functionID = [functionID stringByAppendingString:@", "];
    }
    
    CoronaLuaLogPrefix(L, [msgType UTF8String], UTF8StringWithFormat(@"%@%@", functionID, errorMsg));
  }
}

// check if SDK calls can be made
bool
ApplovinLibrary::isSDKInitialized(lua_State *L)
{
  // has init() been called?
  if (applovinInterstitialDelegate.coronaListener == NULL) {
    logMsg(L, ERROR_MSG, @"applovin.init() must be called before calling other API methods");
    return false;
  }
  
  return true;
}

// ----------------------------------------------------------------------------
// plugin implementation
// ----------------------------------------------------------------------------

ApplovinLibrary::ApplovinLibrary()
:	coronaViewController(NULL)
{
}

bool
ApplovinLibrary::Initialize(void *platformContext)
{
  bool shouldInit = (applovinInterstitialDelegate == nil);
  
  if (shouldInit) {
    id<CoronaRuntime> runtime = (__bridge id<CoronaRuntime>)platformContext;
    coronaViewController = runtime.appViewController;
    coronaWindow = runtime.appWindow;
    
    // set up all delegates
    applovinInterstitialDelegate = [[CoronaApplovinDelegate alloc] initWithAdType:@(TYPE_INTERSTITIAL)];
    applovinInterstitialDelegate.coronaRuntime = runtime;
    applovinRewardedDelegate = [[CoronaApplovinDelegate alloc] initWithAdType:@(TYPE_REWARDEDVIDEO)];
    applovinRewardedDelegate.coronaRuntime = runtime;
    applovinBannerDelegate = [[CoronaApplovinDelegate alloc] initWithAdType:@(TYPE_BANNER)];
    applovinBannerDelegate.coronaRuntime = runtime;
    
    applovinObjects = [NSMutableDictionary new];
  }
  
  return shouldInit;
}

// Open the library
int
ApplovinLibrary::Open(lua_State *L)
{
  // Register __gc callback
  const char kMetatableName[] = __FILE__; // Globally unique string to prevent collision
  CoronaLuaInitializeGCMetatable(L, kMetatableName, Finalizer);
  
  void *platformContext = CoronaLuaGetContext(L);
  
  // Set library as upvalue for each library function
  Self *library = new Self;
  
  if (library->Initialize(platformContext))
  {
    // Functions in library
    static const luaL_Reg kFunctions[] =
    {
      {"init", init},
      {"load", load},
      {"isLoaded", isLoaded},
      {"show", show},
      {"hide", hide},
      {"setUserDetails", setUserDetails},
      {"setHasUserConsent", setHasUserConsent},
      {"setIsAgeRestrictedUser", setIsAgeRestrictedUser},
      {"showDebugger", showDebugger},
        
        
      {NULL, NULL}
    };
    
    // Register functions as closures, giving each access to the
    // 'library' instance via ToLibrary()
    {
      CoronaLuaPushUserdata(L, library, kMetatableName);
      luaL_openlib(L, kName, kFunctions, 1); // leave "library" on top of stack
    }
  }
  
  return 1;
}

int
ApplovinLibrary::Finalizer(lua_State *L)
{
  Self *library = (Self *)CoronaLuaToUserdata(L, 1);
  
  // clean up
    MAInterstitialAd *interstitial = nil;
  interstitial = applovinObjects[USER_INTERSTITIAL_INSTANCE_KEY];
  interstitial.delegate = nil;
  interstitial.revenueDelegate = nil;
  
  MARewardedAd *rewardedAd = nil;
  rewardedAd = applovinObjects[USER_REWARDEDVIDEO_INSTANCE_KEY];
  rewardedAd.delegate = nil;
  rewardedAd.revenueDelegate = nil;
  
  MAAdView *bannerAd = nil;
  bannerAd = applovinObjects[USER_BANNER_INSTANCE_KEY];
  bannerAd.delegate = nil;
  bannerAd.revenueDelegate = nil;
  
  CoronaLuaDeleteRef(L, applovinInterstitialDelegate.coronaListener);
  CoronaLuaDeleteRef(L, applovinRewardedDelegate.coronaListener);
  CoronaLuaDeleteRef(L, applovinBannerDelegate.coronaListener);
  applovinInterstitialDelegate = nil;
  applovinRewardedDelegate = nil;
  applovinBannerDelegate = nil;
  
  // clear the saved ad objects
  [applovinObjects removeAllObjects];
  
  delete library;
  
  return 0;
}

ApplovinLibrary *
ApplovinLibrary::ToLibrary(lua_State *L)
{
  // library is pushed as part of the closure
  Self *library = (Self *)CoronaLuaToUserdata(L, lua_upvalueindex(1));
  return library;
}

static void initApplovin() {
	
	
	// log the plugin version to device console
	NSLog(@"%s: %s (SDK: %@)", PLUGIN_NAME, PLUGIN_VERSION, PLUGIN_SDK_VERSION);
	
	[[ALSdk shared] initializeSdkWithCompletionHandler:^(ALSdkConfiguration * _Nonnull configuration) {
		NSDictionary *coronaEvent = @{
			@(CoronaEventPhaseKey()) : PHASE_INIT
		};
		[applovinInterstitialDelegate dispatchLuaEvent:coronaEvent];
	}];
}

// [Lua] applovinMax.init(listener, options)
int
ApplovinLibrary::init(lua_State *L)
{
  Self *context = ToLibrary(L);
  
  if (! context) { // abort if no valid context
    return 0;
  }
  
  Self& library = *context;
  
  library.functionSignature = @"applovin.init(listener, options)";
  
  // prevent init from being called twice
  if (applovinInterstitialDelegate.coronaListener != NULL) {
    logMsg(L, WARNING_MSG, @"init() should only be called once");
    return 0;
  }
  
  // check number of arguments
  int nargs = lua_gettop(L);
  if (nargs != 2) {
    logMsg(L, ERROR_MSG, MsgFormat(@"Expected 2 arguments, got %d", nargs));
    return 0;
  }
  
  NSString* privacyPolicy = nil;
    NSString* mediationProvider = @"max";
  bool verboseLogging = false;
//  bool testMode = false;
  bool startMuted = false;
  
  // get listener
  if (CoronaLuaIsListener(L, 1, PROVIDER_NAME)) {
    applovinInterstitialDelegate.coronaListener = CoronaLuaNewRef(L, 1);
    applovinRewardedDelegate.coronaListener = CoronaLuaNewRef(L, 1);
    applovinBannerDelegate.coronaListener = CoronaLuaNewRef(L, 1);
  }
  else {
    logMsg(L, ERROR_MSG, MsgFormat(@"listener expected, got: %s", luaL_typename(L, 1)));
    return 0;
  }
  
  // get options table
  if (lua_type(L, 2) == LUA_TTABLE) {
    for (lua_pushnil(L); lua_next(L, 2) != 0; lua_pop(L, 1)) {
      if (lua_type(L, -2) != LUA_TSTRING) {
        logMsg(L, ERROR_MSG, @"options must be a key/value table");
        return 0;
      }
      
      const char *key = lua_tostring(L, -2);
      
      
      if (UTF8IsEqual(key, "privacyPolicy")) {
        if (lua_type(L, -1) == LUA_TSTRING) {
          privacyPolicy = [NSString stringWithUTF8String:lua_tostring(L, -1)];
        }
        else {
          logMsg(L, ERROR_MSG, MsgFormat(@"options.privacyPolicy (string) expected, got: %s", luaL_typename(L, -1)));
          return 0;
        }
      }
      else if (UTF8IsEqual(key, "verboseLogging")) {
        if (lua_type(L, -1) == LUA_TBOOLEAN) {
          verboseLogging = lua_toboolean(L, -1);
        }
        else {
          logMsg(L, ERROR_MSG, MsgFormat(@"options.verboseLogging (boolean) expected, got: %s", luaL_typename(L, -1)));
          return 0;
        }
      }
      else if (UTF8IsEqual(key, "testMode")) {
        if (lua_type(L, -1) == LUA_TBOOLEAN) {
			logMsg(L, WARNING_MSG, @"options.testMode (boolean) does not have effect anymore");
//          testMode = lua_toboolean(L, -1);
        }
        else {
          logMsg(L, ERROR_MSG, MsgFormat(@"options.testMode (boolean) expected, got: %s", luaL_typename(L, -1)));
          return 0;
        }
      } else if (UTF8IsEqual(key, "mediationProvider")) {
          if (lua_type(L, -1) == LUA_TSTRING) {
              mediationProvider = [NSString stringWithUTF8String:lua_tostring(L, -1)];
          }
          else {
            logMsg(L, ERROR_MSG, MsgFormat(@"options.mediationProvider (string) expected, got: %s", luaL_typename(L, -1)));
            return 0;
          }
        }
      else {
        logMsg(L, ERROR_MSG, MsgFormat(@"Invalid option '%s'", key));
        return 0;
      }
    }
  }
  else {
    logMsg(L, ERROR_MSG, MsgFormat(@"options (table) expected, got %s", luaL_typename(L, 2)));
    return 0;
  }
  
  
  // create Applovin SDK settings
//  settings.autoPreloadAdSizes = @"NONE";
//  settings.autoPreloadAdTypes = @"NONE";
    [ALSdk shared].settings.isVerboseLogging = verboseLogging;
    [ALSdk shared].settings.muted = startMuted;
    
    [ALSdk shared].mediationProvider = mediationProvider;
//  settings.isTestAdsEnabled = testMode;
  if([privacyPolicy length]) {
      [ALSdk shared].settings.consentFlowSettings.enabled = YES;
      [ALSdk shared].settings.consentFlowSettings.privacyPolicyURL = [NSURL URLWithString:privacyPolicy];
  }
  	if([[NSBundle mainBundle] objectForInfoDictionaryKey:@"NSUserTrackingUsageDescription"]) {
		if (@available(iOS 14, *)) {
			[ATTrackingManager requestTrackingAuthorizationWithCompletionHandler:^(ATTrackingManagerAuthorizationStatus status) {
				  initApplovin();
			}];
		} else {
			  initApplovin();
		}
	} else {
		  initApplovin();
	}

  
  return 0;
}

// [Lua] applovinMax.load( adType, options )
int
ApplovinLibrary::load(lua_State *L)
{
  Self *context = ToLibrary(L);
  
  if (! context) { // abort if no valid context
    return 0;
  }
  
  Self& library = *context;
  
  library.functionSignature = @"applovinMax.load( adType, options )";
  
  if (! isSDKInitialized(L)) {
    return 0;
  }
  
  // check number of arguments
  // for backwards compatibility we need to accept 0 args
  int nargs = lua_gettop(L);
  if (nargs > 3) {
    logMsg(L, ERROR_MSG, MsgFormat(@"Expected 1 or 2 arguments, got %d", nargs));
    return 0;
  }
  
  bool rewarded = false;
  bool legacyAPI = true;
    NSString * unitId = NULL;
  const char *adType = NULL;
  const char *bannerSize = NULL;
  
  // check args
  if (! lua_isnoneornil(L, 1)) {
    if (lua_type(L, 1) == LUA_TBOOLEAN) {
      rewarded = lua_toboolean(L, 1);
    }
    else if (lua_type(L, 1) == LUA_TSTRING) {
      legacyAPI = false;
      adType = lua_tostring(L, 1);
    }
    else {
      logMsg(L, ERROR_MSG, MsgFormat(@"adType (string) expected, got: %s", luaL_typename(L, 1)));
      return 0;
    }
  }
    lua_getfield(L, 2, "iOSUnitId");
    if (lua_isnil(L, 1)) {
        logMsg(L, ERROR_MSG, @"iOSUnitId expected, ads won't load without it");
    }
    lua_pop(L, 1);
  // get options table
  if (! lua_isnoneornil(L, 2)) {
    if (lua_type(L, 2) == LUA_TTABLE) {
      for (lua_pushnil(L); lua_next(L, 2) != 0; lua_pop(L, 1)) {
        if (lua_type(L, -2) != LUA_TSTRING) {
          logMsg(L, ERROR_MSG, @"options must be a key/value table");
          return 0;
        }
        
        const char *key = lua_tostring(L, -2);
        
        if (UTF8IsEqual(key, "bannerSize")) {
          if (lua_type(L, -1) == LUA_TSTRING) {
            bannerSize = lua_tostring(L, -1);
          }
          else {
            logMsg(L, ERROR_MSG, MsgFormat(@"options.bannerSize (string) expected, got: %s", luaL_typename(L, -1)));
            return 0;
          }
        }else if (UTF8IsEqual(key, "iOSUnitId")){
            if (lua_type(L, -1) == LUA_TSTRING) {
                unitId = [NSString stringWithUTF8String:lua_tostring(L, -1)];
            }
            else {
              logMsg(L, ERROR_MSG, MsgFormat(@"options.iOSUnitId (string) expected, got: %s", luaL_typename(L, -1)));
              return 0;
            }
        }
        else if (UTF8IsEqual(key, "androidUnitId")){
            //padding to prevent error
        }
        else {
          logMsg(L, ERROR_MSG, MsgFormat(@"Invalid option '%s'", key));
          return 0;
        }
      }
    }
    else {
      logMsg(L, ERROR_MSG, MsgFormat(@"options (table) expected, got %s", luaL_typename(L, 2)));
      return 0;
    }
  }
  
  // validate
  if (! legacyAPI) {
    if (! [validAdTypes containsObject:@(adType)]) {
      logMsg(L, ERROR_MSG, MsgFormat(@"Invalid adType '%s'", adType));
      return 0;
    }
    
    if (UTF8IsEqual(adType, TYPE_REWARDEDVIDEO)) {
      rewarded = true;
    }
    else {
      rewarded = false;
    }

    // check banner size
    if (bannerSize != NULL) {
      if (! [validBannerSizes containsObject:@(bannerSize)]) {
        logMsg(L, ERROR_MSG, MsgFormat(@"Invalid banner size '%s'", bannerSize));
        return 0;
      }
    }
  }
  else {
    if (rewarded) {
      adType = TYPE_REWARDEDVIDEO;
    }
    else {
      adType = TYPE_INTERSTITIAL;
    }
  }
    if(unitId == NULL){
        logMsg(L, ERROR_MSG, MsgFormat(@"iOSUnitId is param is required in options table but found nil"));
        return 0;
    }
  
  // should we use our ids?
  NSUserDefaults *sharedPref = [NSUserDefaults standardUserDefaults];
  
  long currentAdCount = [sharedPref integerForKey:@(adType)];
  
  int savedRatio = 20; // current ratio 5% (1/20)
  const bool useCoronaKey = (currentAdCount % savedRatio == 0);
  

  
  if (rewarded) {
    MARewardedAd *rewardedAd = applovinObjects[USER_REWARDEDVIDEO_INSTANCE_KEY];
    
    // initialize rewarded object
    if (rewardedAd == nil) {
        rewardedAd =[MARewardedAd sharedWithAdUnitIdentifier: unitId];
      rewardedAd.delegate = applovinRewardedDelegate;
      rewardedAd.revenueDelegate = applovinRewardedDelegate;
      applovinObjects[USER_REWARDEDVIDEO_INSTANCE_KEY] = rewardedAd;
    }
    
    // save extra ad status information not available in ad object
    CoronaApplovinAdStatus *adStatus = [[CoronaApplovinAdStatus alloc] initWithCoronaKey:useCoronaKey];
    applovinObjects[@(TYPE_REWARDEDVIDEO)] = adStatus;
    
    [rewardedAd loadAd];
  }
  else { // interstitial or banner ad
    if (UTF8IsEqual(adType, TYPE_BANNER)) {
      // calculate the Corona->device coordinate ratio.
      // we don't use display.contentScaleY here as there are cases where it's difficult to get the proper values to use
      // especially on Android. uses the same formula for iOS and Android for the sake of consistency.
      // re-calculate this value on every load as the ratio can change between orientation changes
      CGPoint point1 = {0, 0};
      CGPoint point2 = {1000, 1000};
      CGPoint uikitPoint1 = [applovinBannerDelegate.coronaRuntime coronaPointToUIKitPoint: point1];
      CGPoint uikitPoint2 = [applovinBannerDelegate.coronaRuntime coronaPointToUIKitPoint: point2];
      CGFloat yRatio = (uikitPoint2.y - uikitPoint1.y) / 1000.0;
      applovinObjects[Y_RATIO_KEY] = @(yRatio);
    
        MAAdView *bannerAd = applovinObjects[USER_BANNER_INSTANCE_KEY];
      
      // remove old banner
      if (bannerAd != nil) {
        [bannerAd removeFromSuperview];
      }
      
      CGRect bannerRect;
        MAAdFormat * bannerFormat =MAAdFormat.banner;
      if ((bannerSize == NULL) || (UTF8IsEqual(bannerSize, BANNER_STANDARD))) {
        bannerRect = CGRectMake(0, 0, 320.0f, 50.0f);
          bannerFormat =MAAdFormat.banner;
      }
      else if (UTF8IsEqual(bannerSize, BANNER_LEADER)) {
        bannerRect = CGRectMake(0, 0, 728.0f, 90.0f);
          bannerFormat =MAAdFormat.leader;
      }
      else if (UTF8IsEqual(bannerSize, BANNER_MREC)) {
        bannerRect = CGRectMake(0, 0, 320.0f, 250.0f);
          bannerFormat =MAAdFormat.mrec;
      }
      bannerAd = [[MAAdView alloc] initWithAdUnitIdentifier:unitId  adFormat:bannerFormat];
        bannerAd.frame = bannerRect;
      bannerAd.delegate = applovinBannerDelegate;
      bannerAd.revenueDelegate = applovinBannerDelegate;
      applovinObjects[USER_BANNER_INSTANCE_KEY] = bannerAd;
      
      // save extra ad status information not available in ad object
      CoronaApplovinAdStatus *adStatus = [[CoronaApplovinAdStatus alloc] initWithCoronaKey:useCoronaKey];
      applovinObjects[@(TYPE_BANNER)] = adStatus;

      [bannerAd loadAd];
    }
    else { // interstitial
        MAInterstitialAd *interstitialAd = [[MAInterstitialAd alloc] initWithAdUnitIdentifier:unitId ];
        interstitialAd.delegate = applovinInterstitialDelegate;
        interstitialAd.revenueDelegate = applovinInterstitialDelegate;
        applovinObjects[USER_INTERSTITIAL_INSTANCE_KEY] = interstitialAd;
      
      // save extra ad status information not available in ad object
      CoronaApplovinAdStatus *adStatus = [[CoronaApplovinAdStatus alloc] initWithCoronaKey:useCoronaKey];
      applovinObjects[@(TYPE_INTERSTITIAL)] = adStatus;
      [interstitialAd loadAd];
    }
  }
  
  return 0;
}

// [Lua] applovinMax.isLoaded( adType )
int
ApplovinLibrary::isLoaded(lua_State *L)
{
  Self *context = ToLibrary(L);
  
  if (! context) { // abort if no valid context
    return 0;
  }
  
  Self& library = *context;
  
  library.functionSignature = @"applovin.isLoaded( adType )";
  
  if (! isSDKInitialized(L)) {
    return 0;
  }
  
  // check number of arguments
  // for backwards compatibility we need to accept 0 args
  int nargs = lua_gettop(L);
  if (nargs > 1) {
    logMsg(L, ERROR_MSG, MsgFormat(@"Expected 1 argument, got %d", nargs));
    return 0;
  }
  
  bool rewarded = false;
  bool legacyAPI = true;
  const char *adType = NULL;
    bool isAdLoaded = false;
  // check options
  if (! lua_isnoneornil(L, 1)) {
    if (lua_type(L, 1) == LUA_TBOOLEAN) {
      rewarded = lua_toboolean(L, 1);
    }
    else if (lua_type(L, 1) == LUA_TSTRING) {
      legacyAPI = false;
      adType = lua_tostring(L, 1);
    }
    else {
      logMsg(L, ERROR_MSG, MsgFormat(@"adType (string) expected, got: %s", luaL_typename(L, 1)));
      return 0;
    }
  }
  
  // validate
  if (! legacyAPI) {
    if (! [validAdTypes containsObject:@(adType)]) {
      logMsg(L, ERROR_MSG, MsgFormat(@"Invalid adType '%s'", adType));
      return 0;
    }
    
    if (UTF8IsEqual(adType, TYPE_REWARDEDVIDEO)) {
        MARewardedAd* rewardVideo = applovinObjects[@(TYPE_REWARDEDVIDEO)];
        if(rewardVideo){
            isAdLoaded = [rewardVideo isReady];
        }
    }
    else {
      rewarded = false;
    }
  }
    if (adType == TYPE_REWARDEDVIDEO) {
        MARewardedAd* rewardVideo = applovinObjects[@(TYPE_REWARDEDVIDEO)];
        if(rewardVideo){
            isAdLoaded = [rewardVideo isReady];
        }
        
    }
    else if(adType == TYPE_INTERSTITIAL) {
        MAInterstitialAd * interstital = applovinObjects[@(TYPE_INTERSTITIAL)];
        if(interstital){
            isAdLoaded = [interstital isReady];
        }
    }else {
        CoronaApplovinAdStatus *adStatus = applovinObjects[@(adType)];
        isAdLoaded = (adStatus != nil) && (adStatus.ad != nil) && (adStatus.isLoaded || adStatus.bannerIsVisible);
    }
  
    
  
  lua_pushboolean(L, isAdLoaded);
  
  return 1;
}

// [Lua] applovin.hide( adType )
int
ApplovinLibrary::hide(lua_State *L)
{
  Self *context = ToLibrary(L);
  
  if (! context) { // abort if no valid context
    return 0;
  }
  
  Self& library = *context;
  
  library.functionSignature = @"applovin.hide( adType )";
  
  if (! isSDKInitialized(L)) {
    return 0;
  }
  
  // check number of arguments
  int nargs = lua_gettop(L);
  if (nargs != 1) {
    logMsg(L, ERROR_MSG, MsgFormat(@"Expected 1 argument, got %d", nargs));
    return 0;
  }
  
  const char *adType = NULL;
  
  // check options
  if (lua_type(L, 1) == LUA_TSTRING) {
    adType = lua_tostring(L, 1);
  }
  else {
    logMsg(L, ERROR_MSG, MsgFormat(@"adType (string) expected, got: %s", luaL_typename(L, 1)));
    return 0;
  }
  
  // validate
  if (! UTF8IsEqual(adType, TYPE_BANNER)) {
    logMsg(L, ERROR_MSG, MsgFormat(@"Invalid adType '%s'. Only banners can be hidden", adType));
    return 0;
  }
  
  CoronaApplovinAdStatus *adStatus = applovinObjects[@(TYPE_BANNER)];
  if ((adStatus == nil) || (adStatus.ad == nil) || (! adStatus.bannerIsVisible && ! adStatus.isLoaded)) {
    logMsg(L, ERROR_MSG, @"Banner not loaded");
    return 0;
  }
  
    MAAdView *bannerAd = applovinObjects[USER_BANNER_INSTANCE_KEY];

  // send hidden event
    NSDictionary *coronaEvent = @{
      @(CoronaEventPhaseKey()): PHASE_HIDDEN,
      @(CoronaEventTypeKey()): applovinBannerDelegate.adType
    };
    [applovinBannerDelegate dispatchLuaEvent:coronaEvent];

  [bannerAd removeFromSuperview];
  [applovinObjects removeObjectForKey:@(TYPE_BANNER)];
  [applovinObjects removeObjectForKey:USER_BANNER_INSTANCE_KEY];
  
  return 0;
}

// [Lua] applovinMax.show( adType [, options] )
int
ApplovinLibrary::show(lua_State *L)
{
  Self *context = ToLibrary(L);
  
  if (! context) { // abort if no valid context
    return 0;
  }
  
  Self& library = *context;
  
  library.functionSignature = @"applovin.show( adType [, options] )";
  
  if (! isSDKInitialized(L)) {
    return 0;
  }
  
  // check number of arguments
  // for backwards compatibility we need to accept 0 args
  int nargs = lua_gettop(L);
  if (nargs > 2) {
    logMsg(L, ERROR_MSG, MsgFormat(@"Expected 1 or 2 arguments, got %d", nargs));
    return 0;
  }
  
  bool rewarded = false;
  bool legacyAPI = true;
  const char *placement = NULL;
  const char *adType = NULL;
  const char *yAlign = NULL;
  double yOffset = 0;
  
  // check ad type
  if (! lua_isnoneornil(L, 1)) {
    if (lua_type(L, 1) == LUA_TBOOLEAN) {
      rewarded = lua_toboolean(L, 1);
    }
    else if (lua_type(L, 1) == LUA_TSTRING) {
      legacyAPI = false;
      adType = lua_tostring(L, 1);
    }
    else {
      logMsg(L, ERROR_MSG, MsgFormat(@"adType (string) expected, got: %s", luaL_typename(L, 1)));
      return 0;
    }
  }
  
  // check options
  if (! lua_isnoneornil(L, 2)) {
    if (lua_type(L, 2) == LUA_TSTRING) {
      placement = lua_tostring(L, 2);
    }
    else if (lua_type(L, 2) == LUA_TTABLE) {
      for (lua_pushnil(L); lua_next(L, 2) != 0; lua_pop(L, 1)) {
        if (lua_type(L, -2) != LUA_TSTRING) {
          logMsg(L, ERROR_MSG, @"options must be a key/value table");
          return 0;
        }
        
        const char *key = lua_tostring(L, -2);
        
        if (UTF8IsEqual(key, "placement")) {
          if (lua_type(L, -1) == LUA_TSTRING) {
            placement = lua_tostring(L, -1);
          }
          else {
            logMsg(L, ERROR_MSG, MsgFormat(@"options.placement (string) expected, got: %s", luaL_typename(L, -1)));
            return 0;
          }
        }
        else if (UTF8IsEqual(key, "y")) {
          if (lua_type(L, -1) == LUA_TSTRING) {
            yAlign = lua_tostring(L, -1);
          }
          else if (lua_type(L, -1) == LUA_TNUMBER) {
            yOffset = lua_tonumber(L, -1);
          }
          else {
            logMsg(L, ERROR_MSG, MsgFormat(@"options.y (string or number) expected, got: %s", luaL_typename(L, -1)));
            return 0;
          }
        }
        else {
          logMsg(L, ERROR_MSG, MsgFormat(@"Invalid option '%s'", key));
          return 0;
        }
      }
    }
    else {
      logMsg(L, ERROR_MSG, MsgFormat(@"options (table) expected, got: %s", luaL_typename(L, 2)));
      return 0;
    }
  }
  
  // validate
  if (! legacyAPI) {
    if (! [validAdTypes containsObject:@(adType)]) {
      logMsg(L, ERROR_MSG, MsgFormat(@"Invalid adType '%s'", adType));
      return 0;
    }
    
    if (UTF8IsEqual(adType, TYPE_REWARDEDVIDEO)) {
      rewarded = true;
    }
    else {
      rewarded = false;
    }

    if (yAlign != NULL) {
      if (! [validBannerPositions containsObject:@(yAlign)]) {
        logMsg(L, ERROR_MSG, MsgFormat(@"y '%s' invalid", yAlign));
        return 0;
      }
    }
  }
  
  if (rewarded) {
    CoronaApplovinAdStatus *adStatus = applovinObjects[@(TYPE_REWARDEDVIDEO)];
    if ((adStatus == nil) || (adStatus.ad == nil) || ! adStatus.isLoaded) {
      logMsg(L, ERROR_MSG, @"Rewarded video not loaded");
      return 0;
    }
    
      MARewardedAd *rewardedAd = applovinObjects[USER_REWARDEDVIDEO_INSTANCE_KEY];
      rewardedAd.delegate =applovinRewardedDelegate;
      [rewardedAd showAd];
      
  }
  else {
    if (UTF8IsEqual(adType, TYPE_BANNER)) {
      CoronaApplovinAdStatus *adStatus = applovinObjects[@(TYPE_BANNER)];
      if ((adStatus == nil) || (adStatus.ad == nil) || (! adStatus.bannerIsVisible && ! adStatus.isLoaded)) {
        logMsg(L, ERROR_MSG, @"Banner not loaded");
        return 0;
      }
      else if (adStatus.bannerIsVisible) {
        logMsg(L, ERROR_MSG, @"Banner already visable");
        return 0;
      }

        MAAdView *bannerAd = applovinObjects[USER_BANNER_INSTANCE_KEY];
        
      // get screen size
      CGFloat orientedWidth = library.coronaViewController.view.frame.size.width;
      CGFloat orientedHeight = library.coronaViewController.view.frame.size.height;
      
      // calculate the size for the ad, and set its frame
      CGSize bannerSize = bannerAd.bounds.size;
      
      CGFloat bannerCenterX = ((orientedWidth - bannerSize.width) / 2);
      CGFloat bannerCenterY = ((orientedHeight - bannerSize.height) / 2);
      CGFloat bannerTopY = 0;
      CGFloat bannerBottomY = (orientedHeight - bannerSize.height);
        //Add safe area just in case
        if (@available(iOS 11.0, *)) {
            bannerTopY = library.coronaViewController.view.safeAreaInsets.top;
            bannerBottomY = (orientedHeight - bannerSize.height- library.coronaViewController.view.safeAreaInsets.bottom);
        }
      CGRect bannerFrame = bannerAd.frame;
      bannerFrame.origin.x = bannerCenterX;
      
      // set the banner position
      if (yAlign == NULL) {
        // convert corona coordinates to device coordinates and set banner position
        CGFloat newBannerY = floor(yOffset * [applovinObjects[Y_RATIO_KEY] floatValue]);
        
        // negative values count from bottom
        if (yOffset < 0) {
          newBannerY = bannerBottomY + newBannerY;
        }
        
        // make sure the banner frame is visible.
        // adjust it if the user has specified 'y' which will render it partially off-screen
        NSUInteger ySnap = 0;
        if (newBannerY + bannerFrame.size.height > orientedHeight) {
          logMsg(L, WARNING_MSG, @"Banner y position off screen. Adjusting position.");
          ySnap = newBannerY - orientedHeight + bannerFrame.size.height;
        }
        bannerFrame.origin.y = newBannerY - ySnap;
      }
      else {
        if (UTF8IsEqual(yAlign, BANNER_ALIGN_TOP)) {
          bannerFrame.origin.y = bannerTopY;
        }
        else if (UTF8IsEqual(yAlign, BANNER_ALIGN_CENTER)) {
          bannerFrame.origin.y = bannerCenterY;
        }
        else if (UTF8IsEqual(yAlign, BANNER_ALIGN_BOTTOM)) {
          bannerFrame.origin.y = bannerBottomY;
        }
      }
        bannerAd.frame = bannerFrame;
        
        [library.coronaViewController.view addSubview:bannerAd];
        [bannerAd startAutoRefresh];
        [bannerAd setHidden:NO];
      adStatus.bannerIsVisible = YES;
      
      // the SDK automatically sends a 'displayed' event after load which doesn't follow Corona standards.
      // therefore we must manually send a 'displayed' event since the SDK was prevented to do so.
        NSDictionary *coronaEvent = @{
          @(CoronaEventPhaseKey()): PHASE_DISPLAYED,
          @(CoronaEventTypeKey()): applovinBannerDelegate.adType
        };
        [applovinBannerDelegate dispatchLuaEvent:coronaEvent];
    }
    else { // interstitial
      CoronaApplovinAdStatus *adStatus = applovinObjects[@(TYPE_INTERSTITIAL)];
      if ((adStatus == nil) || (adStatus.ad == nil) || ! adStatus.isLoaded) {
        logMsg(L, ERROR_MSG, @"Interstitial not loaded");
        return 0;
      }
      
        MAInterstitialAd *interstitialAd = applovinObjects[USER_INTERSTITIAL_INSTANCE_KEY];
      
      if (placement != NULL) {
          
          [interstitialAd showAdForPlacement:[NSString stringWithUTF8String:placement]];
      }
      else {
        [interstitialAd showAd];
      }
    }
  }
  
  return 0;
}

// [Lua] applovin.setUserDetails( options )
int
ApplovinLibrary::setUserDetails(lua_State *L)
{
  Self *context = ToLibrary(L);
  
  if (! context) { // abort if no valid context
    return 0;
  }
  
  Self& library = *context;
  
  library.functionSignature = @"applovin.setUserDetails( options )";
  
  if (! isSDKInitialized(L)) {
    return 0;
  }
  
  // check number of arguments
  int nargs = lua_gettop(L);
  if (nargs != 1) {
    logMsg(L, ERROR_MSG, MsgFormat(@"Expected 1 argument, got %d", nargs));
    return 0;
  }
  
  const char *userId = NULL;
  
  // check options
  if (lua_type(L, 1) == LUA_TTABLE) {
    for (lua_pushnil(L); lua_next(L, 1) != 0; lua_pop(L, 1)) {
      if (lua_type(L, -2) != LUA_TSTRING) {
        logMsg(L, ERROR_MSG, @"options must be a key/value table");
        return 0;
      }
      
      const char *key = lua_tostring(L, -2);
      
      if (UTF8IsEqual(key, "userId")) {
        if (lua_type(L, -1) == LUA_TSTRING) {
          userId = lua_tostring(L, -1);
        }
        else {
          logMsg(L, ERROR_MSG, MsgFormat(@"options.userId (string) expected, got: %s", luaL_typename(L, -1)));
          return 0;
        }
      }
      else {
        logMsg(L, ERROR_MSG, MsgFormat(@"Invalid option '%s'", key));
        return 0;
      }
    }
  }
  else {
    logMsg(L, ERROR_MSG, MsgFormat(@"options (table) expected, got %s", luaL_typename(L, 1)));
    return 0;
  }
    if(userId){
        [ALSdk shared].userIdentifier = @(userId);
    }
    
  
  return 0;
}

// [Lua] applovin.setHasUserConsent( bool )
int
ApplovinLibrary::setHasUserConsent(lua_State *L)
{
    Self *context = ToLibrary(L);

    if (! context) { // abort if no valid context
        return 0;
    }

    Self& library = *context;

    library.functionSignature = @"applovin.setHasUserConsent( bool )";

    if (! isSDKInitialized(L)) {
        return 0;
    }

    // check number of arguments
    int nargs = lua_gettop(L);
    if (nargs != 1) {
        logMsg(L, ERROR_MSG, MsgFormat(@"Expected 1 argument, got %d", nargs));
        return 0;
    }

    int hasUserConsent = NULL;

    // check options
    if (lua_type(L, 1) == LUA_TBOOLEAN) {
        hasUserConsent = lua_toboolean(L, -1);
    }
    else {
        logMsg(L, ERROR_MSG, MsgFormat(@"hasUserConsent (bool) expected, got %s", luaL_typename(L, 1)));
        return 0;
    }

    [ALPrivacySettings setHasUserConsent:hasUserConsent!=0];

    return 0;
}

// [Lua] applovin.setIsAgeRestrictedUser( bool )
int
ApplovinLibrary::setIsAgeRestrictedUser(lua_State *L)
{
    Self *context = ToLibrary(L);

    if (! context) { // abort if no valid context
        return 0;
    }

    Self& library = *context;

    library.functionSignature = @"applovin.setIsAgeRestrictedUser( bool )";

    if (! isSDKInitialized(L)) {
        return 0;
    }

    // check number of arguments
    int nargs = lua_gettop(L);
    if (nargs != 1) {
        logMsg(L, ERROR_MSG, MsgFormat(@"Expected 1 argument, got %d", nargs));
        return 0;
    }

    int isAgeRestrictedUser = NULL;

    // check options
    if (lua_type(L, 1) == LUA_TBOOLEAN) {
        isAgeRestrictedUser = lua_toboolean(L, -1);
    }
    else {
        logMsg(L, ERROR_MSG, MsgFormat(@"isAgeRestrictedUser (bool) expected, got %s", luaL_typename(L, 1)));
        return 0;
    }

    [ALPrivacySettings setIsAgeRestrictedUser:isAgeRestrictedUser];

    return 0;
}

// [Lua] applovin.showDebugger( bool )
int
ApplovinLibrary::showDebugger(lua_State *L)
{
    Self *context = ToLibrary(L);

    if (! context) { // abort if no valid context
        return 0;
    }

    Self& library = *context;

    library.functionSignature = @"applovin.showDebugger()";

    if (! isSDKInitialized(L)) {
        return 0;
    }
    [[ALSdk shared] showMediationDebugger];

    

    return 0;
}

// ----------------------------------------------------------------------------
// delegate implementation
// ----------------------------------------------------------------------------

@implementation CoronaApplovinDelegate

- (instancetype)init {
  return [self initWithAdType:nil];
}

- (instancetype)initWithAdType:(NSString *)adType
{
  if (self = [super init]) {
    self.adType = adType;
    self.coronaListener = NULL;
    self.coronaRuntime = NULL;
  }
  
  return self;
}

// dispatch a new Lua event
- (void)dispatchLuaEvent:(NSDictionary *)dict
{
  [[NSOperationQueue mainQueue] addOperationWithBlock:^{
    lua_State *L = self.coronaRuntime.L;
    CoronaLuaRef coronaListener = self.coronaListener;
    bool hasErrorKey = false;
    
    // create new event
    CoronaLuaNewEvent(L, EVENT_NAME);
    
    for (NSString *key in dict) {
      CoronaLuaPushValue(L, [dict valueForKey:key]);
      lua_setfield(L, -2, key.UTF8String);
      
      if (! hasErrorKey) {
        hasErrorKey = [key isEqualToString:@(CoronaEventIsErrorKey())];
      }
    }
    
    // add error key if not in dict
    if (! hasErrorKey) {
      lua_pushboolean(L, false);
      lua_setfield(L, -2, CoronaEventIsErrorKey());
    }
    
    // add provider
    lua_pushstring(L, PROVIDER_NAME );
    lua_setfield(L, -2, CoronaEventProviderKey());
    
    CoronaLuaDispatchEvent(L, coronaListener, 0);
  }];
}



#pragma mark - MAAdDelegate Protocol

- (void)didLoadAd:(MAAd *)ad {
    CoronaApplovinAdStatus *adStatus = applovinObjects[self.adType];
    [adStatus setAd:ad];
    [adStatus setIsLoaded:YES];
    
    // send Corona Lua event
    NSDictionary *coronaEvent = @{
      @(CoronaEventPhaseKey()): PHASE_LOADED,
      @(CoronaEventTypeKey()): self.adType
    };
    [self dispatchLuaEvent:coronaEvent];
    
    // increment saved ad count
    NSUserDefaults *sharedPref = [NSUserDefaults standardUserDefaults];
    long currentAdCount = [sharedPref integerForKey:self.adType];
    [sharedPref setInteger:++currentAdCount forKey:self.adType];
    retryAttempt = 0;
}

- (void)didFailToLoadAdForAdUnitIdentifier:(NSString *)adUnitIdentifier withError:(MAError *)error {
    // send Corona Lua event
    NSDictionary *coronaEvent = @{
      @(CoronaEventPhaseKey()): PHASE_FAILED,
      @(CoronaEventIsErrorKey()): @(true),
      @(CoronaEventTypeKey()): self.adType,
      @(CoronaEventResponseKey()): error.message
    };
    [self dispatchLuaEvent:coronaEvent];
    retryAttempt++;
    NSInteger delaySec = pow(2, MIN(6, retryAttempt));
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, delaySec * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
        [applovinObjects[USER_REWARDEDVIDEO_INSTANCE_KEY] loadAd];
    });
}

- (void)didClickAd:(MAAd *)ad {
    // send Corona Lua event
    NSDictionary *coronaEvent = @{
      @(CoronaEventPhaseKey()): PHASE_CLICKED,
      @(CoronaEventTypeKey()): self.adType
    };
    [self dispatchLuaEvent:coronaEvent];
}

- (void)didFailToDisplayAd:(MAAd *)ad withError:(MAError *)error {
    // send Corona Lua event
    NSDictionary *coronaEvent = @{
      @(CoronaEventPhaseKey()): PHASE_FAILED,
      @(CoronaEventIsErrorKey()): @(true),
      @(CoronaEventTypeKey()): self.adType,
      @(CoronaEventResponseKey()): error.message
    };
    [self dispatchLuaEvent:coronaEvent];
    // Rewarded ad failed to display. We recommend loading the next ad
    if ([ad format] == MAAdFormat.rewarded) {
        [applovinObjects[USER_REWARDEDVIDEO_INSTANCE_KEY] loadAd];
    }
    else if ([ad format] == MAAdFormat.interstitial) {
        // Interstitial ad failed to display. We recommend loading the next ad
        [applovinObjects[USER_INTERSTITIAL_INSTANCE_KEY] loadAd];
    }

    
}


#pragma mark - MAAdViewAdDelegate Protocol

- (void)didExpandAd:(MAAd *)ad {
    // send Corona Lua event
    NSDictionary *coronaEvent = @{
      @(CoronaEventPhaseKey()): PHASE_EXPANDED,
      @(CoronaEventTypeKey()): self.adType
    };
    [self dispatchLuaEvent:coronaEvent];
    
}

- (void)didCollapseAd:(MAAd *)ad {
    // send Corona Lua event
    NSDictionary *coronaEvent = @{
      @(CoronaEventPhaseKey()): PHASE_COLLAPSE,
      @(CoronaEventTypeKey()): self.adType
    };
    [self dispatchLuaEvent:coronaEvent];
}


// ----------------------------------------------------------------------------

#pragma mark - MAAdDelegate Protocol


- (void)didDisplayAd:(MAAd *)ad {
    
    if ([ad format] != MAAdFormat.mrec || [ad format] != MAAdFormat.banner || [ad format] != MAAdFormat.leader) {
        NSDictionary *coronaEvent = @{
          @(CoronaEventPhaseKey()): PHASE_DISPLAYED,
          @(CoronaEventTypeKey()): self.adType
        };
        [self dispatchLuaEvent:coronaEvent];
    }
    
}


- (void)didHideAd:(MAAd *)ad
{
    if (ad.format != MAAdFormat.mrec) {
        // send Corona Lua event
        NSDictionary *coronaEvent = @{
          @(CoronaEventPhaseKey()): PHASE_HIDDEN,
          @(CoronaEventTypeKey()): self.adType
        };
        [self dispatchLuaEvent:coronaEvent];
    }
    if ([ad format] == MAAdFormat.rewarded) {
        [applovinObjects[USER_REWARDEDVIDEO_INSTANCE_KEY] loadAd];
    }
    
    
}

#pragma mark - MARewardedAdDelegate Protocol

- (void)didStartRewardedVideoForAd:(MAAd *)ad {
    // send Corona Lua event
    NSDictionary *coronaEvent = @{
      @(CoronaEventPhaseKey()): PHASE_PLAYBACK_BEGAN,
      @(CoronaEventTypeKey()): self.adType
    };
    [self dispatchLuaEvent:coronaEvent];
}

- (void)didCompleteRewardedVideoForAd:(MAAd *)ad {
    NSDictionary *data = @{
      @"percentPlayed": @100,
      @"fullyWatched": @(YES)
    };
    
    // send Corona Lua event
    NSDictionary *coronaEvent = @{
      @(CoronaEventPhaseKey()): PHASE_PLAYBACK_ENDED,
      @(CoronaEventTypeKey()): self.adType,
      @(CORONA_EVENT_DATA_KEY): data
    };
    [self dispatchLuaEvent:coronaEvent];
}

- (void)didRewardUserForAd:(MAAd *)ad withReward:(MAReward *)reward
{
    
    
    NSDictionary *data = @{
      @"amount": @(reward.amount),
      @"label": reward.label
    };
    // send Corona Lua event
    NSDictionary *coronaEvent = @{
      @(CoronaEventPhaseKey()): PHASE_RECEIVED_REWARD,
      @(CoronaEventTypeKey()): self.adType,
      @(CORONA_EVENT_DATA_KEY): data
    };
    [self dispatchLuaEvent:coronaEvent];
}




@end

// ----------------------------------------------------------------------------

@implementation CoronaApplovinAdStatus

- (instancetype)init {
  return [self initWithCoronaKey:false];
}

- (instancetype)initWithCoronaKey:(BOOL)usingCoronaKey
{
  if (self = [super init]) {
    self.ad = nil;
    self.isLoaded = NO;
    self.usingCoronaKey = usingCoronaKey;
    self.bannerIsVisible = NO;
  }
  
  return self;
}

- (void)invalidateInfo
{
  self.ad = nil;
}

- (void)dealloc
{
  [self invalidateInfo];
}

@end

// ----------------------------------------------------------------------------


CORONA_EXPORT int luaopen_plugin_applovinMax(lua_State *L)
{
  return ApplovinLibrary::Open(L);
}
