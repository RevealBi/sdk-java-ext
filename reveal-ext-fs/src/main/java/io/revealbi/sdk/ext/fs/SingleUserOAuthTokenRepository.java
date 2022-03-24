package io.revealbi.sdk.ext.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import io.revealbi.sdk.ext.api.oauth.OAuthProviderType;
import io.revealbi.sdk.ext.api.oauth.OAuthToken;

public class SingleUserOAuthTokenRepository {
	private static Logger log = Logger.getLogger(FileSystemDataSourcesRepository.class.getName());
	
	private String filePath;
	private OAuthTokensInfo tokens;
	private long cacheTimestamp;
	
	public SingleUserOAuthTokenRepository(String filePath) {
		this.filePath = filePath;
	}
	
	public synchronized OAuthToken getToken(String tokenId, OAuthProviderType provider) throws IOException {
		ensureTokens();
		OAuthTokenInfo info = tokens.getTokens().get(getTokenInfoId(tokenId, provider));
		return info == null ? null : info.getToken();
	}

	public synchronized void saveToken(OAuthProviderType provider, OAuthToken token) throws IOException {
		ensureTokens();		
		String tokenId = token.getId();
		if (tokenId == null) {
			tokenId = UUID.randomUUID().toString();
			token.setId(tokenId);
		}
		String tokenInfoId = getTokenInfoId(tokenId, provider);
		OAuthTokenInfo info = tokens.getTokens().get(tokenInfoId);
		if (info == null) {
			info = new OAuthTokenInfo(tokenId, provider, token);
		} else {
			info.setToken(token);
		}
		tokens.getTokens().put(tokenInfoId, info);
		saveTokens();
	}
	 
	public synchronized void setDataSourceToken(String dataSourceId, String tokenId, OAuthProviderType provider) throws IOException {
		ensureTokens();
				
		OAuthToken previousToken;
		// A given datasourceId can only be associated with one token.
		// However, previous versions of this implementation did not take care of that appropriately. 
		// So, the following code considers the possibility of having multiple tokens for the datasource
		// to correct that situation. 
		// Implementations that do not need to 'migrate' data like this can just expect one Token.
		while ((previousToken = getDataSourceToken(dataSourceId, provider)) != null) { 
			OAuthTokenInfo info = tokens.getTokens().get(getTokenInfoId(previousToken.getId(), provider));
			info.getDataSources().remove(dataSourceId);
		}
		
		OAuthTokenInfo info = tokens.getTokens().get(getTokenInfoId(tokenId, provider));
		if (info != null && !info.getDataSources().contains(dataSourceId)) {
			info.getDataSources().add(dataSourceId);
			saveTokens();
		}
	}
	
	public synchronized OAuthToken getDataSourceToken(String dataSourceId, OAuthProviderType provider) throws IOException {
		ensureTokens();
		for (OAuthTokenInfo info : tokens.getTokens().values()) {
			if (info.getProvider() == provider && info.getDataSources().contains(dataSourceId)) {
				return info.getToken();
			}
		}
		//let's see if there's a token with the same id, this is sometimes used, for example for GA, the data source used
		//while browsing the metadata is created with the token id.
		return getToken(dataSourceId, provider);
	}	

	public synchronized void deleteToken(String tokenId, OAuthProviderType provider) throws IOException {
		ensureTokens();
		tokens.getTokens().remove(getTokenInfoId(tokenId, provider));
		saveTokens();
	}
	
	public synchronized void dataSourceDeleted(String dataSourceId, OAuthProviderType provider) throws IOException {
		ensureTokens();
		boolean deleted = false;
		for (OAuthTokenInfo info : tokens.getTokens().values()) {
			if (info.getProvider() == provider && info.getDataSources().contains(dataSourceId)) {
				info.getDataSources().remove(dataSourceId);
				deleted = true;
				break;
			}
		}
		if (!deleted) {
			//let's see if there's a token with the same id, this is sometimes used, for example for GA, the data source used
			//while browsing the metadata is created with the token id.
			deleteToken(dataSourceId, provider);
		}
		saveTokens();
	}	
	
	private void saveTokens() {		
		JsonbConfig config = new JsonbConfig();
		config.setProperty(JsonbConfig.FORMATTING, true);
		Jsonb jsonb = JsonbBuilder.create(config);
		new File(filePath).getParentFile().mkdirs();
		try (FileOutputStream out = new FileOutputStream(filePath)) {
			jsonb.toJson(tokens, out);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to save tokens.json file", e);
		}
		File jsonFile = new File(filePath);
		cacheTimestamp = jsonFile.lastModified();
	}
	
	private void ensureTokens() {
		File jsonFile = new File(filePath);
		if (!jsonFile.exists() || jsonFile.isDirectory() || !jsonFile.canRead()) {
			tokens = new OAuthTokensInfo();
			return;
		}
		
		if (cacheTimestamp != jsonFile.lastModified()) {
			if (cacheTimestamp > 0) {
				log.info("Detected changes in tokens.json, loading again");
			}
			tokens = loadFromJson(filePath);
			if (tokens == null) { //load failed
				tokens = new OAuthTokensInfo();
			} else {				
				cacheTimestamp = jsonFile.lastModified();
				
				log.info("Loaded " + tokens.getTokens().size() + " OAuth token(s)");
			}
		}
	}
	
	private static OAuthTokensInfo loadFromJson(String filePath) {
		Jsonb jsonb = JsonbBuilder.create();
		try {
			OAuthTokensInfo info = jsonb.fromJson(new FileInputStream(filePath), OAuthTokensInfo.class);
			return info;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to load tokens.json file", e);
			return null;
		}
	}
	
	protected static boolean sameUserId(String a, String b) {
		if (a == null) {
			return b == null;
		} else {
			return a.equals(b);
		}
	}
	
	protected String getTokenInfoId(String tokenId, OAuthProviderType provider) {
		return String.format("%s:%s", tokenId, provider);
	}

	public static class OAuthTokensInfo {
		private Map<String, OAuthTokenInfo> tokens;
		
		public OAuthTokensInfo() {			
			tokens = new HashMap<String, OAuthTokenInfo>();
		}

		public Map<String, OAuthTokenInfo> getTokens() {
			return tokens;
		}

		public void setTokens(Map<String, OAuthTokenInfo> tokens) {
			this.tokens = tokens;
		}
	}
	
	public static class OAuthTokenInfo {
		private OAuthToken token;
		private OAuthProviderType provider;
		private String tokenId;
		private List<String> dataSources;
		
		public OAuthTokenInfo() {			
		}
		
		public OAuthTokenInfo( String tokenId, OAuthProviderType provider, OAuthToken token) {
			super();
			this.tokenId = tokenId;
			this.provider = provider;
			this.token = token;
			this.dataSources = new ArrayList<String>();
		}

		public OAuthToken getToken() {
			return token;
		}

		public void setToken(OAuthToken token) {
			this.token = token;
		}

		public OAuthProviderType getProvider() {
			return provider;
		}

		public void setProvider(OAuthProviderType provider) {
			this.provider = provider;
		}

		public String getTokenId() {
			return tokenId;
		}

		public void setTokenId(String tokenId) {
			this.tokenId = tokenId;
		}

		public List<String> getDataSources() {
			return dataSources;
		}

		public void setDataSources(List<String> dataSources) {
			this.dataSources = dataSources;
		}
			
	}
}
