package io.revealbi.sdk.ext.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.infragistics.reportplus.dashboardmodel.DataSource;
import com.infragistics.reportplus.datalayer.DashboardModelUtils;

import io.revealbi.sdk.ext.api.CredentialRepositoryFactory;
import io.revealbi.sdk.ext.api.DataSourcesInfo;
import io.revealbi.sdk.ext.api.DataSourcesRepositoryFactory;
import io.revealbi.sdk.ext.api.IDataSourcesRepository;

@Path("/dataSources")
public class DataSourcesResource extends BaseResource {	
	protected IDataSourcesRepository getDataSourcesRepository() {
		return DataSourcesRepositoryFactory.getInstance();
	}
	
	@GET
	@Produces("application/json")
	public DataSourcesInfo getDataSources() throws IOException {
		return getDataSourcesRepository().getUserDataSources(getUserContext());
	}
	
	@Path("/{dataSourceId}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveDataSource(@PathParam("dataSourceId") String dataSourceId, Map<String, Object> dataSource) throws IOException {
		DataSourcesRepositoryFactory.getInstance().saveDataSource(getUserContext(), dataSourceId, dataSource);
		return Response.ok().build();
	}
	
	@Path("/{provider}/{dataSourceId}")
	@DELETE
	public Response deleteDataSource(@PathParam("provider") String provider, @PathParam("dataSourceId") String dataSourceId) throws IOException {
		IDataSourcesRepository repository = DataSourcesRepositoryFactory.getInstance();
		Map<String, Object> dataSourceJson = repository.getUserDataSource(getUserContext(), dataSourceId);
		if (dataSourceJson != null) {
			repository.deleteDataSource(getUserContext(), dataSourceId);
			@SuppressWarnings({ "unchecked", "rawtypes" })
			DataSource ds = new DataSource(new HashMap(dataSourceJson));
			String uniqueIdentifier = DashboardModelUtils.getUniqueDataSourceIdentifierForCredentials(ds); 
			
			CredentialRepositoryFactory.getInstance().dataSourceDeleted(getUserContext(), dataSourceId, provider, uniqueIdentifier);			
		}
		return Response.ok().build();
	}

}
