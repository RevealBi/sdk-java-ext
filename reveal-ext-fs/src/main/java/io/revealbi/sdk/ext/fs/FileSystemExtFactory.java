package io.revealbi.sdk.ext.fs;

import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.text.StringSubstitutor;

import io.revealbi.sdk.ext.api.CredentialRepositoryFactory;
import io.revealbi.sdk.ext.api.DashboardRepositoryFactory;
import io.revealbi.sdk.ext.api.DataSourcesRepositoryFactory;
import io.revealbi.sdk.ext.api.IDashboardRepository;
import io.revealbi.sdk.ext.api.oauth.OAuthTokenRepositoryFactory;

/**
 * Helper class to be used when you want to use all services from File System.
 * 
 * See the three providers used for more information:
 * {@link FileSystemCredentialRepository}, {@link FileSystemDashboardRepository} and {@link FileSystemDataSourcesRepository}
 */
public class FileSystemExtFactory {
	private static Logger log = Logger.getLogger(FileSystemExtFactory.class.getSimpleName());
	
	/**
	 * Registers dashboards, credentials and data sources file system providers, using the provided root path.
	 * Dashboards will be stored under {rootPath}/dashboards.
	 * Data sources will be loaded from {rootPath}/datasources.json.
	 * Credentials will be loaded from {rootPath}/credentials.json.
	 * 
	 * See the three providers used for more information:
	 * {@link FileSystemCredentialRepository}, {@link FileSystemDashboardRepository} and {@link FileSystemDataSourcesRepository}
	 * 
	 * @param rootDir The parent directory to be used to store all files, write permission is required. It supports system properties with the syntax ${property}, like ${user.home}/folder/
	 * @param personal A boolean flag indicating if data should be personal or global for all users.
	 */
	public static void registerAllServices(String rootDir, boolean personal) {
		rootDir = StringSubstitutor.replaceSystemProperties(rootDir);
		DashboardRepositoryFactory.setInstance(new FileSystemDashboardRepository(getDashboardsRootDir(rootDir), personal));
		CredentialRepositoryFactory.setInstance(new FileSystemCredentialRepository(getCredentialsRootDir(rootDir, personal), personal));
		DataSourcesRepositoryFactory.setInstance(new FileSystemDataSourcesRepository(getDataSourcesRootPath(rootDir, personal), personal));
		OAuthTokenRepositoryFactory.setInstance(new FileSystemOAuthTokenRepository(getOAuthTokensRootDir(rootDir, personal), personal));
	}
	
	public static void installSampleDashboards(String userId, Class<?> clazz, String[] resources) {
		IDashboardRepository repository = DashboardRepositoryFactory.getInstance();
		if (!(repository instanceof FileSystemDashboardRepository)) {
			log.warning("Skipping installation of sample dashboards, invalid repository");
			return;
		}
		if (clazz == null) {
			log.warning("Skipping installation of sample dashboards, null class specified");
			return;
		}
		if (resources == null || resources.length == 0) {
			log.warning("Skipping installation of sample dashboards, no resources specified");
			return;
		}
		((FileSystemDashboardRepository)repository).installSampleDashboards(userId, clazz, resources);
	}
	
	private static String getDashboardsRootDir(String rootDir) {
		return new File(rootDir, "dashboards").getAbsolutePath();
	}
	private static String getCredentialsRootDir(String rootDir, boolean personal) {
		if (!personal) {
			return rootDir;
		}
		return new File(rootDir, "credentials").getAbsolutePath();
	}	
	private static String getDataSourcesRootPath(String rootDir, boolean personal) {
		if (!personal) {
			return rootDir;
		}
		return new File(rootDir, "datasources").getAbsolutePath();
	}
	private static String getOAuthTokensRootDir(String rootDir, boolean personal) {
		if (!personal) {
			return rootDir;
		}
		return new File(rootDir, "tokens").getAbsolutePath();
	}

}
