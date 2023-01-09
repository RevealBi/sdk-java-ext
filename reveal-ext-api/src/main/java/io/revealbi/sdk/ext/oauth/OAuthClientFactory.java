package io.revealbi.sdk.ext.oauth;

import io.revealbi.sdk.ext.api.oauth.OAuthProviderType;

public class OAuthClientFactory {
	public static OAuthClient getClient(OAuthProviderType provider) {
		switch (provider) {
		case GOOGLE_ANALYTICS:
		case GOOGLE_ANALYTICS_4:
		case GOOGLE_BIG_QUERY:
		case GOOGLE_DRIVE:
		case GOOGLE_SEARCH_CONSOLE:
			return new GoogleOAuthClient();
		case ONE_DRIVE:
			return new OneDriveOAuthClient();
		case DROPBOX:
			return new DropboxOAuthClient();
		case BOX:
			return new BoxOAuthClient();
		default:
			throw new RuntimeException("Client not found for " + provider);
		}
	}
}
