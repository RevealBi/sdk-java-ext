package io.revealbi.sdk.ext.api.oauth;

public class OAuthToken {
	private String accessToken;
	private String refreshToken;
	private long expiration;
	private String idToken;
	private String redirectUri;
	private String scope;
	
	public OAuthToken(String accessToken, String refreshToken, long expiration, String idToken, String redirectUri, String scope) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.expiration = expiration;
		this.idToken = idToken;
		this.redirectUri = redirectUri;
		this.scope = scope;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public long getExpiration() {
		return expiration;
	}

	public String getIdToken() {
		return idToken;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public String getScope() {
		return scope;
	}
	
	
}
