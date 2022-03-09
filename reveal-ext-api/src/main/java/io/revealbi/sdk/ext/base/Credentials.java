package io.revealbi.sdk.ext.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Credentials {
	String id;
	private String userName;
	private String domain;
	private String password;		
	private String accountName;
	List<String> dataSources;
	private Map<String, Object> oauthDefinition;  
	private Map<String, Object> sensitive;
	
	public Credentials() {			
	}
	
	@SuppressWarnings("unchecked")
	public Credentials(Map<String, Object> json) {
		this.id = (String)json.get("accountId");
		this.domain = (String)json.get("domain");
		this.userName = (String)json.get("userName");
		this.password = (String)json.get("password");
		this.accountName = (String)json.get("accountName");
		this.setOauthDefinition((Map<String, Object>) json.get("oauthDefinition"));
		this.setSensitive((Map<String, Object>) json.get("sensitive"));
	}
	
	public Map<String, Object> toJson() { 
		Map<String, Object> json = new HashMap<String, Object>();
		putValue(json, "accountId", id);
		putValue(json, "accountName", accountName);
		putValue(json, "domain", domain);
		putValue(json, "userName", userName);
		putValue(json, "oauthDefinition", oauthDefinition);
		putValue(json, "sensitive", sensitive);
		putValue(json, "password", password);
		return json;
	}
	
	private static void putValue(Map<String, Object> json, String key, Object value) {
		if (value == null) {
			return;
		}
		json.put(key, value);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}		
	public List<String> getDataSources() {
		return dataSources;
	}
	public void setDataSources(List<String> dataSources) {
		this.dataSources = dataSources;
	}		
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public boolean isUsedByDataSource(String dataSourceId) {
		return dataSources != null && dataSources.contains(dataSourceId);
	}
	public void addDataSource(String dsId) {
		if (dsId == null) {
			return;
		}
		if (dataSources == null) {
			dataSources = new ArrayList<String>();				
		} else if (dataSources.contains(dsId)) {
			return;
		}
		dataSources.add(dsId);
	}
	public void removeDataSource(String dataSourceId) {
		if (dataSources == null || dataSourceId == null) {
			return;
		}
		dataSources.remove(dataSourceId);
	}

	public Map<String, Object> getOauthDefinition() {
		return oauthDefinition;
	}

	public void setOauthDefinition(Map<String, Object> oauthDefinition) {
		this.oauthDefinition = oauthDefinition;
	}

	public Map<String, Object> getSensitive() {
		return sensitive;
	}

	public void setSensitive(Map<String, Object> sensitive) {
		this.sensitive = sensitive;
	}
	
}