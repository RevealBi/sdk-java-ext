package io.revealbi.sdk.ext.rest;

import java.util.HashMap;
import java.util.Map;

public class GenericCredentials {
	private String accountId;
	private String accountName;
	private String domain;
	private String userName;
	private String password;
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}	
	public Map<String, Object> toJson() {
		Map<String, Object> json = new HashMap<String, Object>();
		putValue(json, "accountId", accountId);
		putValue(json, "accountName", accountName);
		putValue(json, "domain", domain);
		putValue(json, "userName", userName);
		putValue(json, "password", password);
		return json;
	}
	
	static void putValue(Map<String, Object> json, String key, Object value) {
		if (value == null) {
			return;
		}
		json.put(key, value);
	}
}
