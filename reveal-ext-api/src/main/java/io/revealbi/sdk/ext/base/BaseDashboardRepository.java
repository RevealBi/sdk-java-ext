package io.revealbi.sdk.ext.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.infragistics.reveal.sdk.api.model.RVDashboardSummary;
import com.infragistics.reveal.sdk.util.RVSerializationUtilities;

import io.revealbi.sdk.ext.api.DashboardInfo;
import io.revealbi.sdk.ext.api.IDashboardRepository;

public abstract class BaseDashboardRepository implements IDashboardRepository {
	@Override
	public DashboardInfo[] getUserDashboards(String userId) throws IOException {
		String[] ids = getUserDashboardIds(userId);
		
		if (ids == null || ids.length == 0) {
			return new DashboardInfo[0];
		}
		
		List<DashboardInfo> result = new ArrayList<DashboardInfo>();
		for (String id : ids) {
			DashboardInfo info = getDashboardInfo(userId, id);
			if (info != null) {
				result.add(info);
			}
		}
		result.sort(new Comparator<DashboardInfo>() {
			@Override
			public int compare(DashboardInfo d1, DashboardInfo d2) {
				return d1.getDisplayName().compareToIgnoreCase(d2.getDisplayName());
			}
		});
		return result.toArray(new DashboardInfo[result.size()]);
	}
	
	protected DashboardInfo getDashboardInfo(String userId, String dashboardId) throws IOException {
		InputStream in = getDashboard(userId, dashboardId);
		if (in == null) {
			return null;
		}
		RVDashboardSummary summary = RVSerializationUtilities.getDashboardSummary(in);
		if (summary == null) {
			return null;
		}
		return new DashboardInfo(dashboardId, summary.toJson());

	}

	protected abstract String[] getUserDashboardIds(String userId) throws IOException;
	
}
