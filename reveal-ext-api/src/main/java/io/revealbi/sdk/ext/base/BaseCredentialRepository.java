package io.revealbi.sdk.ext.base;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.infragistics.controls.CPJSONObject;
import com.infragistics.controls.CPJSONObjectBlock;
import com.infragistics.controls.CloudError;
import com.infragistics.controls.CloudErrorBlock;
import com.infragistics.controls.GenericOAuthProvider;
import com.infragistics.controls.HttpRequestBuilder;
import com.infragistics.controls.NativeRequestUtility;
import com.infragistics.controls.ProviderGenericOAuth;
import com.infragistics.controls.SessionHTTPMethod;
import com.infragistics.controls.TokenObject;
import com.infragistics.reportplus.datalayer.api.ProviderKeys;
import com.infragistics.reveal.sdk.api.IRVDataSourceCredential;
import com.infragistics.reveal.sdk.api.IRVUserContext;
import com.infragistics.reveal.sdk.api.RVAmazonWebServicesCredentials;
import com.infragistics.reveal.sdk.api.RVBearerTokenDataSourceCredential;
import com.infragistics.reveal.sdk.api.RVUsernamePasswordDataSourceCredential;
import com.infragistics.reveal.sdk.api.model.RVBigQueryDataSource;
import com.infragistics.reveal.sdk.api.model.RVBoxDataSource;
import com.infragistics.reveal.sdk.api.model.RVDashboardDataSource;
import com.infragistics.reveal.sdk.api.model.RVDropboxDataSource;
import com.infragistics.reveal.sdk.api.model.RVGoogleAnalyticsDataSource;
import com.infragistics.reveal.sdk.api.model.RVGoogleDriveDataSource;
import com.infragistics.reveal.sdk.api.model.RVGoogleSearchConsoleDataSource;
import com.infragistics.reveal.sdk.api.model.RVOneDriveDataSource;
import com.infragistics.reveal.sdk.util.RVModelUtilities;

import io.revealbi.sdk.ext.api.ICredentialRepository;
import io.revealbi.sdk.ext.api.oauth.IOAuthManager;
import io.revealbi.sdk.ext.api.oauth.OAuthManagerFactory;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderType;
import io.revealbi.sdk.ext.oauth.TokenLock;
import io.revealbi.sdk.ext.oauth.TokenLock.Lock;

/**
 * Base credential repository, handles resolution of OAuth credentials by delegating them to the current OAuthManager.
 * Regular (non-OAuth) credentials must be resolved by subclasses in the implementation of {@link BaseCredentialRepository#resolveRegularCredentials(String, RVDashboardDataSource)}
 */
public abstract class BaseCredentialRepository implements ICredentialRepository {
	
	private static Logger log = Logger.getLogger(BaseCredentialRepository.class.getSimpleName());

	private final TokenLock tokenLock = new TokenLock();
	
	@Override
	public final IRVDataSourceCredential resolveCredentials(IRVUserContext userContext, RVDashboardDataSource dataSource) {
		OAuthProviderType oauthProvider = getOAuthProvider(dataSource);
		if (oauthProvider != null) {
			IOAuthManager oauth = OAuthManagerFactory.getInstance();
			if (oauth != null) {
				return oauth.resolveCredentials(userContext, dataSource.getId(), oauthProvider);
			}
		}
		return resolveRegularCredentials(userContext, dataSource);
	}
	
	@Override
	public final void dataSourceDeleted(IRVUserContext userContext, String dataSourceId, String provider, String uniqueIdentifier) throws IOException {
		setDataSourceCredentials(userContext, uniqueIdentifier == null ? dataSourceId : uniqueIdentifier, null);
		OAuthProviderType oauthProvider = getOAuthProvider(provider);
		if (oauthProvider != null) {
			IOAuthManager oauth = OAuthManagerFactory.getInstance();
			if (oauth != null) {
				oauth.dataSourceDeleted(userContext, dataSourceId, oauthProvider);
			}
		}
	}
	
	/**
	 * Invoked to resolve credentials for non-OAuth data sources, as OAuth data sources are resolved in this class by using the OAuthManagerFactory
	 * @param userContext context of the user to return credentials for
	 * @param dataSource Data source to return credentials for
	 * @return Credentials to be used for this data source, null if no credentials are configured.
	 */
	private IRVDataSourceCredential resolveRegularCredentials(IRVUserContext userContext, RVDashboardDataSource dataSource) {
		Map<String, Object> map;
		try {
			map = getDataSourceCredentials(userContext, RVModelUtilities.getUniqueIdentifier(dataSource));
			if (map == null) {				
				map = getDataSourceCredentials(userContext, dataSource.getId());
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Failed to resolve credentials for datasource '" + dataSource.getId() + "'. Reason: " + e, e);
			return null;
		}
		if (map == null) return null;
		return getDataSourceCredentials(map, userContext);
	}
	
	public final IRVDataSourceCredential getCredentialsById(IRVUserContext userContext, String accountId) {
		Map<String, Object> creds = getCredentialsByIdOrNull(userContext.getUserId(), accountId);
		if (creds == null) return null;
		return getDataSourceCredentials(creds, userContext);
	}
	
	private Map<String, Object> getCredentialsByIdOrNull(String userId, String accountId) {
		try {
			return getCredentialsById(userId, accountId);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Failed to get credentials with id '" + accountId + "'. Reason: " + e, e);
			return null;
		}
	}
	
	private IRVDataSourceCredential getDataSourceCredentials(Map<String, Object> credsMap, IRVUserContext userContext) {
		Credentials credentials = new Credentials(credsMap);
		String id = credentials.getId();
		if (credentials.getOauthDefinition() != null) {
			String accessToken = null;
			Map<String, Object> sensitive = credentials.getSensitive();
			if (sensitive != null) {
				@SuppressWarnings("unchecked")
				Map<String, Object> tokenData = (Map<String, Object>) sensitive.get("token");
				TokenObject tokenJsonObject = new TokenObject(toCPJSONObject(tokenData));
				if (tokenJsonObject.isExpired()) {
					accessToken = refreshToken(userContext, credentials);
					if (accessToken == null) {
						accessToken = tokenJsonObject.getAccessToken(); // use the expired one so we get an appropriate error message
					}
				} else {
					accessToken = tokenJsonObject.getAccessToken();
				}
			}
			
			return new RVBearerTokenDataSourceCredential(accessToken, null);
		} else if (id != null && id.startsWith("rplus_aws:")) {
			return new RVAmazonWebServicesCredentials(credentials.getUserName(), credentials.getPassword(), credentials.getDomain());
		} else {				
			return new RVUsernamePasswordDataSourceCredential(credentials.getUserName(), credentials.getPassword(), credentials.getDomain());
		}
	}
		
	protected abstract Map<String, Object> getCredentialsById(String userId, String id) throws IOException;
	
	@SuppressWarnings("unchecked")
	private String refreshToken(IRVUserContext userContext, Credentials credentials) {
		String userId = userContext != null ? userContext.getUserId() : null;
		String lockKey = userId != null ? NativeRequestUtility.utility().base64Encode(userId) : "" + "_" + NativeRequestUtility.utility().base64Encode(credentials.getId());
		Lock lock = tokenLock.getLockObject(lockKey);
		try {
			synchronized (lock) {
				Map<String, Object> credentialsMap = getCredentialsByIdOrNull(userId, credentials.getId()); // get the credentials again, in case some other thread updated before me.
				if (credentialsMap == null) {
					return null; // Credentials are gone
				}
				Map<String, Object> sensitive = credentials.getSensitive();
				if (sensitive == null) {
					log.severe("Credentials with id '" + credentials.getId() + "' has lost information.");
					return null;					
				}
				Map<String, Object> tokenData = (Map<String, Object>) sensitive.get("token");
				TokenObject tokenJsonObject = new TokenObject(toCPJSONObject(tokenData));
				if (tokenJsonObject.isExpired()) {
					Map<String, Object> oauthDefinition = (Map<String, Object>) credentialsMap.get("oauthDefinition");
					if (oauthDefinition == null) return null;
					ProviderGenericOAuth pgo = GenericOAuthProvider.fromOAuthDefinition(toCPJSONObject(oauthDefinition));
					CompletableFuture<Object> cf = new CompletableFuture<Object>();
					String refreshToken = tokenJsonObject.getRefreshToken();
					if (refreshToken == null) {
						log.info("Credentials with id '" + credentials.getId() + "' do not have a refresh_token. Maybe an offline scope is missing? For google, an access_type=offline parameter may be missing.");
						return null;
					}
					HttpRequestBuilder.create()
						.setURL(pgo.getTokenUrl())
						.setHttpMethod(SessionHTTPMethod.POST)
						.setContentType("application/x-www-form-urlencoded")
						.addFormAttribute("grant_type", "refresh_token")
						.addFormAttribute("refresh_token", refreshToken)
						.addFormAttribute("client_id", pgo.getClientId())
						.addFormAttribute("client_secret", pgo.getClientSecret())
						.setJSONSuccessHandler(new CPJSONObjectBlock() {
							
							@Override
							public void invoke(CPJSONObject obj) {
								cf.complete(obj);
							}
						})
						.setErrorHandler(new CloudErrorBlock() {
							
							@Override
							public void invoke(CloudError error) {
								cf.complete(error);
							}
						})
						.buildAndExecute();
					Object result;
					try {
						result = cf.get(30, TimeUnit.SECONDS);
					} catch (InterruptedException | ExecutionException | TimeoutException e) {
						log.log(Level.SEVERE, "Unable to perform refresh. It failed unexpectedly, or timed out. " + e.getMessage(), e);
						return null;
					}
					
					if (result instanceof CloudError) {
						CloudError cloudError = (CloudError)result;
						log.log(Level.INFO, "Refresh token failed. " + cloudError.getErrorMessage(), cloudError);
						return null;
					}
					tokenData = ((CPJSONObject)result).getJSONObject();
					tokenJsonObject = new TokenObject(toCPJSONObject(tokenData));
					tokenJsonObject.setRefreshToken(refreshToken);
					sensitive.put("token", tokenJsonObject.getJSONObject());
					try {
						saveCredentials(userContext, credentials.getId(), credentialsMap);
					} catch (IOException e) {
						log.log(Level.SEVERE, "Failed to save credentials '" + credentials.getId() + "'. Reason: " + e, e);
						return null;
					}
					return tokenJsonObject.getAccessToken();
				} else {
					return tokenJsonObject.getAccessToken();
				}
			}
		} finally {
			tokenLock.releaseLock(lockKey, lock);
		}
	}
	
	private static CPJSONObject toCPJSONObject(Map<String, Object> jsonMap) {
		sanitize(jsonMap);
		return CPJSONObject.createFromJSONObject(jsonMap);
	}
	
	@SuppressWarnings("unchecked")
	private static void sanitize(Map<String, Object> jsonMap) {
		// We need to do this b/c the CPJSONObject doesn't work well with BigDecimal (when using resolveLong). And parsers seem to like parsing numbers to that type.
		for (String key : jsonMap.keySet()) {
			Object v = jsonMap.get(key);
			if (v instanceof BigDecimal) {
				try {
					long longV = ((BigDecimal)v).longValueExact();
					jsonMap.put(key, longV);
				} catch (ArithmeticException ae) {
					double doubleV = ((BigDecimal)v).doubleValue();
					jsonMap.put(key, doubleV);
				}
			} else if (v instanceof Map) {
				sanitize((Map<String, Object>) v);
			} // TODO any scenario where we need to handle lists?
		}
	}

	protected static OAuthProviderType getOAuthProvider(RVDashboardDataSource ds) {
		if (ds instanceof RVGoogleAnalyticsDataSource) {
			return OAuthProviderType.GOOGLE_ANALYTICS;
		} else if (ds instanceof RVBigQueryDataSource) {
			return OAuthProviderType.GOOGLE_BIG_QUERY;
		} else if (ds instanceof RVGoogleDriveDataSource) {
			return OAuthProviderType.GOOGLE_DRIVE;
		} else if (ds instanceof RVOneDriveDataSource) {
			return OAuthProviderType.ONE_DRIVE;
		} else if (ds instanceof RVDropboxDataSource) {
			return OAuthProviderType.DROPBOX;
		} else if (ds instanceof RVBoxDataSource) {
			return OAuthProviderType.BOX;
		} else if (ds instanceof RVGoogleSearchConsoleDataSource) {
			return OAuthProviderType.GOOGLE_SEARCH_CONSOLE;
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
		}  else if (provider.equals(ProviderKeys.oneDriveProviderKey)) {
			return OAuthProviderType.ONE_DRIVE;
		}  else if (provider.equals(ProviderKeys.dropboxProviderKey)) {
			return OAuthProviderType.DROPBOX;
		}  else if (provider.equals(ProviderKeys.boxProviderKey)) {
			return OAuthProviderType.BOX;
		} else if (provider.equals(ProviderKeys.googleSearchProviderKey)) {
			return OAuthProviderType.GOOGLE_SEARCH_CONSOLE;
		} else {
			return null;
		}
	}

}
