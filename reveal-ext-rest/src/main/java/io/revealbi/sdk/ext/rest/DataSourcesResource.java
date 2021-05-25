package io.revealbi.sdk.ext.rest;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
}
