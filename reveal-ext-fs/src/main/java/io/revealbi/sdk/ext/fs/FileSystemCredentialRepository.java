package io.revealbi.sdk.ext.fs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.infragistics.reveal.sdk.api.IRVDataSourceCredential;
import com.infragistics.reveal.sdk.api.IRVUserContext;
import com.infragistics.reveal.sdk.api.model.RVDashboardDataSource;

import io.revealbi.sdk.ext.base.BaseCredentialRepository;

/**
 * Credentials repository implementation that loads credentials from a JSON document.
 * The JSON file is expected to have a format like this:
 * 
 * <pre>{@code
 * {
 *   "credentials": 
 *   [
 *    {
 * 	    "id": "unique id for this account",
 * 	    "userName": "",
 * 	    "password": "",
 * 		"dataSources": [
 * 			"dataSourceId" //id of the data source using this account, matching the id defined in datasources.json
 * 		]
 * 	  }
 *   ]
 * }}</pre>
 * 
 * Changes to the file are detected and automatically loaded, no need to restart the server if the file was modified.
 * When the personal flag is true, there will be a separate file for each user, under credentials folder named {userId}.json,
 * like credentials/guest.json.
 * If personal is false, all credentials will be shared among users and stored in a single file: credentials.json.
 */
public class FileSystemCredentialRepository extends BaseCredentialRepository {
	private static final String SINGLE_USER_KEY = "credentials";
	
	private String rootDir;
	private boolean personal;
	private Map<String, SingleUserCredentialRepository> repositories;
	
	public FileSystemCredentialRepository(String rootDir, boolean personal) {
		this.rootDir = rootDir;
		this.personal = personal;
		this.repositories = new HashMap<String, SingleUserCredentialRepository>();
	}
	
	@Override
	protected IRVDataSourceCredential resolveRegularCredentials(String userId, RVDashboardDataSource ds) {		
		return getRepository(userId).resolveCredentials(ds);
	}
	
	@Override
	public IRVDataSourceCredential getCredentialsById(IRVUserContext userContext, String accountId) {
		return getRepository(userContext.getUserId()).getCredentialsById(accountId);
	}
	
	@Override
	public String saveCredentials(String userId, String id, Map<String, Object> json) throws IOException {
		return getRepository(userId).saveCredentials(id, json);
	}

	@Override
	public Map<String, Object> getDataSourceCredentials(String userId, String dataSourceId) throws IOException {
		return getRepository(userId).getDataSourceCredentials(dataSourceId);
	}
		
	@Override
	public List<Map<String, Object>> getCredentials(String userId) throws IOException {
		return getRepository(userId).getCredentials();
	}

	@Override
	public boolean deleteCredentials(String userId, String id) {
		return getRepository(userId).deleteCredentials(id);
	}
	
	@Override
	public void setDataSourceCredentials(String userId, String dataSourceId, String credentialsId) throws IOException {
		getRepository(userId).setDataSourceCredentials(dataSourceId, credentialsId);
	}
	
	private synchronized SingleUserCredentialRepository getRepository(String userId) {
		String key = personal ? userId : SINGLE_USER_KEY;
		SingleUserCredentialRepository repo = repositories.get(key);
		if (repo == null) {
			repo = new SingleUserCredentialRepository(new File(rootDir, key + ".json").getAbsolutePath());
			repositories.put(key, repo);
		}
		return repo;
	}
}
