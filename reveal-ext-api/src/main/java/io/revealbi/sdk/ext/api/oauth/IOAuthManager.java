package io.revealbi.sdk.ext.api.oauth;

import java.io.IOException;
import java.util.Map;

import com.infragistics.reveal.sdk.api.RVBearerTokenDataSourceCredential;

public interface IOAuthManager {
	OAuthProviderSettings getProviderSettings(OAuthProviderType providerType);
	RVBearerTokenDataSourceCredential resolveCredentials(String userId, String dataSourceId, OAuthProviderType provider);

	Map<String, Object> getUserInfo(String userId, OAuthProviderType provider, String tokenId) throws IOException;
	void saveToken(String userId, OAuthProviderType provider, OAuthToken token) throws IOException;
	void setDataSourceToken(String userId, String dataSourceId, String tokenId, OAuthProviderType provider) throws IOException;
	void deleteToken(String userId, String tokenId, OAuthProviderType provider) throws IOException;
	
	void registerProvider(OAuthProviderType provider, String clientId, String clientSecret, String redirectUri);
}
