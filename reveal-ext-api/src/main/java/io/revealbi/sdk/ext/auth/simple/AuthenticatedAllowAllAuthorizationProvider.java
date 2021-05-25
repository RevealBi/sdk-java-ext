package io.revealbi.sdk.ext.auth.simple;

import io.revealbi.sdk.ext.auth.BaseAuthorizationProvider;

public class AuthenticatedAllowAllAuthorizationProvider extends BaseAuthorizationProvider {
	@Override
	public boolean hasDashboardsPermission(String userId, DashboardsActionType action) {
		return userId != null && userId.trim().length() > 0;
	}

	@Override
	public boolean hasDashboardPermission(String userId, String dashboardId, DashboardActionType action) {
		return userId != null && userId.trim().length() > 0;
	}

}
