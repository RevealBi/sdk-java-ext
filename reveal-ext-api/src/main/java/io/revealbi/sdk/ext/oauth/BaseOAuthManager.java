package io.revealbi.sdk.ext.oauth;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.infragistics.reveal.sdk.api.RVBearerTokenDataSourceCredential;

import io.revealbi.sdk.ext.api.oauth.IOAuthManager;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderSettings;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderType;
import io.revealbi.sdk.ext.api.oauth.OAuthToken;
import io.revealbi.sdk.ext.api.oauth.OAuthTokenRepositoryFactory;

public class BaseOAuthManager implements IOAuthManager {
	private static Logger log = Logger.getLogger(BaseOAuthManager.class.getName());
	
	private Map<OAuthProviderType, OAuthProviderSettings> map = Collections.synchronizedMap(new HashMap<OAuthProviderType, OAuthProviderSettings>());
	private Map<String, TokenLock> locks = new HashMap<String, BaseOAuthManager.TokenLock>();
	
	@Override
	public OAuthProviderSettings getProviderSettings(OAuthProviderType providerType) {
		return map.get(providerType);
	}

	@Override
	public RVBearerTokenDataSourceCredential resolveCredentials(String userId, String dataSourceId, OAuthProviderType provider) {
		try {
			OAuthToken token = getRefreshedToken(userId, dataSourceId, provider);
			if (token == null) {
				return null;
			}
			
			String oauthUserId = getOAuthUserId(token, provider, userId);
			
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
			case GOOGLE_BIG_QUERY:
			case GOOGLE_DRIVE:
				userId = GoogleOAuthClient.GoogleUserInfo.getUserId(userInfo);
				break;
			case ONE_DRIVE:
				userId = new OneDriveOAuthClient.OneDriveUserInfo(userInfo).getUserId();
				break;
			case DROPBOX:
				userId = DropboxOAuthClient.DropboxUserInfo.getUserId(userInfo); 
				break;
			case BOX:
				//XXXTODO
				break;
			}
		}
		return userId != null ? userId : defaultUserIdValue;
	}

	@Override
	public Map<String, Object> getUserInfo(String userId, OAuthProviderType provider, String tokenId) throws IOException {
		OAuthToken token = OAuthTokenRepositoryFactory.getInstance().getToken(userId, tokenId, provider);
		return token == null ? null : token.getUserInfo();
	}
	
	protected OAuthToken getDataSourceToken(String userId, String dataSourceId, OAuthProviderType provider) throws IOException {		
		return OAuthTokenRepositoryFactory.getInstance().getDataSourceToken(userId, dataSourceId, provider);
	}
	
	@Override
	public void saveToken(String userId, OAuthProviderType provider, OAuthToken token) throws IOException {
		OAuthTokenRepositoryFactory.getInstance().saveToken(userId, provider, token);
	}	
	
	@Override
	public void deleteToken(String userId, String tokenId, OAuthProviderType provider) throws IOException {
		OAuthTokenRepositoryFactory.getInstance().deleteToken(userId, tokenId, provider);
	}
	
	@Override
	public void setDataSourceToken(String userId, String dataSourceId, String tokenId, OAuthProviderType provider) throws IOException {
		OAuthTokenRepositoryFactory.getInstance().setDataSourceToken(userId, dataSourceId, tokenId, provider);
	}
	
	@Override
	public void dataSourceDeleted(String userId, String dataSourceId, OAuthProviderType provider) throws IOException {
		OAuthTokenRepositoryFactory.getInstance().dataSourceDeleted(userId, dataSourceId, provider);
	}
	
	public void registerProviderSettings(OAuthProviderSettings settings) {
		map.put(settings.getProviderType(), settings);
	}
	
	@Override
	public void registerProvider(OAuthProviderType provider, String clientId, String clientSecret, String redirectUri) {
		switch (provider) {
		case GOOGLE_ANALYTICS:
			registerProviderSettings(OAuthProviderSettings.createGoogleAnalyticsSettings(clientId, clientSecret, redirectUri));
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
	protected void refreshToken(String userId, String dataSourceId, OAuthProviderType provider, OAuthToken token) throws IOException {
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
		saveToken(userId, provider, token);
		log.info("RefreshToken completed for " + getCacheKey(userId, dataSourceId, provider));

	}

	private OAuthToken getRefreshedToken(String userId, String dataSourceId, OAuthProviderType provider) throws IOException {
		String cacheKey = getCacheKey(userId, dataSourceId, provider);
		TokenLock lock = getLockObject(cacheKey);
		try {
			synchronized (lock) {
				OAuthToken token = getDataSourceToken(userId, dataSourceId, provider);
				if (token == null) {
					return null;
				}
				if (isTokenExpired(token)) {
					refreshToken(userId, dataSourceId, provider, token);
				}							
				return token;
			}
		} finally {
			releaseLock(cacheKey, lock);
		}
	}
	
	private TokenLock getLockObject(String cacheKey) {
		TokenLock lock;
		synchronized (locks) {
			lock = locks.get(cacheKey);
			if (lock == null) {
				lock = new TokenLock();
				locks.put(cacheKey, lock);
			}
			lock.references++;
		}
		return lock;
	}
	
	private void releaseLock(String cacheKey, TokenLock lock) {
		synchronized (locks) {
			lock.references--;
			if (lock.references == 0) {
				locks.remove(cacheKey);
			}
		}
	}
	
	protected static String getCacheKey(String userId, String dataSourceId, OAuthProviderType provider) {
		return userId + ":" + dataSourceId + ":" + provider;
	}
	
	private static class TokenLock {
		int references;		
	}
}
