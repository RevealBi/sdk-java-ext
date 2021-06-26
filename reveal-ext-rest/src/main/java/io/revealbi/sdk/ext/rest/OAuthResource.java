package io.revealbi.sdk.ext.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.revealbi.sdk.ext.api.DataSourcesRepositoryFactory;
import io.revealbi.sdk.ext.api.oauth.IOAuthManager;
import io.revealbi.sdk.ext.api.oauth.OAuthManagerFactory;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderSettings;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderType;
import io.revealbi.sdk.ext.api.oauth.OAuthToken;
import io.revealbi.sdk.ext.api.oauth.OAuthUserInfo;
import io.revealbi.sdk.ext.oauth.OAuthClient;
import io.revealbi.sdk.ext.oauth.OAuthClientFactory;
import io.revealbi.sdk.ext.oauth.OAuthTokenResponse;

@Path("/oauth")
public class OAuthResource extends BaseResource {	
	
	protected IOAuthManager getOAuthManager() {
		IOAuthManager manager = OAuthManagerFactory.getInstance();
		if (manager == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		return manager;
	}
	
	@Path("/{providerType}/auth/{dataSourceId}")
	@GET
	public Response doAuth(@PathParam("providerType") OAuthProviderType providerType, @PathParam("dataSourceId") String dataSourceId, @QueryParam("finalUrl") String finalUrl) throws URISyntaxException {
		if (dataSourceId != null && dataSourceId.equals("_new")) {
			dataSourceId = null;
		}
		OAuthProviderSettings settings = providerType == null ? null : getOAuthManager().getProviderSettings(providerType);
		if (settings == null || settings.getAuthEndpoint() == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		Map<String, String> state = getAuthState();
		if (dataSourceId != null) {
			state.put("dataSourceId", dataSourceId);
		}
		if (finalUrl != null) {
			state.put("finalUrl", finalUrl);
		}
		return Response.temporaryRedirect(getAuthURI(settings, state)).build();
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
		if (!isValidUserState(state)) {
			return createErrorResponse("Invalid redirect, state validation failed");			
		} else {
			String finalUrl = state.get("finalUrl");
			return completeAuthentication(settings, dataSourceId, code, finalUrl);
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
	
	@Path("/{providerType}/{tokenId}/{dataSourceId}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveDataSource(@PathParam("providerType") OAuthProviderType providerType, @PathParam("tokenId") String tokenId, @PathParam("dataSourceId") String dataSourceId, Map<String, Object> dataSource) throws IOException {
		DataSourcesRepositoryFactory.getInstance().saveDataSource(getUserId(), dataSourceId, dataSource);
		getOAuthManager().setDataSourceToken(getUserId(), dataSourceId, tokenId, providerType);
		return Response.ok().build();
	}

	protected Response createErrorResponse(String msg) {
		OAuthTokenResponse r = new OAuthTokenResponse();
		r.setError(msg);
		return Response.ok(r).build();
	}
	
	protected OAuthClient getOAuthClient(OAuthProviderType provider) {
		return OAuthClientFactory.getClient(provider);
	}
	
	protected Response completeAuthentication(OAuthProviderSettings settings, String dataSourceId, String code, String finalUrl) throws IOException {
		OAuthClient client = getOAuthClient(settings.getProviderType());
		OAuthTokenResponse response = client.completeAuthentication(settings, code);
		if (response.getError() != null) {
			Jsonb json = JsonbBuilder.create();
			return Response.ok(json.toJson(response)).build();
		} else {
			OAuthToken token = createOAuthToken(response, settings.getRedirectUri());
			OAuthUserInfo userInfo = client.getUserInfo(token);
			if (userInfo != null) {
				token.setUserInfo(userInfo.toJson());
			}
			String tokenId = client.getTokenIdentifier(token);
			token.setId(tokenId);
			OAuthManagerFactory.getInstance().saveToken(getUserId(), settings.getProviderType(), token);
			if (dataSourceId != null) {
				OAuthManagerFactory.getInstance().setDataSourceToken(getUserId(), dataSourceId, tokenId, settings.getProviderType());
			}
			return Response.temporaryRedirect(getAuthenticationSuccessUri(settings.getRedirectUri(), finalUrl, tokenId)).build();
		}
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
		
	protected Map<String, String> getAuthState() {
		Map<String, String> state = new HashMap<String, String>();
		state.put("user", getHashedUserId());
		return state;
	}
	
	protected boolean isValidUserState(Map<String, String> state) {
		String user = state.get("user");
		return user != null && user.equals(getHashedUserId());
	}

	protected String getUserIdForState() {
		String userId = getUserId();
		if (userId == null) {
			return "guest";
		}
		return userId;
	}
	
	protected String getHashedUserId() {
		return sha256(getUserIdForState());
	}
	
	protected static final String sha256(String s) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedHash = digest.digest(s.getBytes(StandardCharsets.UTF_8));
			return hexa(encodedHash);
		} catch (Exception exc) {
			if (exc instanceof RuntimeException) {
				throw (RuntimeException)exc;
			} else {
				throw new RuntimeException(exc);
			}
		}
	}
	
	protected static String hexa(byte[] b) {
	    StringBuilder hexString = new StringBuilder(2 * b.length);
	    for (int i = 0; i < b.length; i++) {
	        String hex = Integer.toHexString(0xff & b[i]);
	        if(hex.length() == 1) {
	            hexString.append('0');
	        }
	        hexString.append(hex);
	    }
	    return hexString.toString();
	}
	
	private static URI getAuthURI(OAuthProviderSettings settings, Map<String, String> state) {
		StringBuilder builder = new StringBuilder(settings.getAuthEndpoint());
		builder.append("?").
			append("access_type=offline").
			append("&response_type=code");
		
		String scope = settings.getScope();
		String clientId = settings.getClientId();
		String redirectUri = settings.getRedirectUri();
		
		if (scope != null) {
			builder.append("&scope=").append(scope);
		}
		if (clientId != null) {
			builder.append("&client_id=").append(clientId);
		}
		if (redirectUri != null) {
			builder.append("&redirect_uri=").append(redirectUri);
		}
		if (state != null && !state.isEmpty()) {
			builder.append("&state=").append(encodeState(state));
		}
		return URI.create(builder.toString());
	}
	
	private static String encodeState(Map<String, String> state) {
		StringBuilder stateBuilder = new StringBuilder();
		for (String key : state.keySet()) {
			if (stateBuilder.length() > 0) {
				stateBuilder.append("&");
			}
			stateBuilder.append(key).append("=").append(state.get(key));
		}
		return OAuthClient.urlEncode(stateBuilder.toString());
	}	
	
	private static Map<String, String> decodeState(String stateStr) {
		try {
			stateStr = URLDecoder.decode(stateStr, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		Map<String, String> state = new HashMap<String, String>();
		for (String part : stateStr.split("&")) {
			String[] pair = part.split("=");
			if (pair.length != 2) {
				continue;
			}
			state.put(pair[0], pair[1]);
		}
		return state;
	}
	
	private static OAuthToken createOAuthToken(OAuthTokenResponse response, String redirectUri) {
		String accessToken = response.getAccessToken();
		String refreshToken = response.getRefreshToken();
		long expiration = OAuthClient.getExpirationTimeForToken(response.getExpiresIn());
		String idToken = response.getIdToken();
		String scope = response.getScope();
		
		return new OAuthToken(accessToken, refreshToken, expiration, idToken, redirectUri, scope);
	}
}

