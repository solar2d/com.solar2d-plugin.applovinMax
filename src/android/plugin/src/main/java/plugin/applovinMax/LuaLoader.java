//
// LuaLoader.java
// Applovin Free Plugin
//
// Copyright (c) 2017 CoronaLabs inc. All rights reserved.
//

package plugin.applovinMax;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdRevenueListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaType;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.NamedJavaFunction;

import com.ansca.corona.CoronaActivity;
import com.ansca.corona.CoronaEnvironment;
import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaLuaEvent;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeListener;
import com.ansca.corona.CoronaRuntimeTask;
import com.ansca.corona.CoronaRuntimeTaskDispatcher;

import static java.lang.Math.ceil;

import java.util.*;

// Applovin
import com.applovin.sdk.*;


@SuppressWarnings("unused")
public class LuaLoader implements JavaFunction, CoronaRuntimeListener {
    private static final String PLUGIN_NAME = "plugin.applovinMax";
    private static final String PLUGIN_SDK_VERSION = AppLovinSdk.VERSION;

    private static final String EVENT_NAME = "adsRequest";
    private static final String PROVIDER_NAME = "applovinMax";

    // ad types
    private static final String TYPE_BANNER = "banner";
    private static final String TYPE_INTERSTITIAL = "interstitial";
    private static final String TYPE_REWARDEDVIDEO = "rewardedVideo";

    // valid ad types
    private static final List<String> validAdTypes = new ArrayList<>();

    // banner ad sizes
    private static final String BANNER_STANDARD = "standard";
    private static final String BANNER_LEADER = "leader";
    private static final String BANNER_MREC = "mrec";

    // valid banner sizes
    private static final List<String> validBannerSizes = new ArrayList<>();

    // banner alignment
    private static final String BANNER_ALIGN_TOP = "top";
    private static final String BANNER_ALIGN_CENTER = "center";
    private static final String BANNER_ALIGN_BOTTOM = "bottom";

    // valid banner positions
    private static final List<String> validBannerPositions = new ArrayList<>();

    // event phases
    private static final String PHASE_INIT = "init";
    private static final String PHASE_DISPLAYED = "displayed";
    private static final String PHASE_EXPANDED = "expanded";
    private static final String PHASE_COLLAPSED = "collapsed";
    private static final String PHASE_LOADED = "loaded";
    private static final String PHASE_FAILED = "failed";
    private static final String PHASE_CLOSED = "hidden"; // using 'hidden' for backwards compatibility with v1.x plugin
    private static final String PHASE_CLICKED = "clicked";
    private static final String PHASE_PLAYBACK_BEGAN = "playbackBegan";
    private static final String PHASE_PLAYBACK_ENDED = "playbackEnded";
    private static final String PHASE_RECEIVED_REWARD = "userReceivedReward";
    private static final String PHASE_RECEIVED_IMPRESSION_DATA = "impressionData";


    // ad revenue field keys
    private static final String EVENT_REVENUE_AD_SOURCE_KEY = "adSource";
    private static final String EVENT_REVENUE_AD_FORMAT_KEY = "adFormat";
    private static final String EVENT_REVENUE_AD_UNIT_NAME_KEY = "adUnitName";
    private static final String EVENT_REVENUE_AD_REVENUE_VALUE_KEY = "value";
    private static final String EVENT_REVENUE_AD_IMPRESSION_ID_KEY = "impressionID";
    private static final String EVENT_REVENUE_AD_PLACEMENT_NAME_KEY = "placementName";
    private static final String EVENT_REVENUE_AD_COUNTRY_CODE_KEY = "countryCode";
    private static final String EVENT_REVENUE_AD_REV_PRECISION_KEY = "revenuePrecision";


    // message constants
    private static final String CORONA_TAG = "Corona";
    private static final String ERROR_MSG = "ERROR: ";
    private static final String WARNING_MSG = "WARNING: ";

    // add missing event keys
    private static final String EVENT_PHASE_KEY = "phase";
    private static final String EVENT_DATA_KEY = "data";
    private static final String EVENT_TYPE_KEY = "type";

    // saved objects (apiKey, ad state, etc)
    final private static Map<String, Object> applovinObjects = new HashMap<>();

    // ad dictionary keys
    private static final String USER_SDK_KEY = "userSdk";
    private static final String USER_INTERSTITIAL_INSTANCE_KEY = "userInterstitial";
    private static final String USER_REWARDEDVIDEO_INSTANCE_KEY = "userRewardedVideo";
    private static final String USER_BANNER_INSTANCE_KEY = "userBanner";
    private static final String Y_RATIO_KEY = "yRatio";

    private static int coronaListener = CoronaLua.REFNIL;
    private static CoronaRuntimeTaskDispatcher coronaRuntimeTaskDispatcher = null;

    private final MaxActivityInter delInter = new MaxActivityInter();
    private final MaxActivityReward delReward = new MaxActivityReward();
    private final MaxActivityBanner delBanner = new MaxActivityBanner();

    private final MaxAdRevListenerInter revInter = new MaxAdRevListenerInter();
    private final MaxAdRevListenerReward revReward = new MaxAdRevListenerReward();
    private final MaxAdRevListenerBanner revBanner = new MaxAdRevListenerBanner();

    private static String functionSignature = "";

    // ----------------------------------------------------------------------------------
    // Helper classes to keep track of information not available in the SDK base classes
    // ----------------------------------------------------------------------------------

    private static class CoronaAdStatus {
        MaxAd ad;
        boolean isLoaded;
        boolean bannerIsVisible;

        CoronaAdStatus() {
            this.ad = null;
            this.isLoaded = false;
            this.bannerIsVisible = false;
        }

        void dealloc() {
            this.ad = null;
        }
    }

    // -------------------------------------------------------
    // Plugin lifecycle events
    // -------------------------------------------------------

    /**
     * Called after the Corona runtime has been created and just before executing the "main.lua" file.
     * <p>
     * Warning! This method is not called on the main thread.
     *
     * @param runtime Reference to the CoronaRuntime object that has just been loaded/initialized.
     *                Provides a LuaState object that allows the application to extend the Lua API.
     */
    @Override
    public void onLoaded(CoronaRuntime runtime) {
        // Note that this method will not be called the first time a Corona activity has been launched.
        // This is because this listener cannot be added to the CoronaEnvironment until after
        // this plugin has been required-in by Lua, which occurs after the onLoaded() event.
        // However, this method will be called when a 2nd Corona activity has been created.
        if (coronaRuntimeTaskDispatcher == null) {
            coronaRuntimeTaskDispatcher = new CoronaRuntimeTaskDispatcher(runtime);

            // initialize validation tables
            validAdTypes.add(TYPE_INTERSTITIAL);
            validAdTypes.add(TYPE_REWARDEDVIDEO);
            validAdTypes.add(TYPE_BANNER);

            validBannerSizes.add(BANNER_STANDARD);
            validBannerSizes.add(BANNER_LEADER);
            validBannerSizes.add(BANNER_MREC);

            validBannerPositions.add(BANNER_ALIGN_TOP);
            validBannerPositions.add(BANNER_ALIGN_CENTER);
            validBannerPositions.add(BANNER_ALIGN_BOTTOM);
        }
    }

    /**
     * Called just after the Corona runtime has executed the "main.lua" file.
     * <p>
     * Warning! This method is not called on the main thread.
     *
     * @param runtime Reference to the CoronaRuntime object that has just been started.
     */
    @Override
    public void onStarted(CoronaRuntime runtime) {
    }

    /**
     * Called just after the Corona runtime has been suspended which pauses all rendering, audio, timers,
     * and other Corona related operations. This can happen when another Android activity (ie: window) has
     * been displayed, when the screen has been powered off, or when the screen lock is shown.
     * <p>
     * Warning! This method is not called on the main thread.
     *
     * @param runtime Reference to the CoronaRuntime object that has just been suspended.
     */
    @Override
    public void onSuspended(CoronaRuntime runtime) {
    }

    /**
     * Called just after the Corona runtime has been resumed after a suspend.
     * <p>
     * Warning! This method is not called on the main thread.
     *
     * @param runtime Reference to the CoronaRuntime object that has just been resumed.
     */
    @Override
    public void onResumed(CoronaRuntime runtime) {
    }

    /**
     * Called just before the Corona runtime terminates.
     * <p>
     * This happens when the Corona activity is being destroyed which happens when the user presses the Back button
     * on the activity, when the native.requestExit() method is called in Lua, or when the activity's finish()
     * method is called. This does not mean that the application is exiting.
     * <p>
     * Warning! This method is not called on the main thread.
     *
     * @param runtime Reference to the CoronaRuntime object that is being terminated.
     */
    @Override
    public void onExiting(CoronaRuntime runtime) {
        // clear the saved ad objects
        for (String key : applovinObjects.keySet()) {
            Object object = applovinObjects.get(key);
            if (object instanceof CoronaAdStatus) {
                CoronaAdStatus adStatus = (CoronaAdStatus) object;
                adStatus.dealloc();
            }
        }
        applovinObjects.clear();

        CoronaLua.deleteRef(runtime.getLuaState(), coronaListener);
        coronaListener = CoronaLua.REFNIL;

        validAdTypes.clear();
        validBannerSizes.clear();
        validBannerPositions.clear();

        coronaRuntimeTaskDispatcher = null;
    }

    // --------------------------------------------------------------------------
    // helper functions
    // --------------------------------------------------------------------------

    // log message to console
    private void logMsg(String msgType, String errorMsg) {
        String functionID = functionSignature;
        if (!functionID.isEmpty()) {
            functionID += ", ";
        }

        Log.i(CORONA_TAG, msgType + functionID + errorMsg);
    }

    // return true if SDK is properly initialized
    private boolean isSDKInitialized() {
        if (coronaListener == CoronaLua.REFNIL) {
            logMsg(ERROR_MSG, "applovinMax.init() must be called before calling other API functions");
            return false;
        }

        return true;
    }

    // dispatch a Lua event to our callback (dynamic handling of properties through map)
    private void dispatchLuaEvent(final Map<String, Object> event) {
        if (coronaRuntimeTaskDispatcher != null) {
            coronaRuntimeTaskDispatcher.send(new CoronaRuntimeTask() {
                public void executeUsing(CoronaRuntime runtime) {
                    try {
                        LuaState L = runtime.getLuaState();
                        CoronaLua.newEvent(L, EVENT_NAME);
                        boolean hasErrorKey = false;

                        // add event parameters from map
                        for (String key : event.keySet()) {
                            CoronaLua.pushValue(L, event.get(key));           // push value
                            L.setField(-2, key);                              // push key

                            if (!hasErrorKey) {
                                hasErrorKey = key.equals(CoronaLuaEvent.ISERROR_KEY);
                            }
                        }

                        // add error key if not in map
                        if (!hasErrorKey) {
                            L.pushBoolean(false);
                            L.setField(-2, CoronaLuaEvent.ISERROR_KEY);
                        }

                        // add provider
                        L.pushString(PROVIDER_NAME);
                        L.setField(-2, CoronaLuaEvent.PROVIDER_KEY);

                        CoronaLua.dispatchEvent(L, coronaListener, 0);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    // -------------------------------------------------------
    // plugin implementation
    // -------------------------------------------------------

    /**
     * <p>
     * Note that a new LuaLoader instance will not be created for every CoronaActivity instance.
     * That is, only one instance of this class will be created for the lifetime of the application process.
     * This gives a plugin the option to do operations in the background while the CoronaActivity is destroyed.
     */
    public LuaLoader() {
        // Set up this plugin to listen for Corona runtime events to be received by methods
        // onLoaded(), onStarted(), onSuspended(), onResumed(), and onExiting().
        CoronaEnvironment.addRuntimeListener(this);
    }

    /**
     * Called when this plugin is being loaded via the Lua require() function.
     * <p>
     * Note that this method will be called everytime a new CoronaActivity has been launched.
     * This means that you'll need to re-initialize this plugin here.
     * <p>
     * Warning! This method is not called on the main UI thread.
     *
     * @param L Reference to the Lua state that the require() function was called from.
     * @return Returns the number of values that the require() function will return.
     * <p>
     * Expected to return 1, the library that the require() function is loading.
     */
    @Override
    public int invoke(LuaState L) {
        // Register this plugin into Lua with the following functions.
        NamedJavaFunction[] luaFunctions = new NamedJavaFunction[]
                {
                        new Init(),
                        new Load(),
                        new IsLoaded(),
                        new Hide(),
                        new Show(),
                        new SetUserDetails(),
                        new SetHasUserConsent(),
                        new SetIsAgeRestrictedUser(),
                        new showDebugger(),
                        new SetCreativeDebuggerEnabled(),
                };
        String libName = L.toString(1);
        L.register(libName, luaFunctions);

        // Returning 1 indicates that the Lua require() function will return the above Lua
        return 1;
    }

    // [Lua] applovinMax.init( listener, options )
    private class Init implements NamedJavaFunction {
        // Gets the name of the Lua function as it would appear in the Lua script
        @Override
        public String getName() {
            return "init";
        }

        // This method is executed when the Lua function is called
        @Override
        public int invoke(LuaState L) {
            functionSignature = "applovinMax.init(listener, options)";

            // prevent init from being called twice
            if (coronaListener != CoronaLua.REFNIL) {
                logMsg(WARNING_MSG, "init() should only be called once");
                return 0;
            }

            // check number of arguments
            int nargs = L.getTop();
            if (nargs != 2) {
                logMsg(ERROR_MSG, "Expected 2 arguments, got " + nargs);
                return 0;
            }

            String userSdkKey = null;
            boolean verboseLogging = false;
            boolean testMode = false;
            boolean startMuted = false;

            // get listener
            if (CoronaLua.isListener(L, 1, PROVIDER_NAME)) {
                coronaListener = CoronaLua.newRef(L, 1);
            } else {
                logMsg(ERROR_MSG, "listener expected, got: " + L.typeName(1));
                return 0;
            }

            // get options table
            if (L.type(2) == LuaType.TABLE) {
                for (L.pushNil(); L.next(2); L.pop(1)) {
                    if (L.type(-2) != LuaType.STRING) {
                        logMsg(ERROR_MSG, "options must be a key/value table");
                        return 0;
                    }

                    String key = L.toString(-2);

                    switch (key) {
                        case "verboseLogging":
                            if (L.type(-1) == LuaType.BOOLEAN) {
                                verboseLogging = L.toBoolean(-1);
                            } else {
                                logMsg(ERROR_MSG, "options.verboseLogging (boolean) expected, got: " + L.typeName(-1));
                                return 0;
                            }
                            break;
                        case "testMode":
                            if (L.type(-1) == LuaType.BOOLEAN) {
                                logMsg(WARNING_MSG, "options.testMode is ignored. Use UI to set test mode");
                            } else {
                                logMsg(ERROR_MSG, "options.testMode (boolean) expected, got: " + L.typeName(-1));
                                return 0;
                            }
                            break;
                        case "startMuted":
                            if (L.type(-1) == LuaType.BOOLEAN) {
                                startMuted = L.toBoolean(-1);
                            } else {
                                logMsg(ERROR_MSG, "options.startMuted (boolean) expected, got: " + L.typeName(-1));
                                return 0;
                            }
                            break;
                        default:
                            logMsg(ERROR_MSG, "Invalid option '" + key + "'");
                            return 0;
                    }
                }
            } else {
                logMsg(ERROR_MSG, "options (table) expected, got " + L.typeName(2));
                return 0;
            }

            // create Applovin SDK settings
            final Context coronaContext = CoronaEnvironment.getApplicationContext();

            AppLovinSdkSettings sdkSettings = AppLovinSdk.getInstance( coronaContext ).getSettings();
            sdkSettings.setVerboseLogging(verboseLogging);
            sdkSettings.setMuted(startMuted);

            AppLovinSdkInitializationConfiguration initConfig = AppLovinSdkInitializationConfiguration.builder( userSdkKey, coronaContext )
                    .setMediationProvider( AppLovinMediationProvider.MAX )
                    .build();


            AppLovinSdk.getInstance( coronaContext ).initialize( initConfig, new AppLovinSdk.SdkInitializationListener()
            {
                @Override
                public void onSdkInitialized(final AppLovinSdkConfiguration sdkConfig)
                {
                    // send Corona Lua Event
                    Map<String, Object> coronaEvent = new HashMap<>();
                    coronaEvent.put(EVENT_PHASE_KEY, PHASE_INIT);
                    dispatchLuaEvent(coronaEvent);
                }
            } );

            // log the plugin version to device console
            Log.i(CORONA_TAG, PLUGIN_NAME + " (SDK: " + PLUGIN_SDK_VERSION + ")");

            return 0;
        }
    }

    // [Lua] applovinMax.load( adType [, options] )
    private class Load implements NamedJavaFunction {
        // Gets the name of the Lua function as it would appear in the Lua script
        @Override
        public String getName() {
            return "load";
        }

        // This method is executed when the Lua function is called
        @Override
        public int invoke(final LuaState L) {
            functionSignature = "applovinMax.load( adType [, options] )";
            if (!isSDKInitialized()) {
                return 0;
            }
            // check number of arguments
            // need to accept 0 args for backwards compatibility
            int nargs = L.getTop();
            if (nargs > 2) {
                logMsg(ERROR_MSG, "Expected 1 or 2 arguments, got " + nargs);
                return 0;
            }

            boolean rewarded = false;
            boolean legacyAPI = true;
            String adType = null;
            String bannerSize = null;
            String adUnitId = null;

            // check args
            if (!L.isNoneOrNil(1)) {
                if (L.type(1) == LuaType.BOOLEAN) {
                    rewarded = L.toBoolean(1);
                } else if (L.type(1) == LuaType.STRING) {
                    legacyAPI = false;
                    adType = L.toString(1);
                } else {
                    logMsg(ERROR_MSG, "adType (string) expected, got: " + L.typeName(1));
                    return 0;
                }
            }
            // get options table
            if (!L.isNoneOrNil(2)) {
                if (L.type(2) == LuaType.TABLE) {
                    for (L.pushNil(); L.next(2); L.pop(1)) {
                        if (L.type(-2) != LuaType.STRING) {
                            logMsg(ERROR_MSG, "options must be a key/value table");
                            return 0;
                        }

                        String key = L.toString(-2);

                        if (key.equals("bannerSize")) {
                            if (L.type(-1) == LuaType.STRING) {
                                bannerSize = L.toString(-1);
                            } else {
                                logMsg(ERROR_MSG, "options.bannerSize (string) expected, got: " + L.typeName(-1));
                                return 0;
                            }
                        }else if (key.equals("androidUnitId")) {
                            if (L.type(-1) == LuaType.STRING) {
                                adUnitId = L.toString(-1);
                            } else {
                                logMsg(ERROR_MSG, "options.androidUnitId (string) expected, got: " + L.typeName(-1));
                                return 0;
                            }
                        }else if (key.equals("iOSUnitId")) {
                            //padding to prevent error
                        } else {
                            logMsg(ERROR_MSG, "Invalid option '" + key + "'");
                            return 0;
                        }
                    }
                } else {
                    logMsg(ERROR_MSG, "options (table) expected, got " + L.typeName(2));
                    return 0;
                }
            }

            // validate
            if (!legacyAPI) {
                if (!validAdTypes.contains(adType)) {
                    logMsg(ERROR_MSG, "Invalid adType '" + adType + "'");
                    return 0;
                }

                rewarded = adType.equals(TYPE_REWARDEDVIDEO);

                // check banner size
                if (bannerSize != null) {
                    if (!validBannerSizes.contains(bannerSize)) {
                        logMsg(ERROR_MSG, "Invalid banner size '" + bannerSize + "'");
                        return 0;
                    }
                }
            } else {
                if (rewarded) {
                    adType = TYPE_REWARDEDVIDEO;
                } else {
                    adType = TYPE_INTERSTITIAL;
                }
            }
            if(adUnitId == null){
                logMsg(ERROR_MSG, "androidUnitId is param is required in options table but found nil");
                return 0;
            }
            final CoronaActivity coronaActivity = CoronaEnvironment.getCoronaActivity();
            final boolean fRewarded = rewarded;
            final String fAdType = adType;
            final String fBannerSize = bannerSize;
            final String fAdUnitId = adUnitId;

            if (coronaActivity != null) {
                coronaActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (fRewarded) {
                            String activeInstanceKey = USER_REWARDEDVIDEO_INSTANCE_KEY;
                            MaxRewardedAd rewardedAd = (MaxRewardedAd) applovinObjects.get(activeInstanceKey);

                            // initialize rewarded object
                            if (rewardedAd == null) {
                                rewardedAd = MaxRewardedAd.getInstance(fAdUnitId, CoronaEnvironment.getCoronaActivity());
                                applovinObjects.put(activeInstanceKey, rewardedAd);
                            }

                            // save extra ad status information not available in ad object
                            CoronaAdStatus adStatus = (CoronaAdStatus) applovinObjects.get(TYPE_REWARDEDVIDEO);
                            if (adStatus != null) { // remove old status
                                adStatus.dealloc();
                            }
                            adStatus = new CoronaAdStatus();
                            applovinObjects.put(TYPE_REWARDEDVIDEO, adStatus);

                            rewardedAd.setListener(delReward);
                            rewardedAd.setRevenueListener(revReward);
                            rewardedAd.loadAd();

                        }else if (fAdType.equals(TYPE_BANNER)) {
                            // calculate the Corona->device coordinate ratio.
                            // we don't use display.contentScaleY here as there are cases where it's difficult to get the proper values to use
                            // especially on Android. uses the same formula for iOS and Android for the sake of consistency.
                            // re-calculate this value on every load as the ratio can change between orientation changes
                            Point point1 = coronaActivity.convertCoronaPointToAndroidPoint(0, 0);
                            Point point2 = coronaActivity.convertCoronaPointToAndroidPoint(1000, 1000);
                            double yRatio = (double) (point2.y - point1.y) / 1000.0;
                            applovinObjects.put(Y_RATIO_KEY, yRatio);

                            MaxAdView bannerAd = (MaxAdView) applovinObjects.get(USER_BANNER_INSTANCE_KEY);

                            // remove old banner
                            if (bannerAd != null) {
                                bannerAd.removeAllViews();
                                bannerAd.destroy();
                            }

                            MaxAdFormat applovinBannerSize = MaxAdFormat.BANNER;

                            if ((fBannerSize == null) || (fBannerSize.equals(BANNER_STANDARD))) {
                                applovinBannerSize = MaxAdFormat.BANNER;
                            } else if (fBannerSize.equals(BANNER_LEADER)) {
                                applovinBannerSize = MaxAdFormat.LEADER;
                            } else if (fBannerSize.equals(BANNER_MREC)) {
                                applovinBannerSize = MaxAdFormat.MREC;
                            }


                            bannerAd = new MaxAdView( fAdUnitId, applovinBannerSize, CoronaEnvironment.getCoronaActivity() );
                            bannerAd.setListener(delBanner);
                            bannerAd.setRevenueListener(revBanner);

                            applovinObjects.put(USER_BANNER_INSTANCE_KEY, bannerAd);

                            // save extra ad status information not available in ad object
                            CoronaAdStatus adStatus = (CoronaAdStatus) applovinObjects.get(TYPE_BANNER);
                            if (adStatus != null) { // remove old status
                                adStatus.dealloc();
                            }
                            adStatus = new CoronaAdStatus();
                            applovinObjects.put(TYPE_BANNER, adStatus);

                            bannerAd.loadAd();
                        } else { // interstitial
                            MaxInterstitialAd interstitialAd = (MaxInterstitialAd) applovinObjects.get(USER_INTERSTITIAL_INSTANCE_KEY);

                            // initialize interstitial object
                            if (interstitialAd == null) {
                                interstitialAd = new MaxInterstitialAd(fAdUnitId, CoronaEnvironment.getCoronaActivity());
                                interstitialAd.setListener(delInter);
                                interstitialAd.setRevenueListener(revInter);
                                applovinObjects.put(USER_INTERSTITIAL_INSTANCE_KEY, interstitialAd);
                            }
                            interstitialAd.loadAd();
                            // save extra ad status information not available in ad object
                            CoronaAdStatus adStatus = (CoronaAdStatus) applovinObjects.get(TYPE_INTERSTITIAL);
                            if (adStatus != null) { // remove old status
                                adStatus.dealloc();
                            }
                            adStatus = new CoronaAdStatus();
                            applovinObjects.put(TYPE_INTERSTITIAL, adStatus);

                        }


                    }
                });
            }

            return 0;
        }
    }

    // [Lua] applovinMax.isLoaded( adType )
    private class IsLoaded implements NamedJavaFunction {
        @Override
        public String getName() {
            return "isLoaded";
        }

        @Override
        public int invoke(LuaState L) {
            functionSignature = "applovinMax.isLoaded( adType )";

            if (!isSDKInitialized()) {
                return 0;
            }

            // check number of arguments
            // need to accept 0 args for backwards compatibility
            int nargs = L.getTop();
            if (nargs > 1) {
                logMsg(ERROR_MSG, "Expected 1 argument, got " + nargs);
                return 0;
            }

            boolean rewarded = false;
            boolean legacyAPI = true;
            String adType = null;

            // check options
            if (!L.isNoneOrNil(1)) {
                if (L.type(1) == LuaType.BOOLEAN) {
                    rewarded = L.toBoolean(1);
                } else if (L.type(1) == LuaType.STRING) {
                    legacyAPI = false;
                    adType = L.toString(1);
                } else {
                    logMsg(ERROR_MSG, "adType (string) expected, got: " + L.typeName(1));
                    return 0;
                }
            }

            // validate
            if (!legacyAPI) {
                if (!validAdTypes.contains(adType)) {
                    logMsg(ERROR_MSG, "Invalid adType '" + adType + "'");
                    return 0;
                }
            }

            CoronaAdStatus adStatus;

            if (legacyAPI) {
                adStatus = (CoronaAdStatus) applovinObjects.get(rewarded ? TYPE_REWARDEDVIDEO : TYPE_INTERSTITIAL);
            } else {
                adStatus = (CoronaAdStatus) applovinObjects.get(adType);
            }

            boolean isAdLoaded = (adStatus != null) && (adStatus.ad != null) && (adStatus.isLoaded || adStatus.bannerIsVisible);
            L.pushBoolean(isAdLoaded);

            return 1;
        }
    }

    // [Lua] applovinMax.hide( adType )
    private class Hide implements NamedJavaFunction {
        @Override
        public String getName() {
            return "hide";
        }

        @Override
        public int invoke(LuaState L) {
            functionSignature = "applovinMax.hide( adType )";

            if (!isSDKInitialized()) {
                return 0;
            }

            // check number of arguments
            int nargs = L.getTop();
            if (nargs != 1) {
                logMsg(ERROR_MSG, "Expected 1 argument, got " + nargs);
                return 0;
            }

            String adType;

            // check options
            if (L.type(1) == LuaType.STRING) {
                adType = L.toString(1);
            } else {
                logMsg(ERROR_MSG, "adType (string) expected, got: " + L.typeName(1));
                return 0;
            }

            // validate
            if (!adType.equals(TYPE_BANNER)) {
                logMsg(ERROR_MSG, "Invalid adType '" + adType + "'. Only banners han be hidden");
                return 0;
            }

            CoronaAdStatus adStatus = (CoronaAdStatus) applovinObjects.get(TYPE_BANNER);
            if ((adStatus == null) || (adStatus.ad == null) || (!adStatus.isLoaded && !adStatus.bannerIsVisible)) {
                logMsg(ERROR_MSG, "Banner not loaded");
                return 0;
            }

            final CoronaActivity coronaActivity = CoronaEnvironment.getCoronaActivity();
            final CoronaAdStatus fAdStatus = adStatus;
            final String fAdType = adType;

            if (coronaActivity != null) {
                coronaActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // send custom hidden event
                        Map<String, Object> coronaEvent = new HashMap<>();
                        coronaEvent.put(EVENT_PHASE_KEY, PHASE_CLOSED);
                        coronaEvent.put(EVENT_TYPE_KEY, fAdType);
                        dispatchLuaEvent(coronaEvent);

                        MaxAdView bannerAd = (MaxAdView) applovinObjects.get(USER_BANNER_INSTANCE_KEY);
                        if (bannerAd != null) {
                            bannerAd.removeAllViews();
                            bannerAd.destroy();
                        }
                        fAdStatus.dealloc();
                        applovinObjects.remove(TYPE_BANNER);
                        applovinObjects.remove(USER_BANNER_INSTANCE_KEY);
                    }
                });
            }

            return 0;
        }
    }

    // [Lua] applovin.show( adType [, options] )
    private class Show implements NamedJavaFunction {
        // Gets the name of the Lua function as it would appear in the Lua script
        @Override
        public String getName() {
            return "show";
        }

        // This method is executed when the Lua function is called
        @Override
        public int invoke(LuaState L) {
            functionSignature = "applovin.show( adType [, options] )";

            if (!isSDKInitialized()) {
                return 0;
            }

            // check number of arguments
            // need to accept 0 args for backwards compatibility
            int nargs = L.getTop();
            if (nargs > 2) {
                logMsg(ERROR_MSG, "Expected 1 or 2 arguments, got " + nargs);
                return 0;
            }

            boolean rewarded = false;
            boolean legacyAPI = true;
            String placement = null;
            String adType = null;
            String yAlign = null;
            double yOffset = 0;

            // check options
            if (!L.isNoneOrNil(1)) {
                if (L.type(1) == LuaType.BOOLEAN) {
                    rewarded = L.toBoolean(1);
                } else if (L.type(1) == LuaType.STRING) {
                    legacyAPI = false;
                    adType = L.toString(1);
                } else {
                    logMsg(ERROR_MSG, "adType (string) expected, got: " + L.typeName(1));
                    return 0;
                }
            }

            // check ad type
            if (!L.isNoneOrNil(2)) {
                if (L.type(2) == LuaType.STRING) {
                    placement = L.toString(2);
                } else if (L.type(2) == LuaType.TABLE) {
                    for (L.pushNil(); L.next(2); L.pop(1)) {
                        if (L.type(-2) != LuaType.STRING) {
                            logMsg(ERROR_MSG, "options must be a key/value table");
                            return 0;
                        }

                        String key = L.toString(-2);

                        if (key.equals("placement")) {
                            if (L.type(-1) == LuaType.STRING) {
                                placement = L.toString(-1);
                            } else {
                                logMsg(ERROR_MSG, "options.placement (string) expected, got: " + L.typeName(-1));
                                return 0;
                            }
                        } else if (key.equals("y")) {
                            if (L.type(-1) == LuaType.STRING) {
                                yAlign = L.toString(-1);
                            } else if (L.type(-1) == LuaType.NUMBER) {
                                yOffset = L.toNumber(-1);
                            } else {
                                logMsg(ERROR_MSG, "options.y (string or number) expected, got: " + L.typeName(-1));
                                return 0;
                            }
                        } else {
                            logMsg(ERROR_MSG, "Invalid option '" + key + "'");
                            return 0;
                        }
                    }
                } else {
                    logMsg(ERROR_MSG, "options (table) expected, got: " + L.typeName(2));
                    return 0;
                }
            }

            // validate
            if (!legacyAPI) {
                if (!validAdTypes.contains(adType)) {
                    logMsg(ERROR_MSG, "Invalid adType '" + adType + "'");
                    return 0;
                }

                if (adType.equals(TYPE_INTERSTITIAL)) {
                    rewarded = false;
                } else if (adType.equals(TYPE_REWARDEDVIDEO)) {
                    rewarded = true;
                }

                if (yAlign != null) {
                    if (!validBannerPositions.contains(yAlign)) {
                        logMsg(ERROR_MSG, "y '" + yAlign + "' invalid");
                        return 0;
                    }
                }
            }

            final CoronaActivity coronaActivity = CoronaEnvironment.getCoronaActivity();
            final boolean fRewarded = rewarded;
            final String fPlacement = placement;
            final String fAdType = adType;
            final String fYAlign = yAlign;
            final double fYOffset = yOffset;

            if (coronaActivity != null) {
                Runnable runnableActivity = new Runnable() {
                    public void run() {
                        if (fRewarded) {
                            CoronaAdStatus adStatus = (CoronaAdStatus) applovinObjects.get(TYPE_REWARDEDVIDEO);
                            if ((adStatus == null) || (adStatus.ad == null) || !adStatus.isLoaded) {
                                logMsg(ERROR_MSG, "Rewarded video not loaded");
                                return;
                            }

                            MaxRewardedAd rewardedAd = (MaxRewardedAd) applovinObjects.get(USER_REWARDEDVIDEO_INSTANCE_KEY);
                            rewardedAd.setListener(delReward);
                            rewardedAd.setRevenueListener(revReward);

                            Map<String, Object> coronaEvent = new HashMap<>();
                            coronaEvent.put(EVENT_PHASE_KEY, PHASE_DISPLAYED);
                            coronaEvent.put(EVENT_TYPE_KEY, fAdType);
                            dispatchLuaEvent(coronaEvent);
                            if (fPlacement != null) {
                                rewardedAd.showAd(fPlacement);

                            } else {
                                rewardedAd.showAd();
                            }


                        } else { // interstitial or banner
                            if (fAdType != null && fAdType.equals(TYPE_BANNER)) {
                                CoronaAdStatus adStatus = (CoronaAdStatus) applovinObjects.get(TYPE_BANNER);
                                if ((adStatus == null) || (adStatus.ad == null) || (!adStatus.isLoaded && !adStatus.bannerIsVisible)) {
                                    logMsg(ERROR_MSG, "Banner not loaded");
                                    return;
                                } else if (adStatus.bannerIsVisible) {
                                    logMsg(ERROR_MSG, "Banner already visable");
                                    return;
                                }

                                MaxAdView bannerAd = (MaxAdView) applovinObjects.get(USER_BANNER_INSTANCE_KEY);
                                bannerAd.setListener(delBanner);
                                bannerAd.setRevenueListener(revBanner);
                                if(fPlacement != null) bannerAd.setPlacement(fPlacement);

                                // remove old layout
                                if (bannerAd.getParent() != null) {
                                    coronaActivity.getOverlayView().removeView(bannerAd);
                                }

                                // set final layout params
                                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.WRAP_CONTENT,
                                        140
                                );
                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    WindowInsets windowInsets = coronaActivity.getWindow().getDecorView().getRootView().getRootWindowInsets();
                                    DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                                    //check displayCutout actually exists
                                    if (displayCutout != null && displayCutout.getBoundingRects().size() > 0) {
                                        params.topMargin = (int) displayCutout.getSafeInsetTop();
                                    }
                                }

                                // set the banner position
                                if (fYAlign == null) {
                                    Display display = coronaActivity.getWindowManager().getDefaultDisplay();
                                    int orientation = coronaActivity.getResources().getConfiguration().orientation;
                                    int orientedHeight;

                                    Point size = new Point();
                                    display.getSize(size);

                                    if (orientation == ORIENTATION_PORTRAIT) {
                                        orientedHeight = size.y;
                                    } else {
                                        orientedHeight = size.x;
                                    }

                                    double newBannerY = ceil(fYOffset * (double) applovinObjects.get(Y_RATIO_KEY));

                                    // make sure the banner frame is visible.
                                    // adjust it if the user has specified 'y' which will render it partially off-screen
                                    if (newBannerY >= 0) { // offset from top
                                        if (newBannerY + bannerAd.getHeight() > orientedHeight) {
                                            logMsg(WARNING_MSG, "Banner y position off screen. Adjusting position.");
                                            params.gravity = Gravity.BOTTOM | Gravity.CENTER;
                                        } else {
                                            params.gravity = Gravity.TOP | Gravity.CENTER;
                                            params.topMargin = (int) newBannerY;
                                        }
                                    } else { // offset from bottom
                                        if (orientedHeight - bannerAd.getHeight() + newBannerY < 0) {
                                            logMsg(WARNING_MSG, "Banner y position off screen. Adjusting position.");
                                            params.gravity = Gravity.TOP | Gravity.CENTER;
                                        } else {
                                            params.gravity = Gravity.BOTTOM | Gravity.CENTER;
                                            params.bottomMargin = Math.abs((int) newBannerY);
                                        }
                                    }
                                } else {

                                    switch (fYAlign) {
                                        case BANNER_ALIGN_TOP:
                                            params.gravity = Gravity.TOP | Gravity.CENTER;
                                            break;
                                        case BANNER_ALIGN_CENTER:
                                            params.gravity = Gravity.CENTER;
                                            bannerAd.setVerticalGravity(Gravity.CENTER);
                                            break;
                                        case BANNER_ALIGN_BOTTOM:
                                            params.gravity = Gravity.BOTTOM | Gravity.CENTER;
                                            bannerAd.setVerticalGravity(Gravity.BOTTOM);
                                            break;
                                    }
                                }
                                Map<String, Object> coronaEvent = new HashMap<>();
                                coronaEvent.put(EVENT_PHASE_KEY, PHASE_DISPLAYED);
                                coronaEvent.put(EVENT_TYPE_KEY, fAdType);
                                dispatchLuaEvent(coronaEvent);
                                // display the banner
                                coronaActivity.getOverlayView().addView(bannerAd, params);
                                bannerAd.setVisibility(View.VISIBLE);
                                bannerAd.bringToFront();
                                adStatus.bannerIsVisible = true;
                            } else { // interstitial
                                CoronaAdStatus adStatus = (CoronaAdStatus) applovinObjects.get(TYPE_INTERSTITIAL);
                                if ((adStatus == null) || (adStatus.ad == null) || !adStatus.isLoaded) {
                                    logMsg(ERROR_MSG, "Interstitial not loaded");
                                    return;
                                }

                                MaxInterstitialAd interstitialAd = (MaxInterstitialAd) applovinObjects.get(USER_INTERSTITIAL_INSTANCE_KEY);
                                interstitialAd.setListener(delInter);
                                interstitialAd.setRevenueListener(revInter);



                                if (fPlacement != null) {
                                    interstitialAd.showAd(fPlacement);
                                }else{
                                    interstitialAd.showAd();
                                }
                            }
                        }
                    }
                };

                coronaActivity.runOnUiThread(runnableActivity);
            }

            return 0;
        }
    }

    // [Lua] applovinMax.setUserDetails( options )
    private class SetUserDetails implements NamedJavaFunction {
        @Override
        public String getName() {
            return "setUserDetails";
        }

        @Override
        public int invoke(LuaState L) {
            functionSignature = "applovinMax.setUserDetails( options )";

            if (!isSDKInitialized()) {
                return 0;
            }

            // check number of arguments
            int nargs = L.getTop();
            if (nargs != 1) {
                logMsg(ERROR_MSG, "Expected 1 argument, got " + nargs);
                return 0;
            }


            // check options
            if (L.type(1) == LuaType.TABLE) {
                for (L.pushNil(); L.next(1); L.pop(1)) {
                    if (L.type(-2) != LuaType.STRING) {
                        logMsg(ERROR_MSG, "options must be a key/value table");
                        return 0;
                    }

                    String key = L.toString(-2);

                    if (key.equals("userId")) {
                        if (L.type(-1) == LuaType.STRING) {
                            AppLovinSdk.getInstance( CoronaEnvironment.getApplicationContext() ).getSettings().setUserIdentifier(L.toString(-1));
                        } else {
                            logMsg(ERROR_MSG, "options.userId (string) expected, got: " + L.typeName(-1));
                            return 0;
                        }
                    } else {
                        logMsg(ERROR_MSG, "Invalid option '" + key + "'");
                        return 0;
                    }
                }
            } else {
                logMsg(ERROR_MSG, "options (table) expected, got " + L.typeName(1));
                return 0;
            }





            return 0;
        }
    }

    // [Lua] applovinMax.setHasUserConsent( bool )
    private class SetHasUserConsent implements NamedJavaFunction {
        @Override
        public String getName() {
            return "setHasUserConsent";
        }

        @Override
        public int invoke(LuaState L) {
            functionSignature = "applovinMax.setHasUserConsent( bool )";

            if (!isSDKInitialized()) {
                return 0;
            }

            // check number of arguments
            int nargs = L.getTop();
            if (nargs != 1) {
                logMsg(ERROR_MSG, "Expected 1 argument, got " + nargs);
                return 0;
            }

            boolean setHasUserConsent;

            // check options
            if (L.type(1) == LuaType.BOOLEAN) {
                setHasUserConsent = L.toBoolean(1);
            } else {
                logMsg(ERROR_MSG, "setHasUserConsent (bool) expected, got " + L.typeName(1));
                return 0;
            }

            AppLovinPrivacySettings.setHasUserConsent(setHasUserConsent, CoronaEnvironment.getApplicationContext());

            return 0;
        }
    }

    // [Lua] applovinMax.setIsAgeRestrictedUser( bool )
    private class SetIsAgeRestrictedUser implements NamedJavaFunction {
        @Override
        public String getName() {
            return "setIsAgeRestrictedUser";
        }

        @Override
        public int invoke(LuaState L) {
            functionSignature = "applovinMax.setIsAgeRestrictedUser( bool )";

            if (!isSDKInitialized()) {
                return 0;
            }

            // check number of arguments
            int nargs = L.getTop();
            if (nargs != 1) {
                logMsg(ERROR_MSG, "Expected 1 argument, got " + nargs);
                return 0;
            }

            boolean isAgeRestrictedUser;

            // check options
            if (L.type(1) == LuaType.BOOLEAN) {
                isAgeRestrictedUser = L.toBoolean(1);
            } else {
                logMsg(ERROR_MSG, "setIsAgeRestrictedUser (bool) expected, got " + L.typeName(1));
                return 0;
            }

            logMsg(WARNING_MSG, "This is no longer supported, don't use if user is a child");
            return 0;
        }
    }

    // [Lua] applovinMax.showDebugger(  )
    private class showDebugger implements NamedJavaFunction {
        @Override
        public String getName() {
            return "showDebugger";
        }

        @Override
        public int invoke(LuaState L) {
            functionSignature = "applovinMax.showDebugger()";

            if (!isSDKInitialized()) {
                return 0;
            }

            AppLovinSdk.getInstance( CoronaEnvironment.getApplicationContext() ).showMediationDebugger();
            return 0;
        }
    }

    // [Lua] applovinMax.setCreativeDebuggerEnabled( bool )
    private class SetCreativeDebuggerEnabled implements NamedJavaFunction {
        @Override
        public String getName() {
            return "setCreativeDebuggerEnabled";
        }

        @Override
        public int invoke(LuaState L) {
            functionSignature = "applovinMax.setCreativeDebuggerEnabled( bool )";

            if (!isSDKInitialized()) {
                return 0;
            }

            // check number of arguments
            int nargs = L.getTop();
            if (nargs != 1) {
                logMsg(ERROR_MSG, "Expected 1 argument, got " + nargs);
                return 0;
            }

            boolean creativeDebugger;

            // check options
            if (L.type(1) == LuaType.BOOLEAN) {
                creativeDebugger = L.toBoolean(1);
            } else {
                logMsg(ERROR_MSG, "setCreativeDebuggerEnabled (bool) expected, got " + L.typeName(1));
                return 0;
            }
            AppLovinSdk.getInstance(CoronaEnvironment.getApplicationContext()).getSettings().setCreativeDebuggerEnabled( creativeDebugger );
            return 0;
        }
    }



    // ----------------------------------------------------------------------------
    // delegate implementation
    // ----------------------------------------------------------------------------

    class MaxActivityInter implements MaxAdListener
    {
        //region MAX Ad Listener
        String adType = TYPE_INTERSTITIAL;


        @Override
        public void onAdLoaded(final MaxAd ad)
        {
            CoronaAdStatus adStatus = (CoronaAdStatus) applovinObjects.get(adType);

            if (adStatus != null) {
                adStatus.ad = ad;
                adStatus.isLoaded = true;

                // send Corona Lua event
                Map<String, Object> coronaEvent = new HashMap<>();
                coronaEvent.put(EVENT_PHASE_KEY, PHASE_LOADED);
                coronaEvent.put(EVENT_TYPE_KEY, adType);
                dispatchLuaEvent(coronaEvent);
            }
        }



        @Override
        public void onAdDisplayed(final MaxAd ad) {
            CoronaAdStatus adStatus = (CoronaAdStatus) applovinObjects.get(adType);

            if (adStatus != null) {
                adStatus.isLoaded = false;

                // send Corona Lua event
                Map<String, Object> coronaEvent = new HashMap<>();
                coronaEvent.put(EVENT_PHASE_KEY, PHASE_DISPLAYED);
                coronaEvent.put(EVENT_TYPE_KEY, adType);
                dispatchLuaEvent(coronaEvent);
            }
        }

        @Override
        public void onAdClicked(final MaxAd ad) {
            // send Corona Lua event
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_CLICKED);
            coronaEvent.put(EVENT_TYPE_KEY, adType);
            dispatchLuaEvent(coronaEvent);
        }

        @Override
        public void onAdLoadFailed(final String adUnitId, final MaxError error) {
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_FAILED);
            coronaEvent.put(CoronaLuaEvent.ISERROR_KEY, true);
            coronaEvent.put(EVENT_TYPE_KEY, adType);
            coronaEvent.put(CoronaLuaEvent.RESPONSE_KEY, error.getMessage());
            dispatchLuaEvent(coronaEvent);
        }

        @Override
        public void onAdDisplayFailed(final MaxAd ad, final MaxError error) {
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_FAILED);
            coronaEvent.put(CoronaLuaEvent.ISERROR_KEY, true);
            coronaEvent.put(EVENT_TYPE_KEY, adType);
            coronaEvent.put(CoronaLuaEvent.RESPONSE_KEY, error.getMessage());
            dispatchLuaEvent(coronaEvent);
        }

        @Override
        public void onAdHidden(final MaxAd ad)
        {
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_CLOSED);
            coronaEvent.put(EVENT_TYPE_KEY, adType);
            dispatchLuaEvent(coronaEvent);
        }
    }
    class MaxActivityReward implements MaxRewardedAdListener
    {
        //region MAX Ad Listener
        String adType = TYPE_REWARDEDVIDEO;
        @Override
        public void onAdLoaded(final MaxAd ad)
        {
            CoronaAdStatus adStatus = (CoronaAdStatus) applovinObjects.get(adType);

            if (adStatus != null) {
                adStatus.ad = ad;
                adStatus.isLoaded = true;

                // send Corona Lua event
                Map<String, Object> coronaEvent = new HashMap<>();
                coronaEvent.put(EVENT_PHASE_KEY, PHASE_LOADED);
                coronaEvent.put(EVENT_TYPE_KEY, adType);
                dispatchLuaEvent(coronaEvent);

                // increment saved ad count
                CoronaActivity coronaActivity = CoronaEnvironment.getCoronaActivity();
                if (coronaActivity != null) {
                    SharedPreferences sharedPref = coronaActivity.getPreferences(Context.MODE_PRIVATE);
                    long currentAdCount = sharedPref.getLong(adType, 0);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putLong(adType, ++currentAdCount);
                    editor.apply();
                }
            }
        }




        @Override
        public void onAdDisplayed(final MaxAd ad) {
            CoronaAdStatus adStatus = (CoronaAdStatus) applovinObjects.get(adType);

            if (adStatus != null) {
                adStatus.isLoaded = false;

                // send Corona Lua event
                Map<String, Object> coronaEvent = new HashMap<>();
                coronaEvent.put(EVENT_PHASE_KEY, PHASE_DISPLAYED);
                coronaEvent.put(EVENT_TYPE_KEY, adType);
                dispatchLuaEvent(coronaEvent);
            }
        }

        @Override
        public void onAdClicked(final MaxAd ad) {
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_CLICKED);
            coronaEvent.put(EVENT_TYPE_KEY, adType);
            dispatchLuaEvent(coronaEvent);
        }

        @Override
        public void onAdLoadFailed(final String adUnitId, final MaxError error) {
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_FAILED);
            coronaEvent.put(CoronaLuaEvent.ISERROR_KEY, true);
            coronaEvent.put(EVENT_TYPE_KEY, adType);
            coronaEvent.put(CoronaLuaEvent.RESPONSE_KEY, error.getMessage());
            dispatchLuaEvent(coronaEvent);
        }

        @Override
        public void onAdDisplayFailed(MaxAd ad, MaxError error) {
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_FAILED);
            coronaEvent.put(CoronaLuaEvent.ISERROR_KEY, true);
            coronaEvent.put(EVENT_TYPE_KEY, adType);
            coronaEvent.put(CoronaLuaEvent.RESPONSE_KEY, error.getMessage());
            dispatchLuaEvent(coronaEvent);
        }

        @Override
        public void onAdHidden(final MaxAd ad)
        {
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_CLOSED);
            coronaEvent.put(EVENT_TYPE_KEY, adType);
            dispatchLuaEvent(coronaEvent);
        }

        @Override
        public void onUserRewarded(final MaxAd ad, final MaxReward reward)
        {

            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_RECEIVED_REWARD);
            coronaEvent.put(EVENT_TYPE_KEY, adType);
            dispatchLuaEvent(coronaEvent);
        }
    }
    class MaxActivityBanner implements MaxAdViewAdListener
    {
        String adType = TYPE_BANNER;
        @Override
        public void onAdLoaded(final MaxAd ad) {
            CoronaAdStatus adStatus = (CoronaAdStatus) applovinObjects.get(adType);

            if (adStatus != null) {
                adStatus.ad = ad;
                adStatus.isLoaded = true;

                // send Corona Lua event
                Map<String, Object> coronaEvent = new HashMap<>();
                coronaEvent.put(EVENT_PHASE_KEY, PHASE_LOADED);
                coronaEvent.put(EVENT_TYPE_KEY, adType);
                dispatchLuaEvent(coronaEvent);

                // increment saved ad count
                CoronaActivity coronaActivity = CoronaEnvironment.getCoronaActivity();
                if (coronaActivity != null) {
                    SharedPreferences sharedPref = coronaActivity.getPreferences(Context.MODE_PRIVATE);
                    long currentAdCount = sharedPref.getLong(adType, 0);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putLong(adType, ++currentAdCount);
                    editor.apply();
                }
            }
        }



        @Override
        public void onAdHidden(final MaxAd ad) {
            if (!adType.equals(TYPE_BANNER)) {
                // send Corona Lua event
                Map<String, Object> coronaEvent = new HashMap<>();
                coronaEvent.put(EVENT_PHASE_KEY, PHASE_CLOSED);
                coronaEvent.put(EVENT_TYPE_KEY, adType);
                dispatchLuaEvent(coronaEvent);
            }
        }



        @Override
        public void onAdDisplayed(final MaxAd ad) {
            //Not  Banner
        }

        @Override
        public void onAdClicked(final MaxAd ad) {
            // send Corona Lua event
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_CLICKED);
            coronaEvent.put(EVENT_TYPE_KEY, adType);
            dispatchLuaEvent(coronaEvent);
        }

        @Override
        public void onAdLoadFailed(String adUnitId, MaxError error) {
            // send Corona Lua event
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_FAILED);
            coronaEvent.put(CoronaLuaEvent.ISERROR_KEY, true);
            coronaEvent.put(EVENT_TYPE_KEY, adType);
            coronaEvent.put(CoronaLuaEvent.RESPONSE_KEY, error.getMessage());
            dispatchLuaEvent(coronaEvent);
        }

        @Override
        public void onAdDisplayFailed(MaxAd ad, MaxError error) {
            // send Corona Lua event
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_FAILED);
            coronaEvent.put(CoronaLuaEvent.ISERROR_KEY, true);
            coronaEvent.put(EVENT_TYPE_KEY, adType);
            coronaEvent.put(CoronaLuaEvent.RESPONSE_KEY, error.getMessage());
            dispatchLuaEvent(coronaEvent);
        }


        @Override
        public void onAdExpanded(final MaxAd ad) {
            // send Corona Lua event
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_EXPANDED);
            coronaEvent.put(EVENT_TYPE_KEY, adType);
            dispatchLuaEvent(coronaEvent);

        }

        @Override
        public void onAdCollapsed(final MaxAd ad) {
            // send Corona Lua event
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_COLLAPSED);
            coronaEvent.put(EVENT_TYPE_KEY, adType);
            dispatchLuaEvent(coronaEvent);

        }
    }

    class MaxAdRevListenerInter implements MaxAdRevenueListener {

        String adType = TYPE_INTERSTITIAL;

        @Override
        public void onAdRevenuePaid(final MaxAd ad)
        {

            final Context coronaContext = CoronaEnvironment.getApplicationContext();

            double revenue = ad.getRevenue(); // In USD

            String revenuePrecision = ad.getRevenuePrecision();

            String countryCode = AppLovinSdk.getInstance( coronaContext ).getConfiguration().getCountryCode(); // "US" for the United States, etc - Note: Do not confuse this with currency code which is "USD" in most cases!

            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_RECEIVED_IMPRESSION_DATA);
            coronaEvent.put(EVENT_REVENUE_AD_REVENUE_VALUE_KEY, revenue);
            coronaEvent.put(EVENT_REVENUE_AD_SOURCE_KEY, ad.getNetworkName());
            coronaEvent.put(EVENT_REVENUE_AD_FORMAT_KEY, adType);
            coronaEvent.put(EVENT_REVENUE_AD_UNIT_NAME_KEY, ad.getAdUnitId());
            coronaEvent.put(EVENT_REVENUE_AD_PLACEMENT_NAME_KEY, ad.getPlacement());
            coronaEvent.put(EVENT_REVENUE_AD_IMPRESSION_ID_KEY, ad.getNetworkPlacement());
            coronaEvent.put(EVENT_REVENUE_AD_COUNTRY_CODE_KEY, countryCode);
            coronaEvent.put(EVENT_REVENUE_AD_REV_PRECISION_KEY, revenuePrecision);
            dispatchLuaEvent(coronaEvent);


        }
    }

    class MaxAdRevListenerReward implements MaxAdRevenueListener {

        String adType = TYPE_REWARDEDVIDEO;

        @Override
        public void onAdRevenuePaid(final MaxAd ad)
        {

            final Context coronaContext = CoronaEnvironment.getApplicationContext();

            double revenue = ad.getRevenue(); // In USD

            String revenuePrecision = ad.getRevenuePrecision();

            String countryCode = AppLovinSdk.getInstance( coronaContext ).getConfiguration().getCountryCode(); // "US" for the United States, etc - Note: Do not confuse this with currency code which is "USD" in most cases!

            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_RECEIVED_IMPRESSION_DATA);
            coronaEvent.put(EVENT_REVENUE_AD_REVENUE_VALUE_KEY, revenue);
            coronaEvent.put(EVENT_REVENUE_AD_SOURCE_KEY, ad.getNetworkName());
            coronaEvent.put(EVENT_REVENUE_AD_FORMAT_KEY, adType);
            coronaEvent.put(EVENT_REVENUE_AD_UNIT_NAME_KEY, ad.getAdUnitId());
            coronaEvent.put(EVENT_REVENUE_AD_PLACEMENT_NAME_KEY, ad.getPlacement());
            coronaEvent.put(EVENT_REVENUE_AD_IMPRESSION_ID_KEY, ad.getNetworkPlacement());
            coronaEvent.put(EVENT_REVENUE_AD_COUNTRY_CODE_KEY, countryCode);
            coronaEvent.put(EVENT_REVENUE_AD_REV_PRECISION_KEY, revenuePrecision);
            dispatchLuaEvent(coronaEvent);


        }
    }

    class MaxAdRevListenerBanner implements MaxAdRevenueListener {

        String adType = TYPE_BANNER;

        @Override
        public void onAdRevenuePaid(final MaxAd ad)
        {

            final Context coronaContext = CoronaEnvironment.getApplicationContext();

            double revenue = ad.getRevenue(); // In USD

            String revenuePrecision = ad.getRevenuePrecision();

            String countryCode = AppLovinSdk.getInstance( coronaContext ).getConfiguration().getCountryCode(); // "US" for the United States, etc - Note: Do not confuse this with currency code which is "USD" in most cases!

            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_RECEIVED_IMPRESSION_DATA);
            coronaEvent.put(EVENT_REVENUE_AD_REVENUE_VALUE_KEY, revenue);
            coronaEvent.put(EVENT_REVENUE_AD_SOURCE_KEY, ad.getNetworkName());
            coronaEvent.put(EVENT_REVENUE_AD_FORMAT_KEY, adType);
            coronaEvent.put(EVENT_REVENUE_AD_UNIT_NAME_KEY, ad.getAdUnitId());
            coronaEvent.put(EVENT_REVENUE_AD_PLACEMENT_NAME_KEY, ad.getPlacement());
            coronaEvent.put(EVENT_REVENUE_AD_IMPRESSION_ID_KEY, ad.getNetworkPlacement());
            coronaEvent.put(EVENT_REVENUE_AD_COUNTRY_CODE_KEY, countryCode);
            coronaEvent.put(EVENT_REVENUE_AD_REV_PRECISION_KEY, revenuePrecision);
            dispatchLuaEvent(coronaEvent);


        }
    }
}
