package io.revealbi.sdk.ext.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.infragistics.controls.IOUtils;

import io.revealbi.sdk.ext.api.DashboardInfo;
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
	public InputStream getDashboard(String userId, String dashboardId) throws IOException {
		String path = getDashboardPath(userId, dashboardId);
		File file = new File(path);
		if (!file.exists() || !file.canRead() || !file.isFile()) {
			path = getDashboardPath(null, dashboardId);
		}
		file = new File(path);
		if (!file.exists() || !file.canRead() || !file.isFile()) {
			return null;
		}
		return new FileInputStream(path);
	}	

	@Override
	public void saveDashboard(String userId, String dashboardId, InputStream dashboardStream) throws IOException {
		String path = getDashboardPath(userId, dashboardId);
		File file = new File(path);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		try (FileOutputStream out = new FileOutputStream(file)) {
			IOUtils.copy(dashboardStream, out);
		}
	}
	
	public DashboardInfo getDashboardInfo(String userId, String dashboardId) throws IOException {
		return super.getDashboardInfo(userId, dashboardId);
	}
	
	@Override
	public void deleteDashboard(String userId, String dashboardId) throws IOException {
		String path = getDashboardPath(userId, dashboardId);
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
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
	
	private static String getDashboardId(String fileName) {
		if (fileName.endsWith(".rdash")) {
			return fileName.substring(0, fileName.length() - ".rdash".length());
		}
		return fileName;
	}
}
