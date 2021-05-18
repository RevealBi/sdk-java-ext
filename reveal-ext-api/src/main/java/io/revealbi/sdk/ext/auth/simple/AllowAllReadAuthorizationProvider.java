package io.revealbi.sdk.ext.auth.simple;

import io.revealbi.sdk.ext.auth.BaseAuthorizationProvider;

public class AllowAllReadAuthorizationProvider extends BaseAuthorizationProvider {
	@Override
	public boolean hasDashboardsPermission(String userId, DashboardsActionType action) {
		return action == DashboardsActionType.LIST;
	}

	@Override
	public boolean hasDashboardPermission(String userId, String dashboardId, DashboardActionType action) {
		return action == DashboardActionType.READ;
	}

}
