package io.revealbi.sdk.ext.auth;

import com.infragistics.reveal.sdk.api.IRVDashboardAuthorizationProvider;
import com.infragistics.reveal.sdk.api.IRVUserContext;

import io.revealbi.sdk.ext.api.IAuthorizationProvider;

/**
 * Implements the methods defined by {@link IRVDashboardAuthorizationProvider} by mapping read/write permissions to those
 * defined by {@link IAuthorizationProvider}, to simplify the implementation. 
 */
public abstract class BaseAuthorizationProvider implements IAuthorizationProvider {
	@Override
	public boolean hasReadPermission(IRVUserContext userContext, String dashboardId) {
		return hasDashboardPermission(userContext.getUserId(), dashboardId, DashboardActionType.READ);
	}

	@Override
	public boolean hasWritePermission(IRVUserContext userContext, String dashboardId) {
		return hasDashboardPermission(userContext.getUserId(), dashboardId, DashboardActionType.WRITE);
	}
}
