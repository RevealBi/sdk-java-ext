package io.revealbi.sdk.ext.rest;

import java.security.Principal;

import javax.ws.rs.container.ContainerRequestContext;

public class UserIdProvider {
	/**
	 * Returns the userId from the user principal in the context, if no principal then it returns "guest".
	 * @param requestContext The container request context.
	 * @return the userId from the user principal in the context, if no principal then it returns "guest".
	 */
	public static String getUserId(ContainerRequestContext requestContext) {
		if (requestContext == null) {
			System.out.println("WARN: request context not available");
		}
		Principal principal = requestContext.getSecurityContext().getUserPrincipal();
		return principal == null ? "guest" : principal.getName();
	}
}
