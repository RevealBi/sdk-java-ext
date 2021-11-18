package io.revealbi.sdk.ext.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import io.revealbi.sdk.ext.api.IAuthorizationProvider;

public class DashboardsUploadResource extends DashboardsResource {
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("/upload")
	public Response uploadDashboards(@FormDataParam("files") List<FormDataBodyPart> files) throws IOException {
		checkDashboardsPermission(IAuthorizationProvider.DashboardsActionType.UPLOAD);
		
		for (FormDataBodyPart file : files) {
			FormDataContentDisposition disposition = file.getFormDataContentDisposition();
			String fileName = disposition.getFileName();
			if (fileName == null || !fileName.endsWith(".rdash")) {
				continue;
			}
			String dashboardId = UUID.randomUUID().toString();
			try (InputStream inputStream = file.getValueAs(InputStream.class)) {
				getDashboardRepository().saveDashboard(getUserContext(), dashboardId, inputStream);
			}
		}
		return Response.ok().build();
	}
}
