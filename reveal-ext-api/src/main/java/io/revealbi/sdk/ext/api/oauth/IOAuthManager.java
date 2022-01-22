package io.revealbi.sdk.ext.api.oauth;

import java.io.IOException;
import java.util.Map;

import com.infragistics.reveal.sdk.api.RVBearerTokenDataSourceCredential;

/**
 * OAuth manager interface, usually the default implementation should cover your needs and you shouldn't need 
 * to have your own implementation.
 * This class takes care of the supporting the authentication flow driven by OAuthResource and also delegating
 * the storage methods to the implementation of {@link IOAuthTokenRepository}, the one that you might
 * need to implement.
 */
public interface IOAuthManager {
	OAuthProviderSettings getProviderSettings(OAuthProviderType providerType);
	RVBearerTokenDataSourceCredential resolveCredentials(String userId, String dataSourceId, OAuthProviderType provider);

	Map<String, Object> getUserInfo(String userId, OAuthProviderType provider, String tokenId) throws IOException;
	void saveToken(String userId, OAuthProviderType provider, OAuthToken token) throws IOException;
	OAuthToken getToken(String userId, OAuthProviderType providerType, String tokenId) throws IOException;
	void setDataSourceToken(String userId, String dataSourceId, String tokenId, OAuthProviderType provider) throws IOException;
	void dataSourceDeleted(String userId, String dataSourceId, OAuthProviderType provider) throws IOException;
	void deleteToken(String userId, String tokenId, OAuthProviderType provider) throws IOException;
	
	/**
	 * Registers an OAuth provider, including settings needed for the OAuth flow: clientId, clientSecret and redirect URI.
	 * @param provider The OAuth provider, as defined in {@link OAuthProviderType}.
	 * @param clientId The OAuth client id.
	 * @param clientSecret The OAuth client secret.
	 * @param redirectUri The redirect URI, should be in this format: https://{server}/{app}/reveal-api/oauth/GOOGLE_ANALYTICS/callback where
	 * {server} and {app} must be replaced with the values for your application and deployment location.
	 */
	void registerProvider(OAuthProviderType provider, String clientId, String clientSecret, String redirectUri);
	
	void setOAuthStateProvider(IOAuthStateProvider provider);
	IOAuthStateProvider getOAuthStateProvider();
}
