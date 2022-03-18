package io.revealbi.sdk.ext.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.infragistics.reveal.sdk.api.IRVAuthenticationResolver;
import com.infragistics.reveal.sdk.api.IRVUserContext;

/**
 * Interface extending {@link IRVAuthenticationResolver} by adding methods to create/update/delete credentials and to link them with data sources.
 */
public interface ICredentialRepository extends IRVAuthenticationResolver {
	/**
	 * Saves the given JSON document describing a credentials object for the given user and with the specified id, this method
	 * is used when new credentials are being created or existing credentials are being updated.
	 * @param userContext The context of the user owning the credentials
	 * @param id The id of the credentials object (also called account) being saved, it could be null for new credentials.
	 * @param json The JSON document with the information about the credentials being saved.
	 * @return The id assigned to the saved credentials, if an id was received it returns the same value, if not a UUID is generated and returned
	 * @throws IOException If there was an error saving the credentials object to the storage.
	 */
	default String saveCredentials(IRVUserContext userContext, String id, Map<String, Object> json) throws IOException {
		return saveCredentials(userContext != null ? userContext.getUserId() : null, id, json);
	}
	
	/**
	 * Saves the given JSON document describing a credentials object for the given user and with the specified id, this method
	 * is used when new credentials are being created or existing credentials are being updated.
	 * <br><br>{@code @deprecated} Use {@link #saveCredentials(IRVUserContext, String, Map)}
	 * @param userId The id of the user owning the credentials
	 * @param id The id of the credentials object (also called account) being saved, it could be null for new credentials.
	 * @param json The JSON document with the information about the credentials being saved.
	 * @return The id assigned to the saved credentials, if an id was received it returns the same value, if not a UUID is generated and returned
	 * @throws IOException If there was an error saving the credentials object to the storage.
	 */
	@Deprecated(forRemoval = true)
	default String saveCredentials(String userId, String id, Map<String, Object> json) throws IOException {
		throw new RuntimeException("Please implement saveCredentials(IRVUserContext userContext, String id, Map<String, Object> json)");
	}
	
	/**
	 * Deletes the credentials object identified by the given id and owned by the given user.
	 * @param userContext The context of the user owning the credentials object
	 * @param id The id of the account to be deleted
	 * @return {@code true} if the account was found and successfully deleted, {@code false} otherwise.
	 */
	default boolean deleteCredentials(IRVUserContext userContext, String id) {
		return deleteCredentials(userContext != null ? userContext.getUserId() : null, id);
	}
	
	/**
	 * Deletes the credentials object identified by the given id and owned by the given user.
	 * <br><br>{@code @deprecated} Use {@link #deleteCredentials(IRVUserContext, String)}
	 * @param userId The id of the user owning the credentials object
	 * @param id The id of the account to be deleted
	 * @return {@code true} if the account was found and successfully deleted, {@code false} otherwise.
	 */
	@Deprecated(forRemoval = true)
	default boolean deleteCredentials(String userId, String id) {
		throw new RuntimeException("Please implement deleteCredentials(IRVUserContext userContext, String id)");
	}
	
	/**
	 * Returns the list of credentials owned by the specified user as a list of JSON documents.
	 * @param userContext The context of the user the credentials are requested for.
	 * @return the list of credentials owned by the specified user as a list of JSON documents.
	 * @throws IOException If there was an error loading the credentials from the storage.
	 */
	default List<Map<String, Object>> getCredentials(IRVUserContext userContext) throws IOException {
		return getCredentials(userContext != null ? userContext.getUserId() : null);
	}
	
	/**
	 * Returns the list of credentials owned by the specified user as a list of JSON documents.
	 * <br><br>{@code @deprecated} Use {@link #getCredentials(IRVUserContext)}
	 * @param userId The id of the user the credentials are requested for.
	 * @return the list of credentials owned by the specified user as a list of JSON documents.
	 * @throws IOException If there was an error loading the credentials from the storage.
	 */
	@Deprecated(forRemoval = true)
	default List<Map<String, Object>> getCredentials(String userId) throws IOException {
		throw new RuntimeException("Please implement getCredentials(IRVUserContext userContext)");
	}
	
	/**
	 * Returns the credentials associated to the given data source and user.
	 * @param userContext The context owning the credentials requested.
	 * @param dataSourceId The id of the data source that must have been previously associated to an account using {@link ICredentialRepository#setDataSourceCredentials(IRVUserContext, String, String)} method.
	 * @return The credentials associated to the given data source and user.
	 * @throws IOException If there was an error loading the credentials from storage.
	 */
	default Map<String, Object> getDataSourceCredentials(IRVUserContext userContext, String dataSourceId) throws IOException {
		return getDataSourceCredentials(userContext != null ? userContext.getUserId() : null, dataSourceId);
	}
	
	/**
	 * Returns the credentials associated to the given data source and user.
	 * <br><br>{@code @deprecated} Use {@link #getDataSourceCredentials(IRVUserContext, String)}
	 * @param userId The user owning the credentials requested.
	 * @param dataSourceId The id of the data source that must have been previously associated to an account using {@link ICredentialRepository#setDataSourceCredentials(IRVUserContext, String, String)} method.
	 * @return The credentials associated to the given data source and user.
	 * @throws IOException If there was an error loading the credentials from storage.
	 */
	@Deprecated(forRemoval = true)
	default Map<String, Object> getDataSourceCredentials(String userId, String dataSourceId) throws IOException {
		throw new RuntimeException("Please implement getDataSourceCredentials(IRVUserContext userContext, String dataSourceId)");
	}
	
	/**
	 * Associates credentials to a data source or clears out credentials previously associated to a data source
	 * @param userContext The context of the user owning the account and data source.
	 * @param dataSourceId The id of the data source being associated.
	 * @param credentialsId The id of the credentials being associated, {@code null} means the previously stored association (if any) should be deleted.
	 * @throws IOException If an error occurs persisting the association in storage.
	 */
	default void setDataSourceCredentials(IRVUserContext userContext, String dataSourceId, String credentialsId) throws IOException {
		setDataSourceCredentials(userContext != null ? userContext.getUserId() : null, dataSourceId, credentialsId);
	}
	
	/**
	 * Associates credentials to a data source or clears out credentials previously associated to a data source
	 * <br><br>{@code @deprecated} Use {@link #setDataSourceCredentials(IRVUserContext, String, String)}
	 * @param userId The id of the user owning the account and data source.
	 * @param dataSourceId The id of the data source being associated.
	 * @param credentialsId The id of the credentials being associated, {@code null} means the previously stored association (if any) should be deleted.
	 * @throws IOException If an error occurs persisting the association in storage.
	 */
	@Deprecated(forRemoval = true)
	default void setDataSourceCredentials(String userId, String dataSourceId, String credentialsId) throws IOException {
		throw new RuntimeException("Please implement setDataSourceCredentials(IRVUserContext userContext, String dataSourceId, String credentialsId)");
	}
	
	/**
	 * Notifies a data source was deleted and thus any association to credentials should be removed.
	 * @param userContext The context of the user owning the data source.
	 * @param dataSourceId The id of the data source that was deleted.
	 * @param provider The provider (from the list of internal providers in Reveal) used by the data source.
	 * @param uniqueIdentifier The unique identifier for the data source, for databases credentials are stored to this id and not the actual id of the data source.
	 * @throws IOException If an error occurred deleting the association from the given data source to credentials.
	 */
	default void dataSourceDeleted(IRVUserContext userContext, String dataSourceId, String provider, String uniqueIdentifier) throws IOException {
		dataSourceDeleted(userContext != null ? userContext.getUserId() : null, dataSourceId, provider, uniqueIdentifier);
	}

	/**
	 * Notifies a data source was deleted and thus any association to credentials should be removed.
	 * <br><br>{@code @deprecated} Use {@link #dataSourceDeleted(IRVUserContext, String, String, String)}
	 * @param userId The id of the user owning the data source.
	 * @param dataSourceId The id of the data source that was deleted.
	 * @param provider The provider (from the list of internal providers in Reveal) used by the data source.
	 * @param uniqueIdentifier The unique identifier for the data source, for databases credentials are stored to this id and not the actual id of the data source.
	 * @throws IOException If an error occurred deleting the association from the given data source to credentials.
	 */
	@Deprecated(forRemoval = true)
	default void dataSourceDeleted(String userId, String dataSourceId, String provider, String uniqueIdentifier) throws IOException {
		throw new RuntimeException("Please implement dataSourceDeleted(IRVUserContext userContext, String dataSourceId, String provider, String uniqueIdentifier)");
	}
}
