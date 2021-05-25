package io.revealbi.sdk.ext.auth.simple;

import io.revealbi.sdk.ext.auth.BaseAuthorizationProvider;

public class DenyAllAuthorizationProvider extends BaseAuthorizationProvider {
	@Override
	public boolean hasDashboardsPermission(String userId, DashboardsActionType action) {
		return false;
	}

	@Override
	public boolean hasDashboardPermission(String userId, String dashboardId, DashboardActionType action) {
		return false;
	}

}
