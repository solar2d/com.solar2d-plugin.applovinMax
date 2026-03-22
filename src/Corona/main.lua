--
-- Abstract: AppLovin Free Sample app
-- Version: 1.0
-- Sample code is MIT licensed; see https://www.coronalabs.com/links/code/license
---------------------------------------------------------------------------------------

local widget = require( "widget" )
local applovin = require( "plugin.applovinMax" )
local json = require("json")

display.setStatusBar( display.HiddenStatusBar )

local processEventTable = function(event)
  local logString = json.prettify(event):gsub("\\","")
  logString = "\nPHASE: "..event.phase.." - - - - - - - - - - - -\n" .. logString
  print(logString)
  return logString
end

-- forward declarations
local onKeyEvent = nil
local eventText = nil
local buttonHeight = 50
local bannerButton
local interstitialButton
local rewardedVideoButton

-- Create the background
local background = display.newImageRect("back-whiteorange.png", display.actualContentWidth, display.actualContentHeight)
background.x = display.contentCenterX
background.y = display.contentCenterY

adTypeText = display.newText {
  text = "Press a button below",
  font = native.systemFont,
  fontSize = 16,
  align = "left",
  width = 320,
  height = 20,
}
adTypeText.anchorX = 0
adTypeText.anchorY = 0
adTypeText.x = display.screenOriginX + 5
adTypeText.y = display.screenOriginY + 5
adTypeText:setFillColor(0)

-- Create a text object to show the Lua listener events on screen
local eventText = display.newText{
  text = "",
  font = native.systemFont,
  fontSize = 12,
  align = "left",
  width = 310,
  height = 200,
}
eventText.anchorX = 0
eventText.anchorY = 0
eventText.x = display.screenOriginX + 5
eventText.y = adTypeText.y + adTypeText.height + 5
eventText:setFillColor(0)

-- AppLovin listener
local function applovinListener( event )
  eventText.text = processEventTable(event)

  if event.phase == "init" then
    print("setting user details")
    applovin.setUserDetails({userId = "corona"})
  elseif event.phase == "loaded" then
    if event.type == "banner" then
      bannerButton:setLabel("Show banner")
    elseif event.type == "interstitial" then
      interstitialButton:setLabel("Show interstitial")
    elseif event.type == "rewardedVideo" then
      rewardedVideoButton:setLabel("Show rewardedVideo")
    end
  elseif event.phase == "displayed" then
    if event.type == "banner" then
      bannerButton:setLabel("Hide Banner")
    end
  elseif event.phase == "hidden" then
    if event.type == "banner" then
      bannerButton:setLabel("Load Banner")
    end
  end
end



local applovinInitialized = false

local function initApplovin()
    if applovinInitialized then return end
    applovinInitialized = true
    local sdkKey
    if system.getInfo("platform") == "android" then
        sdkKey = "YOUR_ANDROID_SDK_KEY"
    elseif system.getInfo("platform") == "ios" then
        sdkKey = "replace with your own"
    else
        sdkKey = ""
    end
    applovin.init( applovinListener, {
        verboseLogging=true,
        sdkKey = sdkKey,
        testDeviceIds = {"40DA725F-5936-46AF-B6D3-8D17F41E76D7"}
    } )
end

-- Request ATT before initializing ads
if system.getInfo("platform") == "ios" then
    local att = require("plugin.att")

    local function onATTResponse(event)
        print("ATT status: " .. tostring(event.status))
        initApplovin()
    end

    -- ATT dialog must be shown after app is fully active
    -- Delay request to next frame to ensure app is in foreground
    timer.performWithDelay(500, function()
        local currentStatus = att.status
        print("ATT current status: " .. tostring(currentStatus))
        if currentStatus == "notDetermined" then
            att.request(onATTResponse)
        else
            -- Already determined (authorized/denied/restricted), just init
            print("ATT already determined: " .. tostring(currentStatus))
            initApplovin()
        end
    end)
else
    initApplovin()
end


bannerButton = widget.newButton {
  label = "Load banner",
  width = 250,
  height = buttonHeight,
  onRelease = function(event)
    if bannerButton:getLabel() == "Hide Banner" then
      applovin.hide("banner")
    elseif applovin.isLoaded("banner") then
      applovin.show("banner", {y="top"})
    else
      bannerButton:setLabel("Loading banner...")
      applovin.load("banner", {iOSUnitId ="replace with your own", androidUnitId="replace with your own", bannerSize="standard"})
    end
  end
}
bannerButton.x = display.contentCenterX
bannerButton.y = eventText.y + eventText.height + 10

interstitialButton = widget.newButton{
  label = "Load Interstitial",
  width = 250,
  height = buttonHeight,
  onRelease = function(event)
    if applovin.isLoaded("interstitial") then
      interstitialButton:setLabel("Load interstitial")
      applovin.show("interstitial")
    else
      interstitialButton:setLabel("Loading interstitial...")
      applovin.load("interstitial", {iOSUnitId = "replace with your own", androidUnitId="replace with your own"})
    end
  end,
}
interstitialButton.x = display.contentCenterX
interstitialButton.y = bannerButton.y + bannerButton.height * 1.15

rewardedVideoButton = widget.newButton {
  label = "Load Rewarded Video",
  width = 250,
  height = buttonHeight,
  onRelease = function(event)
    if applovin.isLoaded("rewardedVideo") then
      rewardedVideoButton:setLabel("Load rewardedVideo")
      applovin.show("rewardedVideo")
    else
      rewardedVideoButton:setLabel("Loading rewardedVideo...")
      applovin.load("rewardedVideo", {iOSUnitId="replace with your own", androidUnitId="replace with your own"})
    end
  end,
}
rewardedVideoButton.x = display.contentCenterX
rewardedVideoButton.y = interstitialButton.y + interstitialButton.height * 1.15
