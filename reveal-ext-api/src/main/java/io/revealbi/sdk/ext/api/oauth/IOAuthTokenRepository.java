package io.revealbi.sdk.ext.api.oauth;

import java.io.IOException;

public interface IOAuthTokenRepository {
	OAuthToken getToken(String userId, String tokenId, OAuthProviderType provider) throws IOException;
	void saveToken(String userId, OAuthProviderType provider, OAuthToken token) throws IOException;
	
	void setDataSourceToken(String userId, String dataSourceId, String tokenId, OAuthProviderType provider) throws IOException;
	OAuthToken getDataSourceToken(String userId, String dataSourceId, OAuthProviderType provider) throws IOException;
	void deleteToken(String userId, String tokenId, OAuthProviderType provider) throws IOException;
}
