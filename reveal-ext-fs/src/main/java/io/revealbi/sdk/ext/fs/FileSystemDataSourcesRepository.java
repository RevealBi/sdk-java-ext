package io.revealbi.sdk.ext.fs;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

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
 */
public class FileSystemDataSourcesRepository implements IDataSourcesRepository {
	private static Logger log = Logger.getLogger(FileSystemDataSourcesRepository.class.getName());
	
	private String filePath;
	private DataSourcesInfo dataSources;
	private long cacheTimestamp;
	
	public FileSystemDataSourcesRepository(String filePath) {
		this.filePath = filePath;
	}
	
	@Override
	public DataSourcesInfo getUserDataSources(String userId) {
		ensureDataSources();
		return dataSources;
	}
	
	private synchronized void ensureDataSources() {
		File jsonFile = new File(filePath);
		if (!jsonFile.exists() || jsonFile.isDirectory() || !jsonFile.canRead()) {
			dataSources = new DataSourcesInfo();
			return;
		}
		
		if (cacheTimestamp != jsonFile.lastModified()) {
			if (cacheTimestamp > 0) {
				log.info("Detected changes in credentials.json, loading again");
			}
			dataSources = loadFromJson(filePath);
			if (dataSources == null) { //load failed
				dataSources = new DataSourcesInfo();
			} else {				
				cacheTimestamp = jsonFile.lastModified();
				
				log.info("Loaded " + dataSources.getDataSources().size() + " data sources, " + dataSources.getDataSourceItems().size() + " items.");
			}
		}
	}
	
	private static DataSourcesInfo loadFromJson(String filePath) {
		Jsonb jsonb = JsonbBuilder.create();
		try {
			DataSourcesInfo info = jsonb.fromJson(new FileInputStream(filePath), DataSourcesInfo.class);
			return info;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to load datasources.json file", e);
			return null;
		}
	}

}
