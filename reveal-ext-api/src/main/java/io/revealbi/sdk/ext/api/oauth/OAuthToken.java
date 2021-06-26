package io.revealbi.sdk.ext.api.oauth;

import java.util.Map;

public class OAuthToken {
	private String accessToken;
	private String refreshToken;
	private long expiration;
	private String idToken;
	private String redirectUri;
	private String scope;
	private Map<String, Object> userInfo;
	private String id;
	
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
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Map<String, Object> getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(Map<String, Object> userInfo) {
		this.userInfo = userInfo;
	}	

	public void refreshed(String newAccessToken, long newExpiration) {
		this.accessToken = newAccessToken;
		this.expiration = newExpiration;
	}
}
