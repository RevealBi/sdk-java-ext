package io.revealbi.sdk.ext.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import io.revealbi.sdk.ext.api.oauth.IOAuthTokenRepository;
import io.revealbi.sdk.ext.api.oauth.OAuthProviderType;
import io.revealbi.sdk.ext.api.oauth.OAuthToken;

public class FileSystemOAuthTokenRepository implements IOAuthTokenRepository {
	private static Logger log = Logger.getLogger(FileSystemDataSourcesRepository.class.getName());
	
	private String filePath;
	private OAuthTokensInfo tokens;
	private long cacheTimestamp;
	
	public FileSystemOAuthTokenRepository(String filePath) {
		this.filePath = filePath;
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
	
	private void saveTokens() {		
		JsonbConfig config = new JsonbConfig();
		config.setProperty(JsonbConfig.FORMATTING, true);
		Jsonb jsonb = JsonbBuilder.create(config);
		try (FileOutputStream out = new FileOutputStream(filePath)) {
			jsonb.toJson(tokens, out);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to save tokens.json file", e);
		}
		File jsonFile = new File(filePath);
		cacheTimestamp = jsonFile.lastModified();
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
	@Override
	public synchronized OAuthToken getToken(String userId, String dataSourceId, OAuthProviderType provider) throws IOException {
		ensureTokens();
		return tokens.getTokens().get(getTokenId(userId, dataSourceId, provider));
	}

	@Override
	public synchronized void saveToken(String userId, String dataSourceId, OAuthProviderType provider, OAuthToken token) throws IOException {
		ensureTokens();
		tokens.getTokens().put(getTokenId(userId, dataSourceId, provider), token);
		saveTokens();
	}
	
	protected String getTokenId(String userId, String dataSourceId, OAuthProviderType provider) {
		return String.format("%s:%s:%s",userId, dataSourceId, provider);
	}

	public static class OAuthTokensInfo {
		private Map<String, OAuthToken> tokens;
		
		public OAuthTokensInfo() {			
			tokens = new HashMap<String, OAuthToken>();
		}

		public Map<String, OAuthToken> getTokens() {
			return tokens;
		}

		public void setTokens(Map<String, OAuthToken> tokens) {
			this.tokens = tokens;
		}

	}
}
