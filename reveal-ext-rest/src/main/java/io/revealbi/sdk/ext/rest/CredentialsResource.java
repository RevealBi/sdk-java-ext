package io.revealbi.sdk.ext.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.revealbi.sdk.ext.api.CredentialRepositoryFactory;
import io.revealbi.sdk.ext.api.DataSourcesRepositoryFactory;
import io.revealbi.sdk.ext.api.IDataSourcesRepository;

@Path("/Credentials")
public class CredentialsResource extends BaseResource {	
	protected IDataSourcesRepository getDataSourcesRepository() {
		return DataSourcesRepositoryFactory.getInstance();
	}
		
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveDataSource(GenericCredentials credentials) throws IOException {
		String assignedId = CredentialRepositoryFactory.getInstance().saveCredentials(getUserId(), credentials.getAccountId(), credentials.toJson());
		if (assignedId == null) {
			assignedId = credentials.getAccountId();
		}
		Map<String, Object> result = new HashMap<String, Object>();
		if (assignedId != null) {
			result.put("accountId", assignedId);
		}
		return Response.ok(result).build();
	}
	
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteDataSource(GenericCredentials credentials) throws IOException {
		String id = credentials == null ? null : credentials.getAccountId();
		if (id != null) {
			boolean found = CredentialRepositoryFactory.getInstance().deleteCredentials(getUserId(), id);
			return found ? Response.ok().build() : Response.status(Status.NOT_FOUND).build();
		}
		return Response.status(Status.NOT_FOUND).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{dataSourceId}")
	public Response getCredentialsForDataSource(@PathParam("dataSourceId") String dataSourceId) throws IOException {
		Map<String, Object> json = CredentialRepositoryFactory.getInstance().getDataSourceCredentials(getUserId(), dataSourceId);
		if (json == null) {
			return Response.status(Status.NOT_FOUND).build();
		} else {
			return Response.ok(convertForClient(json)).build();
		}
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("accounts")
	public Response getAccounts() throws IOException {
		List<Map<String, Object>> jsonList = CredentialRepositoryFactory.getInstance().getCredentials(getUserId());
		return Response.ok(convertForClient(jsonList)).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("associate")
	public Response setAssignment(Map<String, Object> json) throws IOException {
		String resourceId = (String)json.get("resourceId");
		String accountId = (String)json.get("accountId");
		if (resourceId != null && accountId != null) {
			CredentialRepositoryFactory.getInstance().setDataSourceCredentials(getUserId(), resourceId, accountId);
		}
		return Response.ok().build();
	}
	
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("deleteAssociation")
	public Response deleteAssignment(Map<String, Object> json) throws IOException {
		String resourceId = (String)json.get("resourceId");
		if (resourceId != null) {
			CredentialRepositoryFactory.getInstance().setDataSourceCredentials(getUserId(), resourceId, null);
		}
		return Response.ok().build();
	}
	
	private static Map<String, Object> convertForClient(Map<String, Object> json) {
		if (json == null) {
			return null;
		}
		Map<String, Object> newJson = new HashMap<String, Object>();
		GenericCredentials.putValue(newJson, "id", json.get("accountId"));
		GenericCredentials.putValue(newJson, "accountId", json.get("accountId"));
		GenericCredentials.putValue(newJson, "name", json.get("accountName"));
		GenericCredentials.putValue(newJson, "domain", json.get("domain"));
		GenericCredentials.putValue(newJson, "userName", json.get("userName"));
		return newJson;
	}
	
	private static List<Map<String, Object>> convertForClient(List<Map<String, Object>> list) {
		List<Map<String, Object>> newList = new ArrayList<Map<String,Object>>();
		for (Map<String, Object> json : list) {
			newList.add(convertForClient(json));
		}
		return newList;
	}
}
