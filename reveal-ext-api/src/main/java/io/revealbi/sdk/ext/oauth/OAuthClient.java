package io.revealbi.sdk.ext.oauth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import io.revealbi.sdk.ext.api.oauth.OAuthProviderSettings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class OAuthClient {
	public OAuthTokenResponse completeAuthentication(OAuthProviderSettings settings, String code) throws IOException {
		String body = getTokenBody(settings, code);
		return performTokenAction(settings, body);
	}
	
	public OAuthTokenResponse refreshToken(OAuthProviderSettings settings, String refreshToken) throws IOException {
		String body = getRefreshTokenBody(settings, refreshToken);
		return performTokenAction(settings, body);
	}
	
	private OAuthTokenResponse performTokenAction(OAuthProviderSettings settings, String requestBody) throws UnsupportedEncodingException, IOException {
		OkHttpClient client = new OkHttpClient.Builder().build();
		Request request = new Request.Builder().
				url(settings.getTokenEndpoint()).
				post(RequestBody.create(okhttp3.MediaType.parse("application/x-www-form-urlencoded"), requestBody)).
				build();
		
		okhttp3.Response response = client.newCall(request).execute();
		String str = new String(response.body().bytes(), "UTF-8");
		Jsonb json = JsonbBuilder.create();
		return json.fromJson(str, OAuthTokenResponse.class);	
	}
	
	private static String getTokenBody(OAuthProviderSettings settings, String code) {
		StringBuilder builder = new StringBuilder();
		builder.append("grant_type=authorization_code");
		
		String clientId = settings.getClientId();
		String clientSecret = settings.getClientSecret();
		String redirectUri = settings.getRedirectUri();
		
		if (code != null) {
			builder.append("&code=").append(code);
		}
		if (clientId != null) {
			builder.append("&client_id=").append(clientId);
		}
		if (clientSecret != null) {
			builder.append("&client_secret=").append(clientSecret);
		}
		if (redirectUri != null) {
			builder.append("&redirect_uri=").append(urlEncode(redirectUri));
		}
		return builder.toString();
	}
	
	private static String getRefreshTokenBody(OAuthProviderSettings settings, String refreshToken) {
		StringBuilder builder = new StringBuilder();
		builder.append("grant_type=refresh_token");
		
		String clientId = settings.getClientId();
		String clientSecret = settings.getClientSecret();
		
		if (clientId != null) {
			builder.append("&client_id=").append(clientId);
		}
		if (clientSecret != null) {
			builder.append("&client_secret=").append(clientSecret);
		}
		if (refreshToken != null) {
			builder.append("&refresh_token=").append(urlEncode(refreshToken));
		}
		return builder.toString();
	}
	
	public static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static long getExpirationTimeForToken(long expiresIn) {
		return System.currentTimeMillis() + (expiresIn * 1000);
	}
}
