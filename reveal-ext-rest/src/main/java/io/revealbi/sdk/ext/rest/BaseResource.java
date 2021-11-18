package io.revealbi.sdk.ext.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;

import com.infragistics.reveal.sdk.api.IRVUserContext;

import io.revealbi.sdk.ext.api.AuthorizationProviderFactory;
import io.revealbi.sdk.ext.api.IAuthorizationProvider;

public abstract class BaseResource {
	@Context
	protected ContainerRequestContext requestContext;

	protected String getUserId() {
		return getUserContext().getUserId();
	}
	
	protected IRVUserContext getUserContext() {
		return UserContextProvider.getUserContext(requestContext);
	}
	
	protected void checkDashboardPermission(String dashboardId, IAuthorizationProvider.DashboardActionType action) {
		boolean ok = AuthorizationProviderFactory.getInstance().hasDashboardPermission(getUserId(), dashboardId, action);
		if (!ok) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
	}
	
	protected void checkDashboardsPermission(IAuthorizationProvider.DashboardsActionType action) {
		boolean ok = AuthorizationProviderFactory.getInstance().hasDashboardsPermission(getUserId(), action);
		if (!ok) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
	}
}
