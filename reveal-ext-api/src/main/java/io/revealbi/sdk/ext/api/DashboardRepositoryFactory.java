package io.revealbi.sdk.ext.api;

public class DashboardRepositoryFactory {
	private static IDashboardRepository instance;
	
	public static IDashboardRepository getInstance() {
		return instance;
	}
	
	public static void setInstance(IDashboardRepository instance) {
		DashboardRepositoryFactory.instance = instance;
	}
	
}
