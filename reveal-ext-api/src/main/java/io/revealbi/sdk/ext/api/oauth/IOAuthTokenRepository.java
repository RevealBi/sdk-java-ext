package io.revealbi.sdk.ext.api.oauth;

import java.io.IOException;

public interface IOAuthTokenRepository {
	OAuthToken getToken(String userId, String dataSourceId, OAuthProviderType provider) throws IOException;
	void saveToken(String userId, String dataSourceId, OAuthProviderType provider, OAuthToken token) throws IOException;
}
