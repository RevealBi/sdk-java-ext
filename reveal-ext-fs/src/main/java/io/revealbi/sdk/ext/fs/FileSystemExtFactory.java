package io.revealbi.sdk.ext.fs;

import java.io.File;

import io.revealbi.sdk.ext.api.CredentialRepositoryFactory;
import io.revealbi.sdk.ext.api.DashboardRepositoryFactory;
import io.revealbi.sdk.ext.api.DataSourcesRepositoryFactory;
import io.revealbi.sdk.ext.api.oauth.OAuthTokenRepositoryFactory;

/**
 * Helper class to be used when you want to use all services from File System.
 * 
 * See the three providers used for more information:
 * {@link FileSystemCredentialRepository}, {@link FileSystemDashboardRepository} and {@link FileSystemDataSourcesRepository}
 */
public class FileSystemExtFactory {
	/**
	 * Registers dashboards, credentials and data sources file system providers, using the provided root path.
	 * Dashboards will be stored under {rootPath}/dashboards.
	 * Data sources will be loaded from {rootPath}/datasources.json.
	 * Credentials will be loaded from {rootPath}/credentials.json.
	 * 
	 * See the three providers used for more information:
	 * {@link FileSystemCredentialRepository}, {@link FileSystemDashboardRepository} and {@link FileSystemDataSourcesRepository}
	 * 
	 * @param rootDir The parent directory to be used to store all files, write permission is required.
	 * @param personalDashboards A boolean flag indicating if dashboards should be personal or global for all users.
	 */
	public static void registerAllServices(String rootDir, boolean personalDashboards) {
		DashboardRepositoryFactory.setInstance(new FileSystemDashboardRepository(getDashboardsRootDir(rootDir), personalDashboards));
		CredentialRepositoryFactory.setInstance(new FileSystemCredentialRepository(getCredentialsFilePath(rootDir)));
		DataSourcesRepositoryFactory.setInstance(new FileSystemDataSourcesRepository(getDataSourcesFilePath(rootDir)));
		OAuthTokenRepositoryFactory.setInstance(new FileSystemOAuthTokenRepository(getOAuthTokensFilePath(rootDir)));
	}
	private static String getDashboardsRootDir(String rootDir) {
		return new File(rootDir, "dashboards").getAbsolutePath();
	}
	private static String getCredentialsFilePath(String rootDir) {
		return new File(rootDir, "credentials.json").getAbsolutePath();
	}

	private static String getDataSourcesFilePath(String rootDir) {
		return new File(rootDir, "datasources.json").getAbsolutePath();
	}
	
	private static String getOAuthTokensFilePath(String rootDir) {
		return new File(rootDir, "tokens.json").getAbsolutePath();
	}

}
