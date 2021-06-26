package io.revealbi.sdk.ext.api.oauth;

import java.util.Map;

public interface OAuthUserInfo {
	String getEmail();
	String getUserId();
	String getDisplayName();
	Map<String, Object> toJson();
}
