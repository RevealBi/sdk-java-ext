package io.revealbi.sdk.ext.oauth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import com.infragistics.reveal.sdk.api.IRVUserContext;

import io.revealbi.sdk.ext.api.oauth.IOAuthStateProvider;
import io.revealbi.sdk.ext.api.oauth.OAuthStateValidationResult;

public class BaseOAuthStateProvider implements IOAuthStateProvider {

	@Override
	public Map<String, String> getStateForAuthenticationRequest(IRVUserContext context) {
		Map<String, String> state = new HashMap<String, String>();
		state.put("user", getHashedUserId(context.getUserId()));
		return state;
	}

	@Override
	public OAuthStateValidationResult validateAuthenticationState(IRVUserContext context, Map<String, String> state) {
		String user = state.get("user");
		boolean valid = user != null && user.equals(getHashedUserId(context.getUserId()));
		return valid ? OAuthStateValidationResult.createSuccessfulValidationResult() : OAuthStateValidationResult.createFailedValidationResult(null);
	}
	
	protected String getHashedUserId(String userId) {
		if (userId == null) {
			userId = "guest";
		}
		return sha256(userId);
	}
	
	protected static final String sha256(String s) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedHash = digest.digest(s.getBytes(StandardCharsets.UTF_8));
			return hexa(encodedHash);
		} catch (Exception exc) {
			if (exc instanceof RuntimeException) {
				throw (RuntimeException)exc;
			} else {
				throw new RuntimeException(exc);
			}
		}
	}
	
	protected static String hexa(byte[] b) {
	    StringBuilder hexString = new StringBuilder(2 * b.length);
	    for (int i = 0; i < b.length; i++) {
	        String hex = Integer.toHexString(0xff & b[i]);
	        if(hex.length() == 1) {
	            hexString.append('0');
	        }
	        hexString.append(hex);
	    }
	    return hexString.toString();
	}
}
