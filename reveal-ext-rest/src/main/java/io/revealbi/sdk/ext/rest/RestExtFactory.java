package io.revealbi.sdk.ext.rest;

import com.infragistics.reveal.engine.init.RevealEngineInitializer;

public class RestExtFactory {
	public static void registerAllResources() {
		RevealEngineInitializer.registerResource(DashboardsResource.class);
		RevealEngineInitializer.registerResource(DataSourcesResource.class);
		RevealEngineInitializer.registerResource(CredentialsResource.class);
		RevealEngineInitializer.registerResource(OAuthResource.class);
	}
}
