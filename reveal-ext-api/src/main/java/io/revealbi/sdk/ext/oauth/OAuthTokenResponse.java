package io.revealbi.sdk.ext.oauth;

import javax.json.bind.annotation.JsonbProperty;

public class OAuthTokenResponse {
	private String accessToken;
	private Integer expiresIn;
	private String refreshToken;
	private String scope;
	private String tokenType;
	private String idToken;
	private String error;
	private String errorDescription;

	@JsonbProperty("access_token")
	public String getAccessToken() {
		return accessToken;
	}
	
	@JsonbProperty("access_token")
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@JsonbProperty("expires_in")
	public Integer getExpiresIn() {
		return expiresIn;
	}
	
	@JsonbProperty("expires_in")
	public void setExpiresIn(Integer expiresIn) {
		this.expiresIn = expiresIn;
	}

	@JsonbProperty("refresh_token")
	public String getRefreshToken() {
		return refreshToken;
	}
			
	@JsonbProperty("refresh_token")
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getScope() {
		return scope;
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}
	
	@JsonbProperty("token_type")
	public String getTokenType() {
		return tokenType;
	}
	
	@JsonbProperty("token_type")
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	@JsonbProperty("id_token")
	public String getIdToken() {
		return idToken;
	}
	
	@JsonbProperty("id_token")
	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@JsonbProperty("error_description")
	public String getErrorDescription() {
		return errorDescription;
	}

	@JsonbProperty("error_description")
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}		
}