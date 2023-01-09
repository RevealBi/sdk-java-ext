package io.revealbi.sdk.ext.api.oauth;

public class OAuthProviderSettings {
	private OAuthProviderType providerType;
	private String clientId;
	private String clientSecret;
	private String authEndpoint;
	private String tokenEndpoint;
	private String scope;
	private String redirectUri;
	
	private static final String GOOGLE_OAUTH_AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
	private static final String GOOGLE_OAUTH_TOKEN_ENDPOINT = "https://www.googleapis.com/oauth2/v4/token";
	private static final String GOOGLE_ANALYTICS_SCOPE = "https://www.googleapis.com/auth/userinfo.email%20https://www.googleapis.com/auth/userinfo.profile%20https://www.googleapis.com/auth/analytics.readonly";
	private static final String GOOGLE_ANALYTICS_4_SCOPE = "https://www.googleapis.com/auth/userinfo.email%20https://www.googleapis.com/auth/analytics.readonly";
	private static final String GOOGLE_BIGQUERY_SCOPE = "https://www.googleapis.com/auth/userinfo.email%20https://www.googleapis.com/auth/userinfo.profile%20https://www.googleapis.com/auth/bigquery.readonly";
	
	public static OAuthProviderSettings createGoogleAnalyticsSettings(String clientId, String clientSecret, String redirectUri) {
		return new OAuthProviderSettings(OAuthProviderType.GOOGLE_ANALYTICS, GOOGLE_OAUTH_AUTH_ENDPOINT, GOOGLE_OAUTH_TOKEN_ENDPOINT, clientId, clientSecret, GOOGLE_ANALYTICS_SCOPE, redirectUri);
	}
	
	public static OAuthProviderSettings createGoogleAnalytics4Settings(String clientId, String clientSecret, String redirectUri) {
		return new OAuthProviderSettings(OAuthProviderType.GOOGLE_ANALYTICS_4, GOOGLE_OAUTH_AUTH_ENDPOINT, GOOGLE_OAUTH_TOKEN_ENDPOINT, clientId, clientSecret, GOOGLE_ANALYTICS_4_SCOPE, redirectUri);
	}
	
	public static OAuthProviderSettings createBigQuerySettings(String clientId, String clientSecret, String redirectUri) {
		return new OAuthProviderSettings(OAuthProviderType.GOOGLE_BIG_QUERY, GOOGLE_OAUTH_AUTH_ENDPOINT, GOOGLE_OAUTH_TOKEN_ENDPOINT, clientId, clientSecret, GOOGLE_BIGQUERY_SCOPE, redirectUri);
	}
	
	public static OAuthProviderSettings createGoogleDriveSettings(String clientId, String clientSecret, String redirectUri) {
		return new OAuthProviderSettings(OAuthProviderType.GOOGLE_DRIVE, GOOGLE_OAUTH_AUTH_ENDPOINT, GOOGLE_OAUTH_TOKEN_ENDPOINT, clientId, clientSecret, "https://www.googleapis.com/auth/userinfo.email%20https://www.googleapis.com/auth/userinfo.profile%20https://www.googleapis.com/auth/drive", redirectUri);
	}
	
	public static OAuthProviderSettings createOneDriveSettings(String clientId, String clientSecret, String redirectUri) {
		return new OAuthProviderSettings(OAuthProviderType.ONE_DRIVE, "https://login.microsoftonline.com/common/oauth2/v2.0/authorize", "https://login.microsoftonline.com/common/oauth2/v2.0/token", clientId, clientSecret, "openid offline_access https://graph.microsoft.com/User.Read https://graph.microsoft.com/Files.ReadWrite.All", redirectUri);
	}

	public static OAuthProviderSettings createDropboxSettings(String clientId, String clientSecret, String redirectUri) {
		return new OAuthProviderSettings(OAuthProviderType.DROPBOX, "https://www.dropbox.com/oauth2/authorize", "https://api.dropbox.com/oauth2/token", clientId, clientSecret, null, redirectUri);
	}

	public static OAuthProviderSettings createBoxSettings(String clientId, String clientSecret, String redirectUri) {
		return new OAuthProviderSettings(OAuthProviderType.BOX, "https://account.box.com/api/oauth2/authorize", "https://api.box.com/oauth2/token", clientId, clientSecret, null, redirectUri);
	}
	
	public static OAuthProviderSettings createGoogleSearchConsoleSettings(String clientId, String clientSecret, String redirectUri) {
		return new OAuthProviderSettings(OAuthProviderType.GOOGLE_SEARCH_CONSOLE, GOOGLE_OAUTH_AUTH_ENDPOINT, GOOGLE_OAUTH_TOKEN_ENDPOINT, clientId, clientSecret, "https://www.googleapis.com/auth/userinfo.email%20https://www.googleapis.com/auth/webmasters.readonly%20https://www.googleapis.com/auth/userinfo.profile", redirectUri);
	}
	
	public OAuthProviderSettings(OAuthProviderType providerType, String authEndpoint, String tokenEndpoint, String clientId, String clientSecret, String scope, String redirectUri) {
		this.providerType = providerType;
		this.authEndpoint = authEndpoint;
		this.tokenEndpoint = tokenEndpoint;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.scope = scope;
		this.redirectUri = redirectUri;
	}

	public OAuthProviderType getProviderType() {
		return providerType;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getAuthEndpoint() {
		return authEndpoint;
	}
	
	public String getTokenEndpoint() {
		return tokenEndpoint;
	}
	
	public String getScope() {
		return scope;
	}
	
	public String getRedirectUri() {
		return redirectUri;
	}
}
