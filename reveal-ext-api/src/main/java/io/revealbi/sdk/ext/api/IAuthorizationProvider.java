package io.revealbi.sdk.ext.api;

import com.infragistics.reveal.sdk.api.IRVDashboardAuthorizationProvider;

public interface IAuthorizationProvider extends IRVDashboardAuthorizationProvider {
	static enum DashboardActionType {
		READ,
		WRITE,
		DELETE
	}
	
	static enum DashboardsActionType {
		LIST,
		UPLOAD
	}
	
	boolean hasDashboardsPermission(String userId, DashboardsActionType action);
	boolean hasDashboardPermission(String userId, String dashboardId, DashboardActionType action);
}
