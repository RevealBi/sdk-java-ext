package io.revealbi.sdk.ext.api.oauth;

public class OAuthToken {
	private String accessToken;
	private String refreshToken;
	private long expiration;
	private String idToken;
	private String redirectUri;
	private String scope;
	
	public OAuthToken() {		
	}
	
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

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public long getExpiration() {
		return expiration;
	}

	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}

	public String getIdToken() {
		return idToken;
	}

	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public String getScope() {
		return scope;
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}

	public void refreshed(String newAccessToken, long newExpiration) {
		this.accessToken = newAccessToken;
		this.expiration = newExpiration;
	}
}
