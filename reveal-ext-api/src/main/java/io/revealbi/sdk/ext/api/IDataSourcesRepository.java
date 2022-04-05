package io.revealbi.sdk.ext.api;

import java.io.IOException;
import java.util.Map;

import com.infragistics.reveal.sdk.api.IRVUserContext;

public interface IDataSourcesRepository {
	/**
	 * Returns the list of data sources the user has access to, the list is expected to contain JSON documents,
	 * the format used to save data sources as JSON documents should be transparent as the UI will take care 
	 * of serializing and de-serializing them.
	 * But if you're curious or want to generate data sources, the JSON format can be obtained by extracting
	 * a "rdash" file and inspecting the "DataSources" attribute in the Dashboard.json file.
	 * @param userContext The context The id of the user the data sources are being requested for.
	 * @return The list of data sources the user has access to.
	 * @throws IOException If an error occurred reading the list of data sources from storage.
	 */
	default DataSourcesInfo getUserDataSources(IRVUserContext userContext) throws IOException {
		return getUserDataSources(userContext != null ? userContext.getUserId() : null);
	}
	
	/**
	 * Returns the list of data sources the user has access to, the list is expected to contain JSON documents,
	 * the format used to save data sources as JSON documents should be transparent as the UI will take care 
	 * of serializing and de-serializing them.
	 * But if you're curious or want to generate data sources, the JSON format can be obtained by extracting
	 * a "rdash" file and inspecting the "DataSources" attribute in the Dashboard.json file.
	 * <br><br>{@code @deprecated} Use {@link #getUserDataSources(IRVUserContext)}
	 * @param userId The id of the user the data sources are being requested for.
	 * @return The list of data sources the user has access to.
	 * @throws IOException If an error occurred reading the list of data sources from storage.
	 */
	@Deprecated(forRemoval = true)
	default DataSourcesInfo getUserDataSources(String userId) throws IOException {
		throw new RuntimeException("Please implement getUserDataSources(IRVUserContext userContext)");
	}
	
	/**
	 * Saves a data source, this method is used to save new data sources or to modify existing ones.
	 * @param userContext The context of the user owning the data source.
	 * @param dataSourceId The id of the data source being saved.
	 * @param json The JSON document defining the data source.
	 * @throws IOException If an error occurred saving the data source to storage.
	 */
	default void saveDataSource(IRVUserContext userContext, String dataSourceId, Map<String, Object> json) throws IOException {
		saveDataSource(userContext != null ? userContext.getUserId() : null, dataSourceId, json);
	}
	
	/**
	 * Saves a data source, this method is used to save new data sources or to modify existing ones.
	 * <br><br>{@code @deprecated} Use {@link #saveDataSource(IRVUserContext, String, Map)}
	 * @param userId The if of the user owning the data source.
	 * @param dataSourceId The id of the data source being saved.
	 * @param json The JSON document defining the data source.
	 * @throws IOException If an error occurred saving the data source to storage.
	 */
	@Deprecated(forRemoval = true)
	default void saveDataSource(String userId, String dataSourceId, Map<String, Object> json) throws IOException {
		throw new RuntimeException("Please implement saveDataSource(IRVUserContext userContext, String dataSourceId, Map<String, Object> json)");
	}
	
	/**
	 * Deletes the given data source.
	 * @param userContext The context of the user owning the data source.
	 * @param dataSourceId The id of the data source being deleted.
	 * @throws IOException If an error occurred deleting the data source from storage.
	 */
	default void deleteDataSource(IRVUserContext userContext, String dataSourceId) throws IOException {
		deleteDataSource(userContext != null ? userContext.getUserId() : null, dataSourceId);
	}

	/**
	 * Deletes the given data source.
	 * <br><br>{@code @deprecated} Use {@link #deleteDataSource(IRVUserContext, String)}
	 * @param userId The id of the user owning the data source.
	 * @param dataSourceId The id of the data source being deleted.
	 * @throws IOException If an error occurred deleting the data source from storage.
	 */
	@Deprecated(forRemoval = true)
	default void deleteDataSource(String userId, String dataSourceId) throws IOException {
		throw new RuntimeException("Please implement deleteDataSource(IRVUserContext userContext, String dataSourceId)");
	}

	/**
	 * Returns the dataSource by dataSourceId. The user should have access to this dataSource, otherwise it should return null.
	 * @param userContext The context of the user owning the data source.
	 * @param dataSourceId The id of the data source to return
	 * @return The data source, or null if it doesn't exist or user doesn't have access to.
	 */
	Map<String, Object> getUserDataSource(IRVUserContext userContext, String dataSourceId);
}
