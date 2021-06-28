package io.revealbi.sdk.ext.api;

import java.io.IOException;
import java.util.Map;

public interface IDataSourcesRepository {
	/**
	 * Returns the list of data sources the user has access to, the list is expected to contain JSON documents,
	 * the format used to save data sources as JSON documents should be transparent as the UI will take care 
	 * of serializing and de-serializing them.
	 * But if you're curious or want to generate data sources, the JSON format can be obtained by extracting
	 * a "rdash" file and inspecting the "DataSources" attribute in the Dashboard.json file.
	 * @param userId The id of the user the data sources are being requested for.
	 * @return The list of data sources the user has access to.
	 * @throws IOException If an error occurred reading the list of data sources from storage.
	 */
	DataSourcesInfo getUserDataSources(String userId) throws IOException;
	
	/**
	 * Saves a data source, this method is used to save new data sources or to modify existing ones.
	 * @param userId The if of the user owning the data source.
	 * @param dataSourceId The id of the data source being saved.
	 * @param json The JSON document defining the data source.
	 * @throws IOException If an error occurred saving the data source to storage.
	 */
	void saveDataSource(String userId, String dataSourceId, Map<String, Object> json) throws IOException;
	
	/**
	 * Deletes the given data source.
	 * @param userId The id of the data source owning the data source.
	 * @param dataSourceId The id of the data source being deleted.
	 * @throws IOException If an error occurred deleting the data source from storage.
	 */
	void deleteDataSource(String userId, String dataSourceId) throws IOException;
}
