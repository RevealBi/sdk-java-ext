package io.revealbi.sdk.ext.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;

import io.revealbi.sdk.ext.api.AuthorizationProviderFactory;
import io.revealbi.sdk.ext.api.IAuthorizationProvider;

public abstract class BaseResource {
	@Context
	protected ContainerRequestContext requestContext;

	protected String getUserId() {
		return UserIdProvider.getUserId(requestContext);
	}
	
	protected void checkDashboardPermission(String dashboardId, IAuthorizationProvider.DashboardActionType action) {
		boolean ok = AuthorizationProviderFactory.getInstance().hasDashboardPermission(UserIdProvider.getUserId(requestContext), dashboardId, action);
		if (!ok) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
	}
	
	protected void checkDashboardsPermission(IAuthorizationProvider.DashboardsActionType action) {
		boolean ok = AuthorizationProviderFactory.getInstance().hasDashboardsPermission(UserIdProvider.getUserId(requestContext), action);
		if (!ok) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
	}
}
