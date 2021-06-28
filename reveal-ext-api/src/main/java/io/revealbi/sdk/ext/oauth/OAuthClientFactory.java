package io.revealbi.sdk.ext.oauth;

import io.revealbi.sdk.ext.api.oauth.OAuthProviderType;

public class OAuthClientFactory {
	public static OAuthClient getClient(OAuthProviderType provider) {
		switch (provider) {
		case GOOGLE_ANALYTICS:
		case GOOGLE_BIG_QUERY:
			return new GoogleOAuthClient();
		default:
			throw new RuntimeException("Client not found for " + provider);
		}
	}
}
