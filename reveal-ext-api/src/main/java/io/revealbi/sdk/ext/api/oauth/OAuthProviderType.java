package io.revealbi.sdk.ext.api.oauth;

import com.infragistics.reportplus.datalayer.api.ProviderKeys;

public enum OAuthProviderType {
	GOOGLE_ANALYTICS(ProviderKeys.googleAnalyticsProviderKey),
	GOOGLE_BIG_QUERY(ProviderKeys.bigQueryProviderKey),
	GOOGLE_DRIVE(ProviderKeys.googleDriveProviderKey);
	
	private String providerId;
	private OAuthProviderType(String providerId) {
		this.providerId = providerId;
	}
	
	public String getProviderId() {
		return providerId;
	}
}
