package io.revealbi.sdk.ext.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.infragistics.reportplus.dashboardmodel.DataSource;
import com.infragistics.reportplus.datalayer.DashboardModelUtils;

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
	public Response saveDataSource(HashMap<String, Object> credentials) throws IOException {
		
		ensureAccountId(credentials); 
		
		String assignedId = CredentialRepositoryFactory.getInstance().saveCredentials(getUserContext(), GenericCredentials.getAccountId(credentials), credentials);
		if (assignedId == null) {
			assignedId = GenericCredentials.getAccountId(credentials);
		}
		Map<String, Object> result = new HashMap<String, Object>();
		if (assignedId != null) {
			result.put("accountId", assignedId);
		}
		return Response.ok(result).build();
	}
	
	private static void ensureAccountId(HashMap<String, Object> credentials) {
		String accountId = GenericCredentials.getAccountId(credentials);
		if (accountId == null || accountId.trim().length() == 0) {
			GenericCredentials.setAccountId(credentials, UUID.randomUUID().toString());
		}
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteDataSource(GenericCredentials credentials) throws IOException {
		String id = credentials == null ? null : credentials.getAccountId();
		if (id != null) {
			boolean found = CredentialRepositoryFactory.getInstance().deleteCredentials(getUserContext(), id);
			return found ? Response.ok().build() : Response.status(Status.NOT_FOUND).build();
		}
		return Response.status(Status.NOT_FOUND).build();
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("query")
	public Response getCredentialsForDataSource(Map<String, Object> dataSource) throws IOException {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		String dsUniqueId = DashboardModelUtils.getUniqueDataSourceIdentifierForCredentials(new DataSource(new HashMap(dataSource)));
		Map<String, Object> json = CredentialRepositoryFactory.getInstance().getDataSourceCredentials(getUserContext(), dsUniqueId);
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
		List<Map<String, Object>> jsonList = CredentialRepositoryFactory.getInstance().getCredentials(getUserContext());
		return Response.ok(convertForClient(jsonList)).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("associate")
	public Response setAssignment(Map<String, Object> json) throws IOException {
		@SuppressWarnings("unchecked")
		Map<String, Object> dsJson = (Map<String, Object>) json.get("dataSource");
		String accountId = (String)json.get("accountId");
		if (json != null && accountId != null) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			String dsUniqueId = DashboardModelUtils.getUniqueDataSourceIdentifierForCredentials(new DataSource(new HashMap(dsJson)));
			CredentialRepositoryFactory.getInstance().setDataSourceCredentials(getUserContext(), dsUniqueId, accountId);
		}
		return Response.ok().build();
	}
	
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("deleteAssociation")
	public Response deleteAssignment(Map<String, Object> json) throws IOException {
		String resourceId = (String)json.get("resourceId");
		if (resourceId != null) {
			CredentialRepositoryFactory.getInstance().setDataSourceCredentials(getUserContext(), resourceId, null);
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
		GenericCredentials.putValue(newJson, "oauthDefinition", json.get("oauthDefinition"));
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
