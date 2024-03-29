package io.revealbi.sdk.ext.api.oauth;

import com.infragistics.reportplus.datalayer.api.ProviderKeys;

public enum OAuthProviderType {
	GOOGLE_ANALYTICS(ProviderKeys.googleAnalyticsProviderKey),
	GOOGLE_BIG_QUERY(ProviderKeys.bigQueryProviderKey),
	GOOGLE_DRIVE(ProviderKeys.googleDriveProviderKey),
	ONE_DRIVE(ProviderKeys.oneDriveProviderKey),
	DROPBOX(ProviderKeys.dropboxProviderKey),
	BOX(ProviderKeys.boxProviderKey),
	GOOGLE_SEARCH_CONSOLE(ProviderKeys.googleSearchProviderKey),
	GOOGLE_ANALYTICS_4(ProviderKeys.googleAnalytics4ProviderKey);
	
	private String providerId;
	private OAuthProviderType(String providerId) {
		this.providerId = providerId;
	}
	
	public String getProviderId() {
		return providerId;
	}
}
