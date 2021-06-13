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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.revealbi.sdk.ext.api.oauth.IOAuthManager;
import io.revealbi.sdk.ext.api.oauth.OAuthManagerFactory;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderSettings;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderType;
import io.revealbi.sdk.ext.api.oauth.OAuthToken;
import io.revealbi.sdk.ext.oauth.OAuthClient;
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
		OAuthProviderSettings settings = providerType == null ? null : getOAuthManager().getProviderSettings(providerType);
		if (settings == null || settings.getAuthEndpoint() == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		Map<String, String> state = getAuthState();
		state.put("dataSourceId", dataSourceId);
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
		if (dataSourceId == null) {
			return createErrorResponse("Invalid redirect, no data source id received");			
		} else if (!isValidUserState(state)) {
			return createErrorResponse("Invalid redirect, state validation failed");			
		} else {
			String finalUrl = state.get("finalUrl");
			return completeAuthentication(settings, dataSourceId, code, finalUrl);
		}
	}

	@Path("/authenticated")
	@GET
	public Response doAuthenticated() throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><body>");
		builder.append("You can close this window");
		builder.append("</body></html>");
		
		return Response.ok(builder.toString(), MediaType.TEXT_HTML_TYPE).build();
	}

	protected Response createErrorResponse(String msg) {
		OAuthTokenResponse r = new OAuthTokenResponse();
		r.setError(msg);
		return Response.ok(r).build();
	}
	
	protected Response completeAuthentication(OAuthProviderSettings settings, String dataSourceId, String code, String finalUrl) throws IOException {
		OAuthClient client = new OAuthClient();
		OAuthTokenResponse response = client.completeAuthentication(settings, code);
		if (response.getError() != null) {
			Jsonb json = JsonbBuilder.create();
			return Response.ok(json.toJson(response)).build();
		} else {
			OAuthToken token = createOAuthToken(response, settings.getRedirectUri());
			OAuthManagerFactory.getInstance().saveToken(getUserId(), dataSourceId, settings.getProviderType(), token);
			return Response.temporaryRedirect(getAuthenticationSuccessUri(settings.getRedirectUri(), finalUrl)).build();
		}
	}
	
	protected URI getAuthenticationSuccessUri(String redirectUri, String finalUrl) {
		try {
			if (finalUrl != null) {
				return new URI(finalUrl);
			} else {
				return new URI(redirectUri.replaceFirst("/callback", "/authenticated"));
			}
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

