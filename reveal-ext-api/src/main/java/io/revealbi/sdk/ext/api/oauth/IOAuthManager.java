package io.revealbi.sdk.ext.api.oauth;

import java.io.IOException;

import com.infragistics.reveal.sdk.api.RVBearerTokenDataSourceCredential;

public interface IOAuthManager {
	OAuthProviderSettings getProviderSettings(OAuthProviderType providerType);
	RVBearerTokenDataSourceCredential getToken(String userId, String dataSourceId, OAuthProviderType provider);
	void saveToken(String userId, String dataSourceId, OAuthProviderType provider, OAuthToken token) throws IOException;
}
