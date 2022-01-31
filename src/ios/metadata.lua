local metadata =
{
	plugin =
	{
		format = "staticLibrary",
		staticLibs = { "plugin_applovin", "z" },
		frameworks = { "AdSupport", "StoreKit", "AppLovinSDK", "CoreTelephony" },
		frameworksOptional = { "WebKit", "AppTrackingTransparency" }
	}
}

return metadata
