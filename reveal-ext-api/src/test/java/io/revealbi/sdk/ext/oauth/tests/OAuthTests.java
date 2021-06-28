package io.revealbi.sdk.ext.oauth.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.junit.Test;

import io.revealbi.sdk.ext.oauth.OAuthTokenResponse;

public class OAuthTests {

	@Test
	public void testOAuthTokenParsing() {
		String str = "{\n"
				+ "  \"access_token\": \"my_access_token\",\n"
				+ "  \"expires_in\": 3599,\n"
				+ "  \"refresh_token\": \"my_refresh_token\",\n"
				+ "  \"scope\": \"https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/analytics.readonly openid\",\n"
				+ "  \"token_type\": \"Bearer\",\n"
				+ "  \"id_token\": \"my_id_token\"\n"
				+ "}";
		Jsonb jsonb = JsonbBuilder.create();
		OAuthTokenResponse response = jsonb.fromJson(str, OAuthTokenResponse.class);
		assertNotNull(response.getAccessToken());
		assertNotNull(response.getExpiresIn());
		assertEquals(new Integer(3599), response.getExpiresIn());
		assertNotNull(response.getRefreshToken());
		assertNotNull(response.getScope());
		assertNotNull(response.getIdToken());
		assertNull(response.getError());
		assertNull(response.getErrorDescription());
		
		str = jsonb.toJson(response);
		System.out.println(str);
		response = jsonb.fromJson(str, OAuthTokenResponse.class);
		assertNotNull(response.getAccessToken());
		assertNotNull(response.getExpiresIn());
		assertEquals(new Integer(3599), response.getExpiresIn());
		assertNotNull(response.getRefreshToken());
		assertNotNull(response.getScope());
		assertNotNull(response.getIdToken());
		assertNull(response.getError());
		assertNull(response.getErrorDescription());
	}

	@Test
	public void testOAuthTokenErrorParsing() {
		String str = "{\n"
				+ "  \"error\": \"invalid_grant\",\n"
				+ "  \"error_description\": \"Bad Request\"\n"
				+ "}";
		Jsonb jsonb = JsonbBuilder.create();
		OAuthTokenResponse response = jsonb.fromJson(str, OAuthTokenResponse.class);
		assertNull(response.getAccessToken());
		assertNull(response.getExpiresIn());
		assertNull(response.getRefreshToken());
		assertNull(response.getScope());
		assertNull(response.getIdToken());
		assertNotNull(response.getError());
		assertNotNull(response.getErrorDescription());
		
		str = jsonb.toJson(response);
		System.out.println(str);
		response = jsonb.fromJson(str, OAuthTokenResponse.class);
		assertNull(response.getAccessToken());
		assertNull(response.getExpiresIn());
		assertNull(response.getRefreshToken());
		assertNull(response.getScope());
		assertNull(response.getIdToken());
		assertNotNull(response.getError());
		assertNotNull(response.getErrorDescription());
	}

}
