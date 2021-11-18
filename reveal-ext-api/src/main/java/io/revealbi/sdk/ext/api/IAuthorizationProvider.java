package io.revealbi.sdk.ext.api;

import com.infragistics.reveal.sdk.api.IRVDashboardAuthorizationProvider;

/**
 * Interface used to handle authorization for dashboards operations, including operations to the list of dashboards: list and upload and
 * operations to a single dashboard: read, write and delete.
 */
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
	
	/**
	 * Returns true if the specified user has the specified permission to the list of dashboards.
	 * @param userId the id of the user performing the action.
	 * @param action the action being performed, one of LIST, UPLOAD.
	 * @return true if the specified user has the specified permission to the list of dashboards.
	 */
	boolean hasDashboardsPermission(String userId, DashboardsActionType action);
	
	/**
	 * Returns true if the specified user has the specified permission to the given dashboard, this
	 * method is used to check both when a dashboard is being created or updated.
	 * @param userId the id of the user performing the action.
	 * @param dashboardId the id being affected by the action, please note this will be called also for new dashboards.
	 * @param action the action being performed, one of READ, WRITE, DELETE.
	 * @return true if the specified user has the specified permission to the given dashboard.
	 */
	boolean hasDashboardPermission(String userId, String dashboardId, DashboardActionType action);
}
