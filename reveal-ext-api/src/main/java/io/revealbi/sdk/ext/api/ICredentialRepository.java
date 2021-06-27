package io.revealbi.sdk.ext.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.infragistics.reveal.sdk.api.IRVAuthenticationResolver;

/**
 * Interface extending {@link IRVAuthenticationResolver} by adding methods to create/update/delete credentials and to link them with data sources.
 */
public interface ICredentialRepository extends IRVAuthenticationResolver {
	/**
	 * Saves the given JSON document describing a credentials object for the given user and with the specified id, this method
	 * is used when new credentials are being created or existing credentials are being updated.
	 * @param userId The id of the user owning the credentials
	 * @param id The id of the credentials object (also called account) being saved, it could be null for new credentials.
	 * @param json The JSON document with the information about the credentials being saved.
	 * @return The id assigned to the saved credentials, if an id was received it returns the same value, if not a UUID is generated and returned
	 * @throws IOException If there was an error saving the credentials object to the storage.
	 */
	String saveCredentials(String userId, String id, Map<String, Object> json) throws IOException;
	
	/**
	 * Deletes the credentials object identified by the given id and owned by the given user.
	 * @param userId The id of the user owning the credentials object
	 * @param id The id of the account to be deleted
	 * @return {@code true} if the account was found and successfully deleted, {@code false} otherwise.
	 */
	boolean deleteCredentials(String userId, String id);
	
	/**
	 * Returns the list of credentials owned by the specified user as a list of JSON documents.
	 * @param userId The id of the user the credentials are requested for.
	 * @return the list of credentials owned by the specified user as a list of JSON documents.
	 * @throws IOException If there was an error loading the credentials from the storage.
	 */
	List<Map<String, Object>> getCredentials(String userId) throws IOException;
	
	/**
	 * Returns the credentials associated to the given data source and user.
	 * @param userId The user owning the credentials requested.
	 * @param dataSourceId The id of the data source that must have been previously associated to an account using {@link ICredentialRepository#setDataSourceCredentials(String, String, String)} method.
	 * @return The credentials associated to the given data source and user.
	 * @throws IOException If there was an error loading the credentials from storage.
	 */
	Map<String, Object> getDataSourceCredentials(String userId, String dataSourceId) throws IOException;
	
	/**
	 * Associates credentials to a data source or clears out credentials previously associated to a data source
	 * @param userId The id of the user owning the account and data source.
	 * @param dataSourceId The id of the data source being associated.
	 * @param credentialsId The id of the credentials being associated, {@code null} means the previously stored association (if any) should be deleted.
	 * @throws IOException If an error occurs persisting the association in storage.
	 */
	void setDataSourceCredentials(String userId, String dataSourceId, String credentialsId) throws IOException;
	
	/**
	 * Notifies a data source was deleted and thus any association to credentials should be removed.
	 * @param userId The id of the user owning the data source.
	 * @param dataSourceId The id of the data source that was deleted.
	 * @param provider The provider (from the list of internal providers in Reveal) used by the data source.
	 * @param uniqueIdentifier The unique identifier for the data source, for databases credentials are stored to this id and not the actual id of the data source.
	 * @throws IOException If an error occurred deleting the association from the given data source to credentials.
	 */
	void dataSourceDeleted(String userId, String dataSourceId, String provider, String uniqueIdentifier) throws IOException;
}
