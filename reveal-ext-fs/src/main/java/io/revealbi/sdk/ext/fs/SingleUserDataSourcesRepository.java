package io.revealbi.sdk.ext.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import io.revealbi.sdk.ext.api.DataSourcesInfo;

/**
 * Data sources repository that loads data sources from a JSON document.
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
public class SingleUserDataSourcesRepository {
	private static Logger log = Logger.getLogger(SingleUserDataSourcesRepository.class.getName());
	
	private String filePath;
	private DataSourcesInfo dataSources;
	private long cacheTimestamp;
	
	public SingleUserDataSourcesRepository(String filePath) {
		this.filePath = filePath;
	}
	
	public DataSourcesInfo getDataSources() {
		ensureDataSources();
		return dataSources;
	}
	
	public synchronized void saveDataSource(String dataSourceId, Map<String, Object> json) throws IOException {
		ensureDataSources();
		if (getDataSourceById(dataSourceId) != null) {
			deleteDataSource(dataSourceId);
		}
		dataSources.getDataSources().add(json);
		saveDataSources();
	}
	
	public synchronized void deleteDataSource(String dataSourceId) throws IOException {
		ensureDataSources();
		List<Map<String, Object>> list = dataSources.getDataSources();
		if (list != null) {
			Iterator<Map<String, Object>> it = list.iterator();
			while (it.hasNext()) {
				Map<String, Object> ds = it.next();
				String dsId = (String)ds.get("Id");
				if (dsId != null && dsId.equals(dataSourceId)) {
					it.remove();
				}
			}
		}
		saveDataSources();
	}
	
	private Map<String, Object> getDataSourceById(String dataSourceId) {
		List<Map<String, Object>> list = dataSources.getDataSources();
		if (list != null) {
			Iterator<Map<String, Object>> it = list.iterator();
			while (it.hasNext()) {
				Map<String, Object> ds = it.next();
				String dsId = (String)ds.get("Id");
				if (dsId != null && dsId.equals(dataSourceId)) {
					return ds;
				}
			}
		}
		return null;
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
	
	private synchronized void saveDataSources() {
		JsonbConfig config = new JsonbConfig();
		config.setProperty(JsonbConfig.FORMATTING, true);
		Jsonb jsonb = JsonbBuilder.create(config);
		new File(filePath).getParentFile().mkdirs();
		try (FileOutputStream out = new FileOutputStream(filePath)) {
			jsonb.toJson(dataSources, out);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to save datasources.json file", e);
		}
		File jsonFile = new File(filePath);
		cacheTimestamp = jsonFile.lastModified();
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

	public synchronized Map<String, Object> getDataSource(String dataSourceId) {
		return getDataSourceById(dataSourceId);
	}

}
