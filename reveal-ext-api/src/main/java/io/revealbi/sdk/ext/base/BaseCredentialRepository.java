package io.revealbi.sdk.ext.base;

import java.io.IOException;

import com.infragistics.reportplus.datalayer.api.ProviderKeys;
import com.infragistics.reveal.sdk.api.IRVDataSourceCredential;
import com.infragistics.reveal.sdk.api.IRVUserContext;
import com.infragistics.reveal.sdk.api.model.RVBigQueryDataSource;
import com.infragistics.reveal.sdk.api.model.RVDashboardDataSource;
import com.infragistics.reveal.sdk.api.model.RVGoogleAnalyticsDataSource;
import com.infragistics.reveal.sdk.api.model.RVGoogleDriveDataSource;

import io.revealbi.sdk.ext.api.ICredentialRepository;
import io.revealbi.sdk.ext.api.oauth.IOAuthManager;
import io.revealbi.sdk.ext.api.oauth.OAuthManagerFactory;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderType;

/**
 * Base credential repository, handles resolution of OAuth credentials by delegating them to the current OAuthManager.
 * Regular (non-OAuth) credentials must be resolved by subclasses in the implementation of {@link BaseCredentialRepository#resolveRegularCredentials(String, RVDashboardDataSource)}
 */
public abstract class BaseCredentialRepository implements ICredentialRepository {
	@Override
	public IRVDataSourceCredential resolveCredentials(IRVUserContext userContext, RVDashboardDataSource dataSource) {
		String userId = userContext != null ? userContext.getUserId() : null;
		OAuthProviderType oauthProvider = getOAuthProvider(dataSource);
		if (oauthProvider != null) {
			IOAuthManager oauth = OAuthManagerFactory.getInstance();
			if (oauth != null) {
				return oauth.resolveCredentials(userContext.getUserId(), dataSource.getId(), oauthProvider);
			}
		}
		return resolveRegularCredentials(userId, dataSource);
	}
	
	@Override
	public void dataSourceDeleted(String userId, String dataSourceId, String provider, String uniqueIdentifier) throws IOException {
		setDataSourceCredentials(userId, uniqueIdentifier == null ? dataSourceId : uniqueIdentifier, null);
		OAuthProviderType oauthProvider = getOAuthProvider(provider);
		if (oauthProvider != null) {
			IOAuthManager oauth = OAuthManagerFactory.getInstance();
			if (oauth != null) {
				oauth.dataSourceDeleted(userId, dataSourceId, oauthProvider);
			}
		}
	}
	
	/**
	 * Invoked to resolve credentials for non-OAuth data sources, as OAuth data sources are resolved in this class by using the OAuthManagerFactory
	 * @param userId Id of the user to return credentials for
	 * @param dataSource Data source to return credentials for
	 * @return Credentials to be used for this data source, null if no credentials are configured.
	 */
	protected abstract IRVDataSourceCredential resolveRegularCredentials(String userId, RVDashboardDataSource dataSource);
	
	protected static OAuthProviderType getOAuthProvider(RVDashboardDataSource ds) {
		if (ds instanceof RVGoogleAnalyticsDataSource) {
			return OAuthProviderType.GOOGLE_ANALYTICS;
		} else if (ds instanceof RVBigQueryDataSource) {
			return OAuthProviderType.GOOGLE_BIG_QUERY;
		} else if (ds instanceof RVGoogleDriveDataSource) {
			return OAuthProviderType.GOOGLE_DRIVE;
		} else {
			return null;
		}
	}
	
	protected static OAuthProviderType getOAuthProvider(String provider) {
		if (provider == null) {
			return null;			
		}
		if (provider.equals(ProviderKeys.googleAnalyticsProviderKey)) {
			return OAuthProviderType.GOOGLE_ANALYTICS;
		} else if (provider.equals(ProviderKeys.bigQueryProviderKey)) {
			return OAuthProviderType.GOOGLE_BIG_QUERY;
		} else if (provider.equals(ProviderKeys.googleDriveProviderKey)) {
			return OAuthProviderType.GOOGLE_DRIVE;
		} else {
			return null;
		}
	}

}
