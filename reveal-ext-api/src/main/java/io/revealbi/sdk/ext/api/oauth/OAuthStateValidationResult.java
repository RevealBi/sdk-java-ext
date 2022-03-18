package io.revealbi.sdk.ext.api.oauth;

public class OAuthStateValidationResult {
	private String errorMessage;
	private boolean validationOk;
	private String userId;

	public static OAuthStateValidationResult createFailedValidationResult(String message) {
		return new OAuthStateValidationResult(false, message, null);
	}
	
	public static OAuthStateValidationResult createSuccessfulValidationResult() {
		return new OAuthStateValidationResult(true, null, null);		
	}
	
	@Deprecated(forRemoval = true)
	public static OAuthStateValidationResult createSuccessfulValidationResult(String userId) {
		return new OAuthStateValidationResult(true, null, userId);
	}
	
	@Deprecated(forRemoval = true)
	private OAuthStateValidationResult(boolean ok, String errorMessage, String userId) {
		this.validationOk = ok;
		this.errorMessage = ok ? null : errorMessage;
		this.userId = ok ? userId : null;
	}
	
	public boolean isValidationSuccessful() {
		return validationOk;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	@Deprecated(forRemoval = true)
	public String getUserId() {
		return userId;
	}
}
