package io.revealbi.sdk.ext.rest;

import java.security.Principal;

import javax.ws.rs.container.ContainerRequestContext;

import com.infragistics.reveal.sdk.api.IRVUserContext;
import com.infragistics.reveal.sdk.base.RVUserContext;

public class UserContextProvider {
	/**
	 * Returns the user context, whose userId comes from the user principal in the context, if no principal then it is "guest".
	 * @param requestContext The container request context.
	 * @return the user context.
	 */
	public static IRVUserContext getUserContext(ContainerRequestContext requestContext) {
		if (requestContext == null) {
			System.out.println("WARN: request context not available");
		}
		Principal principal = requestContext.getSecurityContext().getUserPrincipal();
		return new RVUserContext(principal == null ? "guest" : principal.getName());
	}
}
