package io.revealbi.sdk.ext.oauth;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.infragistics.controls.NativeStringUtility;

import io.revealbi.sdk.ext.api.oauth.OAuthProviderSettings;
import io.revealbi.sdk.ext.api.oauth.OAuthUserInfo;
import okhttp3.RequestBody;

public class DropboxOAuthClient extends OAuthClient {

	public DropboxOAuthClient() {
		super(DropboxUserInfo.ACCOUNT_ID,new RequestParams("https://api.dropboxapi.com/2/users/get_current_account", "POST", RequestBody.create(null, "")));
	}

	@Override
	public URI getAuthenticationURI(OAuthProviderSettings settings, String encodedState) {
		StringBuilder builder = new StringBuilder(settings.getAuthEndpoint())
				.append("?response_type=code")
				.append("&client_id=").append(NativeStringUtility.encodeURIQueryParam(settings.getClientId()))
				.append("&redirect_uri=").append(NativeStringUtility.encodeURIQueryParam(settings.getRedirectUri()))
				.append("&force_reauthentication=true");
		if (encodedState != null) {
			builder.append("&state=").append(encodedState);
		}
		return URI.create(builder.toString());
	}
	
	@Override
	public OAuthUserInfo createUserInfo(String str) {
		Map<String, Object> map = new Gson().fromJson(str, new TypeToken<HashMap<String, Object>>() {}.getType());
		return new DropboxUserInfo(map);
	}

	public static class DropboxUserInfo implements OAuthUserInfo {

		public static final String ACCOUNT_ID = "account_id";
		
		private Map<String, Object> map;

		public DropboxUserInfo(Map<String, Object> map) {
			this.map = map;
		}

		@Override
		public String getEmail() {
			return (String) map.get("email");
		}

		@Override
		public String getUserId() {
			return getUserId(map);
		}

		@Override
		public String getDisplayName() {
			@SuppressWarnings("unchecked")
			Map<String, Object> nameMap = (Map<String, Object>) map.get("name");
			if (nameMap == null) return null;
			return (String) nameMap.get("display_name");
		}

		@Override
		public Map<String, Object> toJson() {
			return map;
		}

		public static String getUserId(Map<String, Object> userInfo) {
			return (String) userInfo.get(ACCOUNT_ID);
		}
		
	}
}
