package io.revealbi.sdk.ext.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.infragistics.reveal.sdk.api.model.RVDashboardDocument;
import com.infragistics.reveal.sdk.util.RVSerializationUtilities;

import io.revealbi.sdk.ext.api.DashboardInfo;
import io.revealbi.sdk.ext.api.DashboardRepositoryFactory;
import io.revealbi.sdk.ext.api.IAuthorizationProvider;
import io.revealbi.sdk.ext.api.IDashboardRepository;

@Path("/dashboards")
public class DashboardsResource extends BaseResource {	
	
	protected IDashboardRepository getDashboardRepository() {
		return DashboardRepositoryFactory.getInstance();
	}
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public DashboardInfo[] getDashboards() throws IOException {
		checkDashboardsPermission(IAuthorizationProvider.DashboardsActionType.LIST);
		
		return getDashboardRepository().getUserDashboards(getUserContext());
	}
	
	@DELETE	
	@Path("/{dashboardId}")
	public void deleteDashboard(@PathParam("dashboardId") String dashboardId) throws IOException {
		checkDashboardPermission(dashboardId, IAuthorizationProvider.DashboardActionType.DELETE);
		
		getDashboardRepository().deleteDashboard(getUserId(), dashboardId);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{dashboardId}")
	public Map<String, Object> getDashboardJson(@PathParam("dashboardId") String dashboardId) throws IOException {
		checkDashboardPermission(dashboardId, IAuthorizationProvider.DashboardActionType.READ);
		
		InputStream rdashStream = getDashboardRepository().getDashboard(getUserContext(), dashboardId);
		if (rdashStream != null) {
			RVDashboardDocument doc = RVSerializationUtilities.getDashboardDocument(rdashStream);
			if (doc != null) {
				return doc.toJson();
			}
		}
		throw new WebApplicationException(Response.Status.NOT_FOUND);
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/export/{dashboardId}")
	public Response getDashboard(@PathParam("dashboardId") String dashboardId) throws IOException {
		checkDashboardPermission(dashboardId, IAuthorizationProvider.DashboardActionType.READ);
		
		final InputStream rdashStream = getDashboardRepository().getDashboard(getUserContext(), dashboardId);
		if (rdashStream != null) {
			StreamingOutput output = new StreamingOutput() {				
				@Override
				public void write(OutputStream output) throws IOException, WebApplicationException {
					RVSerializationUtilities.copyStream(rdashStream, output);
				}
			};
			return Response.
					ok(output, MediaType.APPLICATION_OCTET_STREAM).
					header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + dashboardId + ".rdash").
					build();
		}
		throw new WebApplicationException(Response.Status.NOT_FOUND);
	}	
}
