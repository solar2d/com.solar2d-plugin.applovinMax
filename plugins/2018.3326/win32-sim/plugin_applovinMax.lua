-- Applovin plugin

local Library = require "CoronaLibrary"

-- Create library
local lib = Library:new{ name="plugin.applovinMax", publisherId="com.solar2d", version=1 }

-------------------------------------------------------------------------------
-- BEGIN
-------------------------------------------------------------------------------

-- This sample implements the following Lua:
--
--    local applovin = require "plugin.applovin"
--    applovin.init()
--

local function showWarning(functionName)
    print( functionName .. " WARNING: The Applovin plugin is only supported on iOS and Android. Please build for device")
end

function lib.init()
    showWarning("applovinMax.init()")
end

function lib.load()
    showWarning("applovinMax.load()")
end

function lib.isLoaded()
    showWarning("applovinMax.isLoaded()")
end

function lib.show()
    showWarning("applovinMax.show()")
end

function lib.setUserDetails()
    showWarning("applovinMax.setUserDetails()")
end

function lib.setIsAgeRestrictedUser()
    showWarning("applovinMax.setIsAgeRestrictedUser()")
end
function lib.showDebugger()
    showWarning("applovinMax.showDebugger()")
end



-------------------------------------------------------------------------------
-- END
-------------------------------------------------------------------------------

-- Return an instance
return lib
