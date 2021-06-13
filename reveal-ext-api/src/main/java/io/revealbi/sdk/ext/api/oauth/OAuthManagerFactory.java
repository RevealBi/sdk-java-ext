package io.revealbi.sdk.ext.api.oauth;

import io.revealbi.sdk.ext.oauth.BaseOAuthManager;

public class OAuthManagerFactory {
	private static IOAuthManager instance = new BaseOAuthManager();
	
	public static void setInstance(IOAuthManager instance) {
		OAuthManagerFactory.instance = instance;
	}
	
	public static IOAuthManager getInstance() {
		return instance;
	}
}
