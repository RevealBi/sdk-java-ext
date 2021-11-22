package io.revealbi.sdk.ext.oauth;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.infragistics.controls.NativeStringUtility;

import io.revealbi.sdk.ext.api.oauth.OAuthProviderSettings;
import io.revealbi.sdk.ext.api.oauth.OAuthUserInfo;

public class BoxOAuthClient extends OAuthClient {

	public BoxOAuthClient() {
		super("id", new RequestParams("https://api.box.com/2.0/users/me", "GET"));
	}
	
	@Override
	public URI getAuthenticationURI(OAuthProviderSettings settings, String encodedState) {
		StringBuilder builder = new StringBuilder(settings.getAuthEndpoint())
				.append("?response_type=code")
				.append("&client_id=").append(NativeStringUtility.encodeURIQueryParam(settings.getClientId()))
				.append("&redirect_uri=").append(NativeStringUtility.encodeURIQueryParam(settings.getRedirectUri()));
		if (encodedState != null) {
			builder.append("&state=").append(encodedState);
		}
		return URI.create(builder.toString());
	}

	@Override
	public OAuthUserInfo createUserInfo(String str) {
		Map<String, Object> map = new Gson().fromJson(str, new TypeToken<HashMap<String, Object>>() {}.getType());
		return new BoxUserInfo(map);
	}

	public static class BoxUserInfo implements OAuthUserInfo {

		private final Map<String, Object> map;

		public BoxUserInfo(Map<String, Object> map) {
			this.map = map;
		}

		@Override
		public String getEmail() {
			return (String) map.get("login");
		}

		@Override
		public String getUserId() {
			return getUserId(map);
		}

		@Override
		public String getDisplayName() {
			return (String) map.get("name");
		}

		@Override
		public Map<String, Object> toJson() {
			return map;
		}

		public static String getUserId(Map<String, Object> userInfo) {
			return (String) userInfo.get("id");
		}
	}
}
