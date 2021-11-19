package io.revealbi.sdk.ext.oauth;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.infragistics.controls.NativeStringUtility;

import io.revealbi.sdk.ext.api.oauth.OAuthProviderSettings;
import io.revealbi.sdk.ext.api.oauth.OAuthUserInfo;

public class OneDriveOAuthClient extends OAuthClient {

	public OneDriveOAuthClient() {
		super("id", new RequestParams("https://graph.microsoft.com/v1.0/me", "GET"));
	}
	
	@Override
	public OAuthUserInfo createUserInfo(String str) {
		Map<String, Object> map = new Gson().fromJson(str, new TypeToken<HashMap<String, Object>>() {}.getType());
		return new OneDriveUserInfo(map);
	}

	@Override
	public URI getAuthenticationURI(OAuthProviderSettings settings, String encodedState) {
		StringBuilder builder = new StringBuilder(settings.getAuthEndpoint())
				.append("?response_type=code")
				.append("&client_id=").append(NativeStringUtility.encodeURIQueryParam(settings.getClientId()))
				.append("&redirect_uri=").append(NativeStringUtility.encodeURIQueryParam(settings.getRedirectUri()))
				.append("&scope=").append(NativeStringUtility.encodeURIQueryParam(settings.getScope()))
				.append("&prompt=select_account");
		if (encodedState != null) {
			builder.append("&state=").append(encodedState);
		}
		return URI.create(builder.toString());
	}

	public static class OneDriveUserInfo implements OAuthUserInfo {

		private final Map<String, Object> map;

		public OneDriveUserInfo(Map<String, Object> map) {
			this.map = map;
		}

		@Override
		public String getEmail() {
			String mail = (String)map.get("mail");
            if (mail != null) 
                return mail;
            return (String)map.get("userPrincipalName");
        }

		@Override
		public String getUserId() {
			return (String) map.get("id");
		}

		@Override
		public String getDisplayName() {
			String displayName = (String) map.get("displayName");
            if (displayName != null && displayName.length() > 0)
                return displayName;
            return getEmail();
		}

		@Override
		public Map<String, Object> toJson() {
			return map;
		}
		
	}
}
