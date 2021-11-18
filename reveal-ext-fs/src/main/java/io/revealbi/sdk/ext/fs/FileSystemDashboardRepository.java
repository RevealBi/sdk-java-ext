package io.revealbi.sdk.ext.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.infragistics.controls.IOUtils;
import com.infragistics.reveal.sdk.api.IRVUserContext;

import io.revealbi.sdk.ext.base.BaseDashboardRepository;

/**
 * Dashboards repository implementation that loads and saves dashboards as ".rdash" files in the file system.
 * Under the root directory specified when this repository is created, folders are created for each user (please
 * note user ids are assumed to be valid names for directories in the file system) and dashboards are stored there.
 * 
 * For the dashboard file, the dashboard id is used as the name of the file (with extension .rdash), which means
 * that dashboard ids are also assumed to be valid names for files in the file system.
 */
public class FileSystemDashboardRepository extends BaseDashboardRepository {
	private static Logger log = Logger.getLogger(FileSystemDashboardRepository.class.getSimpleName());
	
	private String rootDir;
	private boolean personal;
	
	/**
	 * Creates a new instance of the dashboards repository using the specified root directory. If personal is set to 
	 * true then dashboards will be stored under a separated directory for each user.
	 * @param rootDir The root directory to used to store dashboards
	 * @param personal If true dashboards will be personal and dashboards created by a given user will not be accessible for others. If false a single list of dashboards will be used. 
	 */
	public FileSystemDashboardRepository(String rootDir, boolean personal) {
		this.rootDir = rootDir;
		this.personal = personal;
	}
	
	@Override
	public InputStream getDashboard(IRVUserContext userContext, String dashboardId) throws IOException {
		String path = getDashboardPath(userContext, dashboardId);
		File file = new File(path);
		if (!file.exists() || !file.canRead() || !file.isFile()) {
			path = getDashboardPath((String)null, dashboardId);
		}
		file = new File(path);
		if (!file.exists() || !file.canRead() || !file.isFile()) {
			return null;
		}
		return new FileInputStream(path);
	}	

	@Override
	public void saveDashboard(IRVUserContext userContext, String dashboardId, InputStream dashboardStream) throws IOException {
		String path = getDashboardPath(userContext, dashboardId);
		File file = new File(path);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		try (FileOutputStream out = new FileOutputStream(file)) {
			IOUtils.copy(dashboardStream, out);
		}
	}
	
	@Override
	public void deleteDashboard(String userId, String dashboardId) throws IOException {
		String path = getDashboardPath(userId, dashboardId);
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
	}
	
	private String getDashboardPath(IRVUserContext userContext, String dashboardId) {
		String userId = userContext.getUserId();
		return getDashboardPath(userId, dashboardId);
	}
	
	private String getDashboardPath(String userId, String dashboardId) {
		File userDir = (userId == null || !personal) ? new File(rootDir) : new File(rootDir, userId);
		if (dashboardId == null) {
			return userDir.getAbsolutePath();
		}
		return new File(userDir, dashboardId + ".rdash").getAbsolutePath();

	}

	@Override
	protected String[] getUserDashboardIds(String userId) throws IOException {
		String path = getDashboardPath(userId, null);
		File userDir = new File(path);
		if (!userDir.exists() || !userDir.isDirectory()) {
			return null;
		}
		List<String> ids = new ArrayList<String>();
		for (File f : userDir.listFiles()) {
			if (f.isDirectory() || !f.canRead() || !f.getName().endsWith(".rdash")) {
				continue;
			}
			ids.add(getDashboardId(f.getName()));
		}
		return ids.toArray(new String[ids.size()]);
	}
	
	public void installSampleDashboards(IRVUserContext userContext, Class<?> clazz, String[] resources) {
		String userPath = getDashboardPath(userContext, null);
		File userDir = new File(userPath);
		if (userDir.exists()) {
			log.fine("Skipping installation of sample dashboards, folder already exists");
			return;
		}
		boolean ok = userDir.mkdirs();
		if (!ok) {
			log.warning("Failed to create directory " + userPath);
			return;
		}
		for (String resource : resources) {
			InputStream in = clazz.getResourceAsStream(resource);
			if (in == null) {
				log.warning("Resource " + resource + " for sample dashboard not found, skipping.");
				continue;
			}
			try {
				String dashboardName = getDashboardNameFromResource(resource); 
				saveDashboard(userContext, dashboardName, in);
				String userId = userContext.getUserId();
				log.info("Installed sample dashboard: " + dashboardName + " for " + (userId == null ? "all users" : "user " + userId));
				in.close();
			} catch (IOException exc) {
				log.warning("Failed to save sample dashboard at " + resource + ": " + exc);
			}
		}
	}
	
	private static String getDashboardNameFromResource(String resource) {
		File f = new File(resource);
		String name = f.getName();
		int lastDot = name.lastIndexOf('.');
		if (lastDot > 0) {
			return name.substring(0, lastDot);
		} else {
			return name;
		}
	}
	
	private static String getDashboardId(String fileName) {
		if (fileName.endsWith(".rdash")) {
			return fileName.substring(0, fileName.length() - ".rdash".length());
		}
		return fileName;
	}
}
