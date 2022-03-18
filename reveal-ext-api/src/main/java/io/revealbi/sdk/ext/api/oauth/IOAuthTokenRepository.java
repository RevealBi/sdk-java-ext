package io.revealbi.sdk.ext.api.oauth;

import java.io.IOException;

import com.infragistics.reveal.sdk.api.IRVUserContext;

/**
 * Interface defining the repository for OAuth tokens and its association to data sources.
 */
public interface IOAuthTokenRepository {
	/**
	 * Returns the token with the given id for the specified provider and user, {@code null} if there's no token with that id.
	 * @param userContext The context of the user that the token belongs to.
	 * @param tokenId The id of the token requested.
	 * @param provider The provider that returned the token.
	 * @return The token with the given id for the specified provider and user, {@code null} if there's no token with that id.
	 * @throws IOException If there was an error loading the token from storage.
	 */
	default OAuthToken getToken(IRVUserContext userContext, String tokenId, OAuthProviderType provider) throws IOException {
		return getToken(userContext != null ? userContext.getUserId() : null, tokenId, provider);
	}
	
	/**
	 * Returns the token with the given id for the specified provider and user, {@code null} if there's no token with that id.
	 * <br><br>{@code @deprecated} Use {@link #getToken(IRVUserContext, String, OAuthProviderType)}
	 * @param userId The id of the user that the token belongs to.
	 * @param tokenId The id of the token requested.
	 * @param provider The provider that returned the token.
	 * @return The token with the given id for the specified provider and user, {@code null} if there's no token with that id.
	 * @throws IOException If there was an error loading the token from storage.
	 */
	@Deprecated(forRemoval = true)
	default OAuthToken getToken(String userId, String tokenId, OAuthProviderType provider) throws IOException {
		throw new RuntimeException("Please implement getToken(IRVUserContext userContext, String tokenId, OAuthProviderType provider)");
	}
	
	/**
	 * Saves the token for the given user, this method is used to save new tokens and to update existing ones.
	 * @param userContext The context of the user the token belongs to.
	 * @param provider The OAuth provider that returned the token, the same id can be used for two tokens from different providers (like
	 * Google Analytics and Google Big Query, so it's important to use the provider as part of the identification in the storage).
	 * @param token The token itself that needs to be saved, this method is used to save new tokens and to update existing ones.
	 * @throws IOException If there was an error saving the token to storage.
	 */
	default void saveToken(IRVUserContext userContext, OAuthProviderType provider, OAuthToken token) throws IOException {
		saveToken(userContext != null ? userContext.getUserId() : null, provider, token);
	}

	/**
	 * Saves the token for the given user, this method is used to save new tokens and to update existing ones.
	 * <br><br>{@code @deprecated} Use {@link #saveToken(IRVUserContext, OAuthProviderType, OAuthToken)}
	 * @param userId The id of the user the token belongs to.
	 * @param provider The OAuth provider that returned the token, the same id can be used for two tokens from different providers (like
	 * Google Analytics and Google Big Query, so it's important to use the provider as part of the identification in the storage).
	 * @param token The token itself that needs to be saved, this method is used to save new tokens and to update existing ones.
	 * @throws IOException If there was an error saving the token to storage.
	 */
	@Deprecated(forRemoval = true)
	default void saveToken(String userId, OAuthProviderType provider, OAuthToken token) throws IOException {
		throw new RuntimeException("Please implement saveToken(IRVUserContext userContext, OAuthProviderType provider, OAuthToken token)");
	}

	/**
	 * Deletes the token with the given id for the specified user, no error is thrown if the token is not found.
	 * @param userContext The context of the user owning the token.
	 * @param tokenId The if of the token being deleted.
	 * @param provider The provider that created the token.
	 * @throws IOException If there was an error deleting the token from storage.
	 */
	default void deleteToken(IRVUserContext userContext, String tokenId, OAuthProviderType provider) throws IOException {
		deleteToken(userContext != null ? userContext.getUserId() : null, tokenId, provider);
	}

	/**
	 * Deletes the token with the given id for the specified user, no error is thrown if the token is not found.
	 * <br><br>{@code @deprecated} Use {@link #deleteToken(IRVUserContext, String, OAuthProviderType)}
	 * @param userId The id of the user owning the token.
	 * @param tokenId The if of the token being deleted.
	 * @param provider The provider that created the token.
	 * @throws IOException If there was an error deleting the token from storage.
	 */
	@Deprecated(forRemoval = true)
	default void deleteToken(String userId, String tokenId, OAuthProviderType provider) throws IOException {
		throw new RuntimeException("Please implement deleteToken(IRVUserContext userContext, String tokenId, OAuthProviderType provider)");
	}
	
	/**
	 * Associates a data source with the token.
	 * @param userContext The context of the user that owns the token and data source.
	 * @param dataSourceId The id of the data source being linked.
	 * @param tokenId The id of the token being linked.
	 * @param provider The provider that created the token.
	 * @throws IOException If an error occurred saving the association to storage.
	 */
	default void setDataSourceToken(IRVUserContext userContext, String dataSourceId, String tokenId, OAuthProviderType provider) throws IOException {
		setDataSourceToken(userContext != null ? userContext.getUserId() : null, dataSourceId, tokenId, provider);
	}

	/**
	 * Associates a data source with the token.
	 * <br><br>{@code @deprecated} Use #
	 * @param userId The id of the user that owns the token and data source.
	 * @param dataSourceId The id of the data source being linked.
	 * @param tokenId The id of the token being linked.
	 * @param provider The provider that created the token.
	 * @throws IOException If an error occurred saving the association to storage.
	 */
	@Deprecated(forRemoval = true)
	default void setDataSourceToken(String userId, String dataSourceId, String tokenId, OAuthProviderType provider) throws IOException {
		throw new RuntimeException("Please implement setDataSourceToken(IRVUserContext userContext, String dataSourceId, String tokenId, OAuthProviderType provider)");
	}
	
	/**
	 * Gets the token associated to the data source (if any), {@code null} is returned if no token is associated to the data source.
	 * @param userContext The context of the user owning the data source and token.
	 * @param dataSourceId The id of the data source the token is requested for.
	 * @param provider The provider of the token being requested.
	 * @return The token associated to the data source (if any), {@code null} is returned if no token is associated to the data source.
	 * @throws IOException If an error occurred loading the token from storage.
	 */
	default OAuthToken getDataSourceToken(IRVUserContext userContext, String dataSourceId, OAuthProviderType provider) throws IOException {
		return getDataSourceToken(userContext != null ? userContext.getUserId() : null, dataSourceId, provider);
	}

	/**
	 * Gets the token associated to the data source (if any), {@code null} is returned if no token is associated to the data source.
	 * <br><br>{@code @deprecated} Use {@link #getDataSourceToken(IRVUserContext, String, OAuthProviderType)}
	 * @param userId Id of the user owning the data source and token.
	 * @param dataSourceId The id of the data source the token is requested for.
	 * @param provider The provider of the token being requested.
	 * @return The token associated to the data source (if any), {@code null} is returned if no token is associated to the data source.
	 * @throws IOException If an error occurred loading the token from storage.
	 */
	@Deprecated(forRemoval = true)
	default OAuthToken getDataSourceToken(String userId, String dataSourceId, OAuthProviderType provider) throws IOException {
		throw new RuntimeException("Please implement getDataSourceToken(IRVUserContext userContext, String dataSourceId, OAuthProviderType provider)");
	}
	
	/**
	 * Gets notified that a data source was deleted and should clear any information associated to it, like the association to a token.
	 * @param userContext The context of the user that owns the data source.
	 * @param dataSourceId The id of the data source that was deleted.
	 * @param provider The provider associated to the data source, that created the token associated to it (if any).
	 * @throws IOException If an error occurred deleting the association from storage.
	 */
	default void dataSourceDeleted(IRVUserContext userContext, String dataSourceId, OAuthProviderType provider) throws IOException {
		dataSourceDeleted(userContext != null ? userContext.getUserId() : null, dataSourceId, provider);
	}

	/**
	 * Gets notified that a data source was deleted and should clear any information associated to it, like the association to a token.
	 * <br><br>{@code @deprecated} Use {@link #dataSourceDeleted(IRVUserContext, String, OAuthProviderType)}
	 * @param userId The id of the user that owns the data source.
	 * @param dataSourceId The id of the data source that was deleted.
	 * @param provider The provider associated to the data source, that created the token associated to it (if any).
	 * @throws IOException If an error occurred deleting the association from storage.
	 */
	@Deprecated(forRemoval = true)
	default void dataSourceDeleted(String userId, String dataSourceId, OAuthProviderType provider) throws IOException {
		throw new RuntimeException("Please implement dataSourceDeleted(IRVUserContext userContext, String dataSourceId, OAuthProviderType provider)");
	}
}
