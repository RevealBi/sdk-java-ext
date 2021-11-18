package io.revealbi.sdk.ext.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.annotation.JsonbTransient;

import com.infragistics.reveal.sdk.api.IRVDataSourceCredential;
import com.infragistics.reveal.sdk.api.RVAmazonWebServicesCredentials;
import com.infragistics.reveal.sdk.api.RVUsernamePasswordDataSourceCredential;
import com.infragistics.reveal.sdk.api.model.RVDashboardDataSource;
import com.infragistics.reveal.sdk.util.RVModelUtilities;

/**
 * Credentials repository that loads/stores credentials from/to a JSON document.
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
 */
public class SingleUserCredentialRepository {
	private static Logger log = Logger.getLogger(SingleUserCredentialRepository.class.getName());
	
	private String filePath;
	private Map<String, Credentials> credentials;
	private long cacheTimestamp;
	
	public SingleUserCredentialRepository(String filePath) {
		this.filePath = filePath;
	}
	
	protected IRVDataSourceCredential resolveCredentials(RVDashboardDataSource ds) {		
		IRVDataSourceCredential result = getDataSourceCredential(RVModelUtilities.getUniqueIdentifier(ds));
		if (result == null) {
			result = getDataSourceCredential(ds.getId());
		}
		return result;
	}
	
	public IRVDataSourceCredential getCredentialsById(String accountId) {
		try {
			Credentials cred = getCredentialsWithId(accountId);
			return cred == null ? null : cred.getDataSourceCredential();
		} catch (IOException e) {
			return null;
		}		
	}
	
	public String saveCredentials(String id, Map<String, Object> json) throws IOException {
		Credentials cred = new Credentials(json);
		if (cred.userName == null) {
			throw new IOException("Expected userName");
		}
		if (cred.id == null || cred.id.trim().length() == 0) {
			cred.id = UUID.randomUUID().toString();
		}
		
		saveCredentials(cred);
		return cred.id;
	}

	public synchronized Map<String, Object> getDataSourceCredentials(String dataSourceId) throws IOException {
		ensureCredentials();
		Credentials cred = getCredentialsForDataSource(dataSourceId);
		return cred == null ? null : cred.toJson();
	}
		
	public synchronized List<Map<String, Object>> getCredentials() throws IOException {
		ensureCredentials();
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		for (Credentials c : credentials.values()) {
			list.add(c.toJson());
		}
		return list;
	}

	public synchronized boolean deleteCredentials(String id) {
		ensureCredentials();
		Credentials removed = credentials.remove(id);
		if (removed != null) {
			saveCredentials();
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized void setDataSourceCredentials(String dataSourceId, String credentialsId) throws IOException {
		ensureCredentials();
		boolean modified = false;
		if (credentialsId != null) {
			Credentials cred = credentials.get(credentialsId);
			if (cred != null) {
				cred.addDataSource(dataSourceId);
				modified = true;
			}
		} else {
			Credentials cred = getCredentialsForDataSource(dataSourceId);
			if (cred != null) {
				cred.removeDataSource(dataSourceId);
				modified = true;
			}
		}
		if (modified) {
			saveCredentials();
		}
	}
	
	private synchronized IRVDataSourceCredential getDataSourceCredential(String dsId) {
		ensureCredentials();
		Credentials cred = getCredentialsForDataSource(dsId);
		return cred == null ? null : cred.getDataSourceCredential();
	}
	
	private Credentials getCredentialsForDataSource(String dsId) {
		for (Credentials c : credentials.values()) {
			if (c.isUsedByDataSource(dsId)) {
				return c;
			}
		}
		return null;
	}
	
	private synchronized Credentials getCredentialsWithId(String credentialsId) throws IOException {
		ensureCredentials();
		return credentials.get(credentialsId);
	}
	
	private synchronized void ensureCredentials() {
		File jsonFile = new File(filePath);
		if (!jsonFile.exists() || jsonFile.isDirectory() || !jsonFile.canRead()) {
			credentials = new HashMap<String, Credentials>();
			return;
		}
		
		if (cacheTimestamp != jsonFile.lastModified()) {
			if (cacheTimestamp > 0) {
				log.info("Detected changes in credentials.json, loading again");
			}
			credentials = loadFromJson(filePath);
			if (credentials == null) { //load failed
				credentials = new HashMap<String, Credentials>();
			} else {				
				cacheTimestamp = jsonFile.lastModified();
				
				log.info("Loaded credentials: " + credentials.keySet());
			}
		}
	}
	
	private synchronized void saveCredentials(Credentials cred) {
		ensureCredentials();
		Credentials prev = credentials.get(cred.id);
		if (prev != null) {
			cred.dataSources = prev.dataSources;
		}
		credentials.put(cred.id, cred);
		saveCredentials();
	}
	
	private synchronized void saveCredentials() {
		CredentialsConfig list = new CredentialsConfig();
		if (credentials != null) {
			list.credentials = new ArrayList<Credentials>(credentials.values()).toArray(new Credentials[0]);
		} else {
			list.credentials = new Credentials[0];
		}
		JsonbConfig config = new JsonbConfig();
		config.setProperty(JsonbConfig.FORMATTING, true);
		Jsonb jsonb = JsonbBuilder.create(config);
		new File(filePath).getParentFile().mkdirs();
		try (FileOutputStream out = new FileOutputStream(filePath)) {
			jsonb.toJson(list, out);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to save credentials.json file", e);
		}
		File jsonFile = new File(filePath);
		cacheTimestamp = jsonFile.lastModified();
	}
	
	private static Map<String, Credentials> loadFromJson(String filePath) {
		Jsonb jsonb = JsonbBuilder.create();
		try {
			CredentialsConfig config = jsonb.fromJson(new FileInputStream(filePath), CredentialsConfig.class);
			return config.getCredentialsMap();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to load credentials.json file", e);
			return null;
		}
	}
	
	public static class CredentialsConfig {
		public Credentials[] credentials;
		
		@JsonbTransient
		public Map<String, Credentials> getCredentialsMap() {
			Map<String, Credentials> map = new HashMap<String, Credentials>();
			if (credentials != null) {
				for (Credentials c : credentials) {
					if (c.getId() == null) {
						continue;
					}
					map.put(c.getId(), c);
				}
			}
			
			return map;
		}
	}
	
	public static class Credentials {
		private String id;
		private String userName;
		private String domain;
		private String password;		
		private String accountName;
		private List<String> dataSources;
		
		public Credentials() {			
		}
		
		public Credentials(Map<String, Object> json) {
			this.id = (String)json.get("accountId");
			this.domain = (String)json.get("domain");
			this.userName = (String)json.get("userName");
			this.password = (String)json.get("password");
			this.accountName = (String)json.get("accountName");
		}
		
		public Map<String, Object> toJson() {
			Map<String, Object> json = new HashMap<String, Object>();
			putValue(json, "accountId", id);
			putValue(json, "accountName", accountName);
			putValue(json, "domain", domain);
			putValue(json, "userName", userName);
			//password is not included
			return json;
		}
		
		private static void putValue(Map<String, Object> json, String key, Object value) {
			if (value == null) {
				return;
			}
			json.put(key, value);
		}
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getUserName() {
			return userName;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
		public String getDomain() {
			return domain;
		}
		public void setDomain(String domain) {
			this.domain = domain;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}		
		public List<String> getDataSources() {
			return dataSources;
		}
		public void setDataSources(List<String> dataSources) {
			this.dataSources = dataSources;
		}		
		public String getAccountName() {
			return accountName;
		}
		public void setAccountName(String accountName) {
			this.accountName = accountName;
		}
		@JsonbTransient
		public IRVDataSourceCredential getDataSourceCredential() {
			String id = getId();
			if (id != null && id.startsWith("rplus_aws:")) {
				return new RVAmazonWebServicesCredentials(userName, password, domain);
			} else {				
				return new RVUsernamePasswordDataSourceCredential(userName, password, domain);
			}
			
		}
		public boolean isUsedByDataSource(String dataSourceId) {
			return dataSources != null && dataSources.contains(dataSourceId);
		}
		public void addDataSource(String dsId) {
			if (dsId == null) {
				return;
			}
			if (dataSources == null) {
				dataSources = new ArrayList<String>();				
			} else if (dataSources.contains(dsId)) {
				return;
			}
			dataSources.add(dsId);
		}
		public void removeDataSource(String dataSourceId) {
			if (dataSources == null || dataSourceId == null) {
				return;
			}
			dataSources.remove(dataSourceId);
		}
	}
}
