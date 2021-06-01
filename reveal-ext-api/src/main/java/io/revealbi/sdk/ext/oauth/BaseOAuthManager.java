package io.revealbi.sdk.ext.oauth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.revealbi.sdk.ext.api.oauth.IOAuthManager;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderSettings;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderType;

public class BaseOAuthManager implements IOAuthManager {
	private Map<OAuthProviderType, OAuthProviderSettings> map = Collections.synchronizedMap(new HashMap<OAuthProviderType, OAuthProviderSettings>());
	
	@Override
	public OAuthProviderSettings getProviderSettings(OAuthProviderType providerType) {
		return map.get(providerType);
	}

	public void registerProviderSettings(OAuthProviderSettings settings) {
		map.put(settings.getProviderType(), settings);
	}
	
	public void registerGoogleAnalytics(String clientId, String clientSecret, String redirectUri) {
		registerProviderSettings(OAuthProviderSettings.createGoogleAnalyticsSettings(clientId, clientSecret, redirectUri));
	}
	
	public void registerBigQuery(String clientId, String clientSecret, String redirectUri) {
		registerProviderSettings(OAuthProviderSettings.createBigQuerySettings(clientId, clientSecret, redirectUri));
	}

}
