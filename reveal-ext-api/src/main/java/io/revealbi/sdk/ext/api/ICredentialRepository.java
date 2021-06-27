package io.revealbi.sdk.ext.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.infragistics.reveal.sdk.api.IRVAuthenticationResolver;

public interface ICredentialRepository extends IRVAuthenticationResolver {
	String saveCredentials(String userId, String id, Map<String, Object> json) throws IOException;
	boolean deleteCredentials(String userId, String id);
	List<Map<String, Object>> getCredentials(String userId) throws IOException;
	
	Map<String, Object> getDataSourceCredentials(String userId, String dataSourceId) throws IOException;
	void setDataSourceCredentials(String userId, String dataSourceId, String credentialsId) throws IOException;
	void dataSourceDeleted(String userId, String dataSourceId, String provider) throws IOException;
}
