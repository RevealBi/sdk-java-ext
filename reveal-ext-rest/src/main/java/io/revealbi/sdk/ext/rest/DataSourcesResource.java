package io.revealbi.sdk.ext.rest;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
		return getDataSourcesRepository().getUserDataSources(getUserId());
	}
	
	@Path("/{dataSourceId}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveDataSource(@PathParam("dataSourceId") String dataSourceId, Map<String, Object> dataSource) throws IOException {
		DataSourcesRepositoryFactory.getInstance().saveDataSource(getUserId(), dataSourceId, dataSource);
		return Response.ok().build();
	}
}
