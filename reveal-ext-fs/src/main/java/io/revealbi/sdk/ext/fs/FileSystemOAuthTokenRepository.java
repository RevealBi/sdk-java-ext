package io.revealbi.sdk.ext.fs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.revealbi.sdk.ext.api.oauth.IOAuthTokenRepository;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderType;
import io.revealbi.sdk.ext.api.oauth.OAuthToken;

/**
 * OAuth tokens repository implementation that loads/store data from/to a JSON file.
 * When the personal flag is true, there will be a separate file for each user, under tokens folder named {userId}.json,
 * like tokens/guest.json.
 * If personal is false, all tokens will be shared among users and stored in a single file: tokens.json.
 */
public class FileSystemOAuthTokenRepository implements IOAuthTokenRepository {
	private static final String SINGLE_USER_KEY = "tokens";
	
	private String rootDir;
	private boolean personal;
	private Map<String, SingleUserOAuthTokenRepository> repositories;
	
	public FileSystemOAuthTokenRepository(String rootDir, boolean personal) {
		this.rootDir = rootDir;
		this.personal = personal;
		this.repositories = new HashMap<String, SingleUserOAuthTokenRepository>();
	}
	
	@Override
	public OAuthToken getToken(String userId, String tokenId, OAuthProviderType provider) throws IOException {
		return getRepository(userId).getToken(tokenId, provider);
	}

	@Override
	public void saveToken(String userId, OAuthProviderType provider, OAuthToken token) throws IOException {
		getRepository(userId).saveToken(provider, token);
	}
	
	@Override
	public void setDataSourceToken(String userId, String dataSourceId, String tokenId, OAuthProviderType provider) throws IOException {
		getRepository(userId).setDataSourceToken(dataSourceId, tokenId, provider);
	}
	
	@Override
	public OAuthToken getDataSourceToken(String userId, String dataSourceId, OAuthProviderType provider) throws IOException {
		return getRepository(userId).getDataSourceToken(dataSourceId, provider);
	}	
	
	@Override
	public void deleteToken(String userId, String tokenId, OAuthProviderType provider) throws IOException {
		getRepository(userId).deleteToken(tokenId, provider);
	}
	
	@Override
	public void dataSourceDeleted(String userId, String dataSourceId, OAuthProviderType provider) throws IOException {
		getRepository(userId).dataSourceDeleted(dataSourceId, provider);
	}	
	
	private synchronized SingleUserOAuthTokenRepository getRepository(String userId) {
		String key = personal ? userId : SINGLE_USER_KEY;
		SingleUserOAuthTokenRepository repo = repositories.get(key);
		if (repo == null) {
			repo = new SingleUserOAuthTokenRepository(new File(rootDir, key + ".json").getAbsolutePath());
			repositories.put(key, repo);
		}
		return repo;
	}
}
