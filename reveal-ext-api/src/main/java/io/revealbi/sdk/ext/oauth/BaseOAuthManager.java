package io.revealbi.sdk.ext.oauth;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.infragistics.reveal.sdk.api.IRVUserContext;
import com.infragistics.reveal.sdk.api.RVBearerTokenDataSourceCredential;

import io.revealbi.sdk.ext.api.oauth.IOAuthManager;
import io.revealbi.sdk.ext.api.oauth.IOAuthStateProvider;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderSettings;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderType;
import io.revealbi.sdk.ext.api.oauth.OAuthToken;
import io.revealbi.sdk.ext.api.oauth.OAuthTokenRepositoryFactory;
import io.revealbi.sdk.ext.oauth.TokenLock.Lock;

public class BaseOAuthManager implements IOAuthManager {
	private static Logger log = Logger.getLogger(BaseOAuthManager.class.getName());
	
	private Map<OAuthProviderType, OAuthProviderSettings> map = Collections.synchronizedMap(new HashMap<OAuthProviderType, OAuthProviderSettings>());
	private TokenLock tokenLock = new TokenLock();
	private IOAuthStateProvider stateProvider;
	
	public BaseOAuthManager() {
		setOAuthStateProvider(new BaseOAuthStateProvider());
	}
	
	@Override
	public OAuthProviderSettings getProviderSettings(OAuthProviderType providerType) {
		return map.get(providerType);
	}

	@Override
	public RVBearerTokenDataSourceCredential resolveCredentials(IRVUserContext userContext, String dataSourceId, OAuthProviderType provider) {
		try {
			OAuthToken token = getRefreshedToken(userContext, dataSourceId, provider);
			if (token == null) {
				return null;
			}
			
			String oauthUserId = getOAuthUserId(token, provider, userContext != null ? userContext.getUserId() : null);
			
			return new RVBearerTokenDataSourceCredential(token.getAccessToken(), oauthUserId);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to resolve OAuth token:" + e, e);
			return null;
		}
	}
	
	private static String getOAuthUserId(OAuthToken token, OAuthProviderType provider, String defaultUserIdValue) {
		String userId = null;
		Map<String, Object> userInfo = token.getUserInfo();
		if (userInfo != null) {
			switch (provider) {
			case GOOGLE_ANALYTICS:
			case GOOGLE_ANALYTICS_4:
			case GOOGLE_BIG_QUERY:
			case GOOGLE_DRIVE:
			case GOOGLE_SEARCH_CONSOLE:
				userId = GoogleOAuthClient.GoogleUserInfo.getUserId(userInfo);
				break;
			case ONE_DRIVE:
				userId = new OneDriveOAuthClient.OneDriveUserInfo(userInfo).getUserId();
				break;
			case DROPBOX:
				userId = DropboxOAuthClient.DropboxUserInfo.getUserId(userInfo); 
				break;
			case BOX:
				userId = BoxOAuthClient.BoxUserInfo.getUserId(userInfo);
				break;
			}
		}
		return userId != null ? userId : defaultUserIdValue;
	}

	@Override
	public Map<String, Object> getUserInfo(IRVUserContext userContext, OAuthProviderType provider, String tokenId) throws IOException {
		OAuthToken token = OAuthTokenRepositoryFactory.getInstance().getToken(userContext, tokenId, provider);
		return token == null ? null : token.getUserInfo();
	}
	
	protected OAuthToken getDataSourceToken(IRVUserContext userContext, String dataSourceId, OAuthProviderType provider) throws IOException {		
		return OAuthTokenRepositoryFactory.getInstance().getDataSourceToken(userContext, dataSourceId, provider);
	}
	
	@Override
	public void saveToken(IRVUserContext userContext, OAuthProviderType provider, OAuthToken token) throws IOException {
		OAuthTokenRepositoryFactory.getInstance().saveToken(userContext, provider, token);
	}
	
	@Override
	public OAuthToken getToken(IRVUserContext userContext, OAuthProviderType provider, String tokenId) throws IOException {
		return OAuthTokenRepositoryFactory.getInstance().getToken(userContext, tokenId, provider);
	}
	
	@Override
	public void deleteToken(IRVUserContext userContext, String tokenId, OAuthProviderType provider) throws IOException {
		OAuthTokenRepositoryFactory.getInstance().deleteToken(userContext, tokenId, provider);
	}
	
	@Override
	public void setDataSourceToken(IRVUserContext userContext, String dataSourceId, String tokenId, OAuthProviderType provider) throws IOException {
		OAuthTokenRepositoryFactory.getInstance().setDataSourceToken(userContext, dataSourceId, tokenId, provider);
	}
	
	@Override
	public void dataSourceDeleted(IRVUserContext userContext, String dataSourceId, OAuthProviderType provider) throws IOException {
		OAuthTokenRepositoryFactory.getInstance().dataSourceDeleted(userContext, dataSourceId, provider);
	}
	
	public void registerProviderSettings(OAuthProviderSettings settings) {
		map.put(settings.getProviderType(), settings);
	}
	
	@Override
	public void setOAuthStateProvider(IOAuthStateProvider provider) {
		if (provider == null) {
			throw new NullPointerException("OAuthStateProvider is required");
		}
		this.stateProvider = provider;
	}

	@Override
	public IOAuthStateProvider getOAuthStateProvider() {
		return stateProvider;
	}

	
	@Override
	public void registerProvider(OAuthProviderType provider, String clientId, String clientSecret, String redirectUri) {
		switch (provider) {
		case GOOGLE_ANALYTICS:
			registerProviderSettings(OAuthProviderSettings.createGoogleAnalyticsSettings(clientId, clientSecret, redirectUri));
			break;
		case GOOGLE_ANALYTICS_4:
			registerProviderSettings(OAuthProviderSettings.createGoogleAnalytics4Settings(clientId, clientSecret, redirectUri));
			break;
		case GOOGLE_BIG_QUERY:
			registerProviderSettings(OAuthProviderSettings.createBigQuerySettings(clientId, clientSecret, redirectUri));
			break;
		case GOOGLE_DRIVE:
			registerProviderSettings(OAuthProviderSettings.createGoogleDriveSettings(clientId, clientSecret, redirectUri));
			break;
		case ONE_DRIVE:
			registerProviderSettings(OAuthProviderSettings.createOneDriveSettings(clientId, clientSecret, redirectUri));
			break;
		case DROPBOX:
			registerProviderSettings(OAuthProviderSettings.createDropboxSettings(clientId, clientSecret, redirectUri));
			break;
		case BOX:
			registerProviderSettings(OAuthProviderSettings.createBoxSettings(clientId, clientSecret, redirectUri));
			break;
		case GOOGLE_SEARCH_CONSOLE:
			registerProviderSettings(OAuthProviderSettings.createGoogleSearchConsoleSettings(clientId, clientSecret, redirectUri));
			break;
		default:
			break;
		}
	}

	/**
	 * Returns true if the token is expired or about to expire and must be refreshed. 
	 * By default checks that the expirationTime + the grace period ({@link #getExpirationGracePeriod()}) is not in the past.
	 * @param token The token to check if expired
	 * @return True if the token is expired.
	 */
	protected boolean isTokenExpired(OAuthToken token) {
		if (token.getExpiration() == 0) return false;
		return (token.getExpiration() + getExpirationGracePeriod()) < System.currentTimeMillis();
	}
	
	/**
	 * Time before actual expiration that a token is considered expired, this is to 
	 * avoid issues deciding to use a token that expires while being used.
	 * Expressed in milliseconds, defaults to 60000 (1 minute).
	 * @return Time before actual expiration that a token is considered expired.
	 */
	protected long getExpirationGracePeriod() {
		return 60000;
	}
	
	/**
	 * Refreshes the token, must save the token and update refresh and access tokens in the token parameter.
	 * @param userId Id of the user this token is associated with.
	 * @param dataSourceId Id of the data source linked to the token.
	 * @param provider The OAuth provider this token belongs to.
	 * @param token The token to refresh.
	 * @throws IOException If an I/O error occurs refreshing the token 
	 */
	protected void refreshToken(IRVUserContext userContext, String dataSourceId, OAuthProviderType provider, OAuthToken token) throws IOException {
		String userId = userContext != null ? userContext.getUserId() : null;
		log.info("RefreshToken requested for " + getCacheKey(userId, dataSourceId, provider));
		OAuthProviderSettings settings = getProviderSettings(provider);
		if (settings == null) {
			return;
		}
		OAuthClient client = OAuthClientFactory.getClient(provider);
		OAuthTokenResponse response = client.refreshToken(settings, token.getRefreshToken());
		if (response.getError() != null) {
			log.severe("Failed to refresh token: " + response.getError());
			return;
		}
		String newAccessToken = response.getAccessToken();
		long expiresIn = response.getExpiresIn();
		
		if (newAccessToken == null) {
			log.severe("Failed to refresh token, no access token received.");
			return;
		}
		if (expiresIn <= 0) {
			log.severe("Failed to refresh token, no expiresIn received.");
			return;
		}
		token.refreshed(newAccessToken, OAuthClient.getExpirationTimeForToken(expiresIn));
		saveToken(userContext, provider, token);
		log.info("RefreshToken completed for " + getCacheKey(userId, dataSourceId, provider));

	}

	private OAuthToken getRefreshedToken(IRVUserContext userContext, String dataSourceId, OAuthProviderType provider) throws IOException {
		String cacheKey = getCacheKey(userContext != null ? userContext.getUserId() : null, dataSourceId, provider);
		Lock lock = tokenLock.getLockObject(cacheKey);
		try {
			synchronized (lock) {
				OAuthToken token = getDataSourceToken(userContext, dataSourceId, provider);
				if (token == null) {
					return null;
				}
				if (isTokenExpired(token)) {
					refreshToken(userContext, dataSourceId, provider, token);
				}							
				return token;
			}
		} finally {
			tokenLock.releaseLock(cacheKey, lock);
		}
	}
	
	protected static String getCacheKey(String userId, String dataSourceId, OAuthProviderType provider) {
		return userId + ":" + dataSourceId + ":" + provider;
	}
	
}
