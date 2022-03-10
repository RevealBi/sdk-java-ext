package io.revealbi.sdk.ext.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.annotation.JsonbTransient;

import io.revealbi.sdk.ext.base.Credentials;

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
	
	public String saveCredentials(String id, Map<String, Object> json) throws IOException {
		Credentials cred = new Credentials(json);
		saveCredentials(cred);
		return cred.getId();
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
		
		Credentials cred;
		// A given datasourceId can only be associated with one credential.
		// However, previous versions of this implementation did not take care of that appropriately. 
		// So, the following code considers the possibility of having multiple credentials for the datasource
		// to correct that situation. 
		// Implementations that do not need to 'migrate' data like this can just expect one credential.
		while ((cred = getCredentialsForDataSource(dataSourceId)) != null) {
			cred.removeDataSource(dataSourceId);
			modified = true;
		}
		
		if (credentialsId != null) {
			cred = credentials.get(credentialsId);
			if (cred != null) {
				cred.addDataSource(dataSourceId);
				modified = true;
			}
		} 

		if (modified) {
			saveCredentials();
		}
	}
	
	private Credentials getCredentialsForDataSource(String dsId) {
		for (Credentials c : credentials.values()) {
			if (c.isUsedByDataSource(dsId)) {
				return c;
			}
		}
		return null;
	}
	
	public synchronized Credentials getCredentialsWithId(String credentialsId) throws IOException {
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
		Credentials prev = credentials.get(cred.getId());
		if (prev != null) {
			cred.setDataSources(prev.getDataSources());
		}
		credentials.put(cred.getId(), cred);
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
}
