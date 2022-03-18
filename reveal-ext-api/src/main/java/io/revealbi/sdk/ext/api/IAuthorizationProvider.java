package io.revealbi.sdk.ext.api;

import com.infragistics.reveal.sdk.api.IRVDashboardAuthorizationProvider;
import com.infragistics.reveal.sdk.api.IRVUserContext;

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
	 * @param userContext the context of the user performing the action.
	 * @param action the action being performed, one of LIST, UPLOAD.
	 * @return true if the specified user has the specified permission to the list of dashboards.
	 */
	default boolean hasDashboardsPermission(IRVUserContext userContext, DashboardsActionType action) {
		return hasDashboardsPermission(userContext != null ? userContext.getUserId() : null, action);
	}
	
	/**
	 * Returns true if the specified user has the specified permission to the list of dashboards.
	 * <br><br>{@code @deprecated} Use {@link #hasDashboardsPermission(IRVUserContext, DashboardsActionType)}
	 * @param userId the id of the user performing the action.
	 * @param action the action being performed, one of LIST, UPLOAD.
	 * @return true if the specified user has the specified permission to the list of dashboards.
	 */
	@Deprecated(forRemoval = true)
	default boolean hasDashboardsPermission(String userId, DashboardsActionType action) {
		throw new RuntimeException("should not be called");
	}
	
	/**
	 * Returns true if the specified user has the specified permission to the given dashboard, this
	 * method is used to check both when a dashboard is being created or updated.
	 * @param userContext the context of the user performing the action.
	 * @param dashboardId the id being affected by the action, please note this will be called also for new dashboards.
	 * @param action the action being performed, one of READ, WRITE, DELETE.
	 * @return true if the specified user has the specified permission to the given dashboard.
	 */
	default boolean hasDashboardPermission(IRVUserContext userContext, String dashboardId, DashboardActionType action) {
		return hasDashboardPermission(userContext != null ? userContext.getUserId() : null, dashboardId, action);
	}
	
	/**
	 * Returns true if the specified user has the specified permission to the given dashboard, this
	 * method is used to check both when a dashboard is being created or updated.
	 * <br><br>{@code @deprecated} Use {@link #hasDashboardPermission(IRVUserContext, String, DashboardActionType)}
	 * @param userId the id of the user performing the action.
	 * @param dashboardId the id being affected by the action, please note this will be called also for new dashboards.
	 * @param action the action being performed, one of READ, WRITE, DELETE.
	 * @return true if the specified user has the specified permission to the given dashboard.
	 */
	@Deprecated(forRemoval = true)
	default boolean hasDashboardPermission(String userId, String dashboardId, DashboardActionType action) {
		throw new RuntimeException("should not be called");
	}
}
