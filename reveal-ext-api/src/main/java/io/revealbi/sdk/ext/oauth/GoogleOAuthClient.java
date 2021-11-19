package io.revealbi.sdk.ext.oauth;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import io.revealbi.sdk.ext.api.oauth.OAuthProviderSettings;
import io.revealbi.sdk.ext.api.oauth.OAuthUserInfo;

public class GoogleOAuthClient extends OAuthClient {

	public GoogleOAuthClient() {
		super("sub", new RequestParams("https://www.googleapis.com/oauth2/v3/userinfo", "GET"));
	}
	
	@Override
	public OAuthUserInfo createUserInfo(String str) {
		Jsonb json = JsonbBuilder.create();
		return json.fromJson(str, GoogleUserInfo.class); 
	}
	
	@Override
	public URI getAuthenticationURI(OAuthProviderSettings settings, String encodedState) {
		StringBuilder builder = new StringBuilder(settings.getAuthEndpoint());
		builder.append("?").
			append("access_type=offline").
			append("&prompt=consent").
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
		if (encodedState != null) {
			builder.append("&state=").append(encodedState);
		}
		return URI.create(builder.toString());
	}
	
	public static class GoogleUserInfo implements OAuthUserInfo {
		private static final String SUB = "sub";
		private String email;
		private String name;
		private String sub;
		
		@Override
		public String getEmail() {
			return email;
		}

		@Override
		public String getUserId() {
			return sub;
		}

		@Override
		public String getDisplayName() {
			return name;
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getSub() {
			return sub;
		}

		public void setSub(String sub) {
			this.sub = sub;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		@Override
		public Map<String, Object> toJson() {
			Map<String, Object> json = new HashMap<String, Object>();
			addToJson(json, SUB, sub);
			addToJson(json, "email", email);
			addToJson(json, "name", name);
			return json;
		}				
		
		private static void addToJson(Map<String, Object> json, String key, Object value) {
			if (value != null) {
				json.put(key, value);
			}
		}

		public static String getUserId(Map<String, Object> userInfo) {
			return (String) userInfo.get(SUB);
		}
	}
}
