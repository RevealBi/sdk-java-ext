package io.revealbi.sdk.ext.auth;

import com.infragistics.reveal.sdk.api.IRVDashboardAuthorizationProvider;

import io.revealbi.sdk.ext.api.IAuthorizationProvider;

/**
 * Implements the methods defined by {@link IRVDashboardAuthorizationProvider} by mapping read/write permissions to those
 * defined by {@link IAuthorizationProvider}, to simplify the implementation. 
 */
public abstract class BaseAuthorizationProvider implements IAuthorizationProvider {
	@Override
	public boolean hasReadPermission(String userId, String dashboardId) {
		return hasDashboardPermission(userId, dashboardId, DashboardActionType.READ);
	}

	@Override
	public boolean hasWritePermission(String userId, String dashboardId) {
		return hasDashboardPermission(userId, dashboardId, DashboardActionType.WRITE);
	}
}
