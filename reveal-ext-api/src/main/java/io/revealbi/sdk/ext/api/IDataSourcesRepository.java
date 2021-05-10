package io.revealbi.sdk.ext.api;

import java.io.IOException;

public interface IDataSourcesRepository {
	DataSourcesInfo getUserDataSources(String userId) throws IOException;
}
