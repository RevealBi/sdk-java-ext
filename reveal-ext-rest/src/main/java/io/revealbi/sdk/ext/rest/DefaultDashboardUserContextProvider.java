package io.revealbi.sdk.ext.rest;

import javax.ws.rs.container.ContainerRequestContext;

import com.infragistics.reveal.sdk.api.IRVUserContext;
import com.infragistics.reveal.sdk.rest.RVContainerRequestAwareUserContextProvider;

/**
 * Implementation of IRVUserContextProvider that delegates the resolution of the user context to RestUserContextProviderFactory,
 * so the same user contexts are used for both REST services and Reveal backend services.
 */
public class DefaultDashboardUserContextProvider extends RVContainerRequestAwareUserContextProvider {
	@Override
	protected IRVUserContext getUserContext(ContainerRequestContext requestContext) {
		if (requestContext == null) {
			System.out.println("WARN: request context not available");
		}
		return RestUserContextProviderFactory.getInstance().getUserContext(requestContext);
	}
}
