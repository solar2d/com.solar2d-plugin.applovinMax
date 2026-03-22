//
//  CoronaApplication.java
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

package com.mycompany.app;

/**
 * Extends the Application class to receive Corona runtime events and to extend the Lua API.
 * <p>
 * Only one instance of this class will be created by the Android OS. It will be created before this application's
 * activity is displayed and will persist after the activity is destroyed. The name of this class must be set in the
 * AndroidManifest.xml file's "application" tag or else an instance of this class will not be created on startup.
 */
public class CoronaApplication extends android.app.Application {
	/** Called when your application has started. */
	@Override
	public void onCreate() {
		super.onCreate();
		// Set up a Corona runtime listener used to add custom APIs to Lua.
		com.ansca.corona.CoronaEnvironment.addRuntimeListener(new CoronaApplication.CoronaRuntimeEventHandler());
	}

	/** Receives and handles Corona runtime events. */
	private class CoronaRuntimeEventHandler implements com.ansca.corona.CoronaRuntimeListener {
		@Override
		public void onLoaded(com.ansca.corona.CoronaRuntime runtime) {
		}

		@Override
		public void onStarted(com.ansca.corona.CoronaRuntime runtime) {
		}

		@Override
		public void onSuspended(com.ansca.corona.CoronaRuntime runtime) {
		}

		@Override
		public void onResumed(com.ansca.corona.CoronaRuntime runtime) {
		}

		@Override
		public void onExiting(com.ansca.corona.CoronaRuntime runtime) {
		}
	}
}
