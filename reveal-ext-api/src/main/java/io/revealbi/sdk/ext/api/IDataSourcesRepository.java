package io.revealbi.sdk.ext.api;

import java.io.IOException;
import java.util.Map;

public interface IDataSourcesRepository {
	DataSourcesInfo getUserDataSources(String userId) throws IOException;
	void saveDataSource(String userId, String dataSourceId, Map<String, Object> json) throws IOException;
}
