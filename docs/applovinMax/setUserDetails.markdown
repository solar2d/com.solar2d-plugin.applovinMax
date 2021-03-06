# applovinMax.setUserDetails()

> --------------------- ------------------------------------------------------------------------------------------
> __Type__              [Function][api.type.Function]
> __Return value__      none
> __Revision__          [REVISION_LABEL](REVISION_URL)
> __Keywords__          ads, advertising, AppLovin, setUserDetails, Applovin Max
> __See also__          [applovin.init()][plugin.applovinMax.init]
>						[applovinMax.load()][plugin.applovinMax.load]
>						[applovinMax.isLoaded()][plugin.applovinMax.isLoaded]
>						[applovinMax.*][plugin.applovinMax]
> --------------------- ------------------------------------------------------------------------------------------


## Overview

Sets the user details to be passed to [AppLovin](https://support.applovin.com/support/home) for <nobr>server-side</nobr> callbacks.

<div class="guide-notebox">
<div class="notebox-title">Note</div>

This function is completely optional and only applies if you are using [AppLovin](https://support.applovin.com/support/home) <nobr>server-side</nobr> callbacks. You can find out more information on <nobr>server-side</nobr> callbacks at the [AppLovin developer portal](https://www.applovin.com/manage).

</div>


## Syntax

	applovinMax.setUserDetails( params )

##### params ~^(required)^~
_[Table][api.type.Table]._ Table containing AppLovin user detail values &mdash; see the next section for details.


## Parameter Reference

The `params` table includes parameters for the AppLovin user details.

##### userId ~^(optional)^~
_[String][api.type.String]._ The user ID you wish to set for the current user in your app. This can be any <nobr>non-empty</nobr> string value, for example `user123`.


## Example

``````lua
local applovinMax = require( "plugin.applovinMax" )

local function adListener( event )

	if ( event.phase == "init" ) then  -- Successful initialization
		-- Set the user details
		applovinMax.setUserDetails( { userId="user123" } )
	end
end

-- Initialize the AppLovin plugin
applovinMax.init( adListener )
``````
