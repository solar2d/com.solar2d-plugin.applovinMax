local metadata =
{
	plugin =
	{
		format = "staticLibrary",
		staticLibs = { "plugin_applovinMax", "z" },
		frameworks = { "AdSupport", "StoreKit", "CoreTelephony", "AppLovinSDK" },
		frameworksOptional = { "WebKit", "AppTrackingTransparency" },
		usesSwift = true
	}
}

return metadata
