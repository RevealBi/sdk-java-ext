package io.revealbi.sdk.ext.api;

import java.io.IOException;

import com.infragistics.reveal.sdk.api.IRVDashboardProvider;
import com.infragistics.reveal.sdk.api.IRVUserContext;
import com.infragistics.reveal.sdk.util.RVSerializationUtilities;

public interface IDashboardRepository extends IRVDashboardProvider {
	/**
	 * Gets the list of dashboards the user has access to, please note the returned objects are "info" objects, containing just 
	 * a summary of the dashboard so a preview can be rendered client side.
	 * In order to obtain such summary for a "rdash" file, you can use {@link RVSerializationUtilities#getDashboardSummary(java.io.InputStream)}.
	 * @param userContext The user context of the user owning the dashboards requested.
	 * @return The list of dashboards the user has access to.
	 * @throws IOException If there was an error loading the list of dashboards from storage.
	 */
	DashboardInfo[] getUserDashboards(IRVUserContext userContext) throws IOException;
	
	/**
	 * Deletes the specified dashboard from storage.
	 * @param userContext The context if of the user performing the operation.
	 * @param dashboardId The if of the dashboard being deleted.
	 * @throws IOException If there was an error deleting the dashboard from storage.
	 */
	default void deleteDashboard(IRVUserContext userContext, String dashboardId) throws IOException {
		deleteDashboard(userContext != null ? userContext.getUserId() : null, dashboardId);
	}
	
	/**
	 * Deletes the specified dashboard from storage.
	 * <br><br>{@code @deprecated} Use {@link #deleteDashboard(IRVUserContext, String)}
	 * @param userId The if of the user performing the operation.
	 * @param dashboardId The if of the dashboard being deleted.
	 * @throws IOException If there was an error deleting the dashboard from storage.
	 */
	@Deprecated(forRemoval = true)
	default void deleteDashboard(String userId, String dashboardId) throws IOException {
		throw new RuntimeException("should not be called");
	}

}
