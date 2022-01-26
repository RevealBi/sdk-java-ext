package io.revealbi.sdk.ext.rest;

import javax.ws.rs.container.ContainerRequestContext;

import com.infragistics.reveal.sdk.api.IRVUserContext;

/**
 * User context provider used by REST services in this package, it's similar to {@link IRVUserContextProvider} but it 
 * always receives a ContainerRequestContext object.
 * You should only implement this interface and install in Reveal engine an instance of {@link DefaultDashboardUserContextProvider} that delegates 
 * the resolution to this class, so you have a single place to resolve the user context.
 */
public interface IRestUserContextProvider {
	/**
	 * Returns the user context for the given request.
	 * @param requestContext The request being processed
	 * @return the user context associated to this request
	 */
	IRVUserContext getUserContext(ContainerRequestContext requestContext);
}
