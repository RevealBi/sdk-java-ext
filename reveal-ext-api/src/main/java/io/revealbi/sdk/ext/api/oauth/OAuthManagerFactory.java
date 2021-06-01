package io.revealbi.sdk.ext.api.oauth;

public class OAuthManagerFactory {
	private static IOAuthManager instance;
	
	public static void setInstance(IOAuthManager instance) {
		OAuthManagerFactory.instance = instance;
	}
	
	public static IOAuthManager getInstance() {
		return instance;
	}
}
