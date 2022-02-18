package io.revealbi.sdk.ext.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.infragistics.controls.NativeRequestUtility;
import com.infragistics.reportplus.dashboardmodel.DataSource;
import com.infragistics.reportplus.datalayer.DashboardModelUtils;
import com.infragistics.reportplus.datalayer.engine.util.EngineConstants;

import io.revealbi.sdk.ext.api.DataSourcesRepositoryFactory;
import io.revealbi.sdk.ext.api.oauth.IOAuthManager;
import io.revealbi.sdk.ext.api.oauth.OAuthManagerFactory;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderSettings;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderType;
import io.revealbi.sdk.ext.api.oauth.OAuthStateValidationResult;
import io.revealbi.sdk.ext.api.oauth.OAuthToken;
import io.revealbi.sdk.ext.api.oauth.OAuthUserInfo;
import io.revealbi.sdk.ext.oauth.OAuthClient;
import io.revealbi.sdk.ext.oauth.OAuthClientFactory;
import io.revealbi.sdk.ext.oauth.OAuthTokenResponse;

@Path("/oauth")
public class OAuthResource extends BaseResource {	
	private static Logger log = Logger.getLogger(OAuthResource.class.getSimpleName());
	
	protected IOAuthManager getOAuthManager() {
		IOAuthManager manager = OAuthManagerFactory.getInstance();
		if (manager == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		return manager;
	}
	
	@Path("/{providerType}/auth/{dataSourceId}")
	@GET
	@Produces("text/plain")
	public Response doAuth(@PathParam("providerType") OAuthProviderType providerType, @PathParam("dataSourceId") String dataSourceId, @QueryParam("finalUrl") String finalUrl) throws URISyntaxException {
		if (dataSourceId != null && dataSourceId.equals("_new")) {
			dataSourceId = null;
		}
		OAuthProviderSettings settings = providerType == null ? null : getOAuthManager().getProviderSettings(providerType);
		if (settings == null || settings.getAuthEndpoint() == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		Map<String, String> state = getOAuthManager().getOAuthStateProvider().getStateForAuthenticationRequest(getUserContext());
		if (dataSourceId != null) {
			state.put("dataSourceId", dataSourceId);
		}
		if (finalUrl != null) {
			state.put("finalUrl", finalUrl);
		}
		URI location = getAuthURI(settings, state);
		return Response.ok(location.toString()).build();
	}
	
	@Path("/{providerType}/callback")
	@GET
	public Response doCallback(@PathParam("providerType") OAuthProviderType providerType, @QueryParam("code") String code, @QueryParam("state") String stateStr) throws IOException {
		OAuthProviderSettings settings = providerType == null ? null : getOAuthManager().getProviderSettings(providerType);
		if (settings == null || settings.getTokenEndpoint() == null) {
			return Response.status(Status.NOT_FOUND).build();
		}		
		Map<String, String> state = decodeState(stateStr);
		String dataSourceId = state.get("dataSourceId");
		
		OAuthStateValidationResult stateResult = getOAuthManager().getOAuthStateProvider().validateAuthenticationState(getUserContext(), state);
		if (!stateResult.isValidationSuccessful()) {
			String errorMessage = stateResult.getErrorMessage();
			if (errorMessage == null || errorMessage.trim().length() == 0) {
				errorMessage = "Invalid redirect, state validation failed";
			}
			return createErrorResponse(errorMessage);			
		} else {
			String finalUrl = state.get("finalUrl");
			String authUserId = stateResult.getUserId();
			if (authUserId == null) {
				authUserId = getUserId();
			}
			return completeAuthentication(authUserId, settings, dataSourceId, code, finalUrl);
		}
	}

	@Path("/authenticated/{tokenId}")
	@GET
	public Response doAuthenticated(@PathParam("tokenId") String tokenId) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><body>");
		builder.append("You can close this window");
		builder.append("</body></html>");
		
		return Response.ok(builder.toString(), MediaType.TEXT_HTML_TYPE).build();
	}
	
	@Path("/{providerType}/{tokenId}")
	@GET
	public Response getUserInfo(@PathParam("providerType") OAuthProviderType providerType, @PathParam("tokenId") String tokenId) throws IOException {
		Map<String, Object> userInfo = getOAuthManager().getUserInfo(getUserId(), providerType, tokenId);
		if (userInfo == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(userInfo).build();
	}
	
	@Path("/{providerType}/{tokenId}/datasource")
	@GET
	public Response getTokenDataSource(@PathParam("providerType") OAuthProviderType providerType, @PathParam("tokenId") String tokenId) throws IOException {
		OAuthToken token = getOAuthManager().getToken(getUserId(), providerType, tokenId);
		if (token == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		OAuthClient client = getOAuthClient(providerType);
		OAuthUserInfo userInfo = client.createUserInfo(JsonbBuilder.create().toJson(token.getUserInfo()));
		return Response.ok(dataSource_fromOAuthToken(providerType, token, userInfo)).build();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Path("/{providerType}/{tokenId}/registerDashboardDataSource")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerDashboardDataSource(@PathParam("providerType") OAuthProviderType providerType, @PathParam("tokenId") String tokenId, Map<String, Object> dataSourceJson) throws IOException {
		String dataSourceId = generateDataSourceIdentifier(new DataSource(new HashMap(dataSourceJson)));
		getOAuthManager().setDataSourceToken(getUserId(), dataSourceId, tokenId, providerType);
		return Response.ok(dataSourceId).build();
	}

	protected Response createErrorResponse(String msg) {
		OAuthTokenResponse r = new OAuthTokenResponse();
		r.setError(msg);
		return Response.ok(r).build();
	}
	
	protected OAuthClient getOAuthClient(OAuthProviderType provider) {
		return OAuthClientFactory.getClient(provider);
	}
	
	@SuppressWarnings("unchecked")
	protected Response completeAuthentication(String userId, OAuthProviderSettings settings, String dataSourceId, String code, String finalUrl) throws IOException {
		OAuthClient client = getOAuthClient(settings.getProviderType());
		OAuthTokenResponse response = client.completeAuthentication(settings, code);
		if (response.getError() != null) {
			Jsonb json = JsonbBuilder.create();
			return Response.ok(json.toJson(response)).build();
		} else {
			OAuthToken token = createOAuthToken(response, settings.getRedirectUri());
			if (token.getRefreshToken() == null) {
				log.warning("No refreshToken obtained for provider: " + settings.getProviderType());
			}
			OAuthUserInfo userInfo = client.getUserInfo(token);
			if (userInfo != null) {
				token.setUserInfo(userInfo.toJson());
			}
			String tokenId = client.getTokenIdentifier(token);
			token.setId(tokenId);
			getOAuthManager().saveToken(userId, settings.getProviderType(), token);
			
			DataSource ds = dataSource_fromOAuthToken(settings.getProviderType(), token, userInfo); // this can later evolve to ProviderModel.dataSourceFromToken(...)
			DataSourcesRepositoryFactory.getInstance().saveDataSource(userId, ds.getId(), ds.toJson());			
			if (dataSourceId != null) {
				// The dashboard-datasource's id passed as a parameter needs to be associated to this credentials/token: 
				getOAuthManager().setDataSourceToken(userId, dataSourceId, tokenId, settings.getProviderType());
			}
			
			if (dataSourceId == null || !ds.getId().equals(dataSourceId)) {
				// Also associate the user-datasource created for the token, with that token.
				getOAuthManager().setDataSourceToken(userId, ds.getId(), tokenId, settings.getProviderType());
			}
			
			return Response.temporaryRedirect(getAuthenticationSuccessUri(settings.getRedirectUri(), finalUrl, tokenId)).build();
		}
	}
	
	private static DataSource dataSource_fromOAuthToken(OAuthProviderType providerType, OAuthToken oauthToken, OAuthUserInfo oauthUserInfo) {
		// TODO this method should be moved to some place in the Translatable world (maybe DataSourceUtility)?
		//      The logic is borrowed from DataSourceHelper.CreateDataSourceForProvider
		
		DataSource ds = new DataSource();
		ds.getProperties().setObjectValue(EngineConstants.accountIdPropertyName, oauthUserInfo.getUserId());
		ds.setProvider(providerType.getProviderId());
		ds.setSubtitle(oauthUserInfo.getEmail());
		ds.setDescription(oauthUserInfo.getDisplayName());
		ds.setId(generateDataSourceIdentifier(ds));
		return ds;
	}
	
	private static String generateDataSourceIdentifier(DataSource ds) {
		return DashboardModelUtils.getUniqueDataSourceIdentifier(ds);
	}
	
	protected URI getAuthenticationSuccessUri(String redirectUri, String finalUrl, String tokenId) {
		try {
			String baseUrl;
			if (finalUrl != null) {
				baseUrl = finalUrl;
			} else {
				baseUrl = redirectUri.replaceFirst("/callback", "/authenticated");
			}
			String url = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + tokenId;
			return new URI(url);
		} catch (URISyntaxException exc) {
			throw new RuntimeException(exc);
		}
	}
			
	private URI getAuthURI(OAuthProviderSettings settings, Map<String, String> state) {
		OAuthClient client = getOAuthClient(settings.getProviderType());
		String encodedState;
		if (state != null && !state.isEmpty()) {
			encodedState = encodeState(state);
		} else {
			encodedState = null;
		}
		return client.getAuthenticationURI(settings, encodedState);
	}
	
	private static String encodeState(Map<String, String> state) {
		return NativeRequestUtility.utility().base64Encode(JsonbBuilder.create().toJson(state));
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, String> decodeState(String stateStr) {
		return JsonbBuilder.create().fromJson(NativeRequestUtility.utility().base64Decode(stateStr), Map.class);
	}
	
	private static OAuthToken createOAuthToken(OAuthTokenResponse response, String redirectUri) {
		String accessToken = response.getAccessToken();
		String refreshToken = response.getRefreshToken();
		Integer expiresIn = response.getExpiresIn();
		long expiration = expiresIn != null ? OAuthClient.getExpirationTimeForToken(expiresIn) : 0;
		String idToken = response.getIdToken();
		String scope = response.getScope();
		
		return new OAuthToken(accessToken, refreshToken, expiration, idToken, redirectUri, scope);
	}
}

