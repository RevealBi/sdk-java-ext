package io.revealbi.sdk.ext.api;

import io.revealbi.sdk.ext.auth.simple.DenyAllAuthorizationProvider;

public class AuthorizationProviderFactory {
	private static IAuthorizationProvider instance = new DenyAllAuthorizationProvider();
	
	public static IAuthorizationProvider getInstance() {
		return instance; 
	}
	
	public static void setInstance(IAuthorizationProvider instance) {
		AuthorizationProviderFactory.instance = instance;
	}
}
