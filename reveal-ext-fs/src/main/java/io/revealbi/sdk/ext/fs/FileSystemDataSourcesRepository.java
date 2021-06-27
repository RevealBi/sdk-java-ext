package io.revealbi.sdk.ext.fs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.revealbi.sdk.ext.api.DataSourcesInfo;
import io.revealbi.sdk.ext.api.IDataSourcesRepository;

/**
 * Data sources repository implementation that loads data sources from a JSON document.
 * The JSON file is expected to have a format like this:
 * 
 * <pre>{@code
 * {
 *   "showDataSourcesInDashboard": true,
 *   "dataSources": [{
 *     "_type": "DataSourceType",
 *     "Id": "postgres_ds",
 *     "Provider": "POSTGRES",
 *     "Description": "Postgres DS",
 *     "Properties": {
 *       "Host": "name.server.com",
 *       "Port": 5432,
 *       "Database": "database_name"
 *     }
 *   }]
 * }}</pre>
 * 
 * Changes to the file are detected and automatically loaded, no need to restart the server if the file was modified.
 * When the personal flag is true, there will be a separate file for each user, under datasources folder named {userId}.json,
 * like datasources/guest.json.
 * If personal is false, all datasources will be shared among users and stored in a single file: datasources.json.
 */
public class FileSystemDataSourcesRepository implements IDataSourcesRepository {
	private static final String SINGLE_USER_KEY = "datasources";
	
	private String rootDir;
	private boolean personal;
	private Map<String, SingleUserDataSourcesRepository> repositories;
	
	public FileSystemDataSourcesRepository(String rootDir, boolean personal) {
		this.rootDir = rootDir;
		this.personal = personal;
		this.repositories = new HashMap<String, SingleUserDataSourcesRepository>();
	}
	
	@Override
	public DataSourcesInfo getUserDataSources(String userId) {
		return getRepository(userId).getDataSources();
	}
	
	@Override
	public void saveDataSource(String userId, String dataSourceId, Map<String, Object> json) throws IOException {
		getRepository(userId).saveDataSource(dataSourceId, json);
	}
	
	@Override
	public void deleteDataSource(String userId, String dataSourceId) throws IOException {
		getRepository(userId).deleteDataSource(dataSourceId);
	}
	
	private synchronized SingleUserDataSourcesRepository getRepository(String userId) {
		String key = personal ? userId : SINGLE_USER_KEY;
		SingleUserDataSourcesRepository repo = repositories.get(key);
		if (repo == null) {
			repo = new SingleUserDataSourcesRepository(new File(rootDir, key + ".json").getAbsolutePath());
			repositories.put(key, repo);
		}
		return repo;
	}

}
