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
				+ "  \"access_token\": \"ya29.a0AfH6SMCMat2-ErWtmtya1S4xluEoRIRSPUwUmm31DgPdfecSrOjxT9UnoBodyAIpm-fpsGYCrA2ZuJU_ciBffC1oGwpAydT-d6AFmpJPSlgSjRRGqn3iXTcOY7TncNaYkhfV-KM6nC_R0FaNCAcERE3zeYzd\",\n"
				+ "  \"expires_in\": 3599,\n"
				+ "  \"refresh_token\": \"1//0hghaIEudhJ-rCgYIARAAGBESNwF-L9Irbda9-uUoT_achuRlzBd72v7tdgowAYwuZML1cjXIYWD5_z8JxwRhZPjO3fU161voPS4\",\n"
				+ "  \"scope\": \"https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/analytics.readonly openid\",\n"
				+ "  \"token_type\": \"Bearer\",\n"
				+ "  \"id_token\": \"eyJhbGciOiJSUzI1NiIsImtpZCI6IjE3MTllYjk1N2Y2OTU2YjU4MThjMTk2OGZmMTZkZmY3NzRlNzA4ZGUiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI3Mzk0NjE0MjUyLWg0b2FkbDI4cWt0dWliMXJvZzRwZ2w4Mzd2b2psbnAwLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiYXVkIjoiNzM5NDYxNDI1Mi1oNG9hZGwyOHFrdHVpYjFyb2c0cGdsODM3dm9qbG5wMC5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsInN1YiI6IjExMDI0MjQ4MzA2MjQ5NDIzMzYxMiIsImVtYWlsIjoicmVwb3J0cGx1cy5pZ0BnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6InNaTHdrX3FJcHhDdkxpQXAySFl1T0EiLCJuYW1lIjoiUmVwb3J0UGx1cyBSZXBvcnRQbHVzIiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hL0FBVFhBSndrcDBlWWRuR09NZDFQenFVNUtPY1JBVEZoZnR6LTd4eDBiTW42PXM5Ni1jIiwiZ2l2ZW5fbmFtZSI6IlJlcG9ydFBsdXMiLCJmYW1pbHlfbmFtZSI6IlJlcG9ydFBsdXMiLCJsb2NhbGUiOiJlbiIsImlhdCI6MTYyMjMxNzAzNCwiZXhwIjoxNjIyMzIwNjM0fQ.fMYjUhVOCz0JNLAshpCUSvhJKErJBA2BYtn4zEAGRWkVxSh3GTDts98hqAnQX9drKF_D9rjl4BDuF2JO8fBt-6InKepGkiSK8MmipcTmO7_TAvlQHofmpjuv2lhwwGch3zP5TK8MffD1nOQcLgH4mWGZVzFJy7J3YlPDZsO7uMrb_AOQ1Lld2sc0CEvLDBUXg7MVmMzZ6HuHl_5YVRvSnD9lpA7pfzq0wwwGTKlaqkcLKun9iAXKt3idCK5diwmEzsubejXSSC8Bq-j5GOqAHT6F_GkDcX4pQ3YEVZubzdXUy9yU_ZSI9FhDkWReut1SExIHegIQv564A7jEzdDGoQ\"\n"
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
