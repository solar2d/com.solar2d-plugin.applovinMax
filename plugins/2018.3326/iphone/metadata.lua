local metadata =
{
	plugin =
	{
		format = "staticLibrary",
		staticLibs = { "plugin_applovinMax", "z" },
		frameworks = { "AdSupport", "StoreKit", "AppLovinSDK", "CoreTelephony" },
		frameworksOptional = { "WebKit", "AppTrackingTransparency" },
		usesSwift = true
	}
}

return metadata
