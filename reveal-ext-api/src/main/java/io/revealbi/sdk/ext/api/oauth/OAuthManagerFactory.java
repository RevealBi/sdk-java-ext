package io.revealbi.sdk.ext.api.oauth;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import io.revealbi.sdk.ext.oauth.BaseOAuthManager;

public class OAuthManagerFactory {
	private static IOAuthManager instance = new BaseOAuthManager();
	private static Logger log = Logger.getLogger(OAuthManagerFactory.class.getSimpleName());
	
	public static void setInstance(IOAuthManager instance) {
		OAuthManagerFactory.instance = instance;
	}
	
	public static IOAuthManager getInstance() {
		return instance;
	}
	
	public static void registerProviders(String jsonFilePath) {
		jsonFilePath = StringSubstitutor.replaceSystemProperties(jsonFilePath);
		if (jsonFilePath == null) {
			return;
		}
		File jsonFile = new File(jsonFilePath);
		if (!jsonFile.exists()) {
			log.warning("OAuth configuration file not found at: " + jsonFilePath);
			return;
		}
		if (!jsonFile.canRead()) {
			log.warning("OAuth configuration file is not readable");
			return;
		}
		
		try {
			Jsonb json = JsonbBuilder.create();
			try (InputStream in = new FileInputStream(jsonFile)) {
				OAuthProvidersConfig providersConfig = json.fromJson(in, OAuthProvidersConfig.class);
				Map<String, OAuthProviderConfig> providers = providersConfig.providers;
				if (providers == null || providers.size() == 0) {
					log.info("No OAuth providers specified in OAuth configuration file");
				} else {
					for (String provider : providers.keySet()) {
						OAuthProviderType providerType = OAuthProviderType.valueOf(provider);
						if (providerType == null) {
							log.warning("Ignoring OAuth configuration for " + provider + ". Invalid provider name");
							continue;
						}
						OAuthProviderConfig config = providers.get(provider);
						if (StringUtils.isBlank(config.clientId) || StringUtils.isBlank(config.clientSecret) || StringUtils.isBlank(config.redirectUri)) {
							log.warning("Ignoring OAuth configuration for " + provider + ". All fields are required: clientId, clientSecret and redirectUri");
							continue;
						}					
						getInstance().registerProvider(providerType, config.clientId, config.clientSecret, config.redirectUri);
						log.info(provider + " provider registered with redirect URI: " + config.redirectUri);
					}				
				}
			}
		} catch (Exception exc) {
			log.log(Level.SEVERE, "Failed to read OAuth configuration file: " + exc, exc);
		}
	}
	
	
	public static class OAuthProvidersConfig {
		public Map<String, OAuthProviderConfig> providers;
	}
	
	public static class OAuthProviderConfig {
		public String clientSecret;
		public String clientId;
		public String redirectUri;
	}
}
