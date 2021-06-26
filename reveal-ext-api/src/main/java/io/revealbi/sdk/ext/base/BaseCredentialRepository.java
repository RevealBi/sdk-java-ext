package io.revealbi.sdk.ext.base;

import com.infragistics.reveal.sdk.api.IRVDataSourceCredential;
import com.infragistics.reveal.sdk.api.model.RVBigQueryDataSource;
import com.infragistics.reveal.sdk.api.model.RVDashboardDataSource;
import com.infragistics.reveal.sdk.api.model.RVGoogleAnalyticsDataSource;

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
	public IRVDataSourceCredential resolveCredentials(String userId, RVDashboardDataSource dataSource) {
		OAuthProviderType oauthProvider = getOAuthProvider(dataSource);
		if (oauthProvider != null) {
			IOAuthManager oauth = OAuthManagerFactory.getInstance();
			if (oauth != null) {
				return oauth.resolveCredentials(userId, dataSource.getId(), oauthProvider);
			}
		}
		return resolveRegularCredentials(userId, dataSource);
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
		} else {
			return null;
		}
	}

}