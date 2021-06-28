package io.revealbi.sdk.ext.api.oauth;

public class OAuthTokenRepositoryFactory {
	private static IOAuthTokenRepository instance;
	
	public static IOAuthTokenRepository getInstance() {
		return instance;
	}
	
	public static void setInstance(IOAuthTokenRepository instance) {
		OAuthTokenRepositoryFactory.instance = instance;
	}
	
}
