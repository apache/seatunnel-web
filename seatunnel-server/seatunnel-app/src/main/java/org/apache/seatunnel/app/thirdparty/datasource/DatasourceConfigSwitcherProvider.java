package org.apache.seatunnel.app.thirdparty.datasource;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public enum DatasourceConfigSwitcherProvider {
    INSTANCE;

    private final Map<String, DataSourceConfigSwitcher> configSwitcherCache;

    DatasourceConfigSwitcherProvider() {
        ServiceLoader<DataSourceConfigSwitcher> loader =
                ServiceLoader.load(DataSourceConfigSwitcher.class);
        configSwitcherCache = new ConcurrentHashMap<>();

        for (DataSourceConfigSwitcher switcher : loader) {
            configSwitcherCache.put(switcher.getDataSourceName().toUpperCase(), switcher);
        }
    }

    public DataSourceConfigSwitcher getConfigSwitcher(String datasourceName) {
        return configSwitcherCache.get(datasourceName);
    }
}
