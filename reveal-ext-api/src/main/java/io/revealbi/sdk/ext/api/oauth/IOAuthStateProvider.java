package io.revealbi.sdk.ext.api.oauth;

import java.util.Map;

import com.infragistics.reveal.sdk.api.IRVUserContext;

public interface IOAuthStateProvider {
	Map<String, String> getStateForAuthenticationRequest(IRVUserContext context);
	OAuthStateValidationResult validateAuthenticationState(IRVUserContext userContext, Map<String, String> state);
}
