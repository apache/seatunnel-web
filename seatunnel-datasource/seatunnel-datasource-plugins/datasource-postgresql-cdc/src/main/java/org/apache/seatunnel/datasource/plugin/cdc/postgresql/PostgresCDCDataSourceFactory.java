package org.apache.seatunnel.datasource.plugin.cdc.postgresql;

import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourceFactory;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginInfo;

import com.google.auto.service.AutoService;

import java.util.Collections;
import java.util.Set;

@AutoService(DataSourceFactory.class)
public class PostgresCDCDataSourceFactory implements DataSourceFactory {

    @Override
    public String factoryIdentifier() {
        return "Postgres-CDC";
    }

    @Override
    public Set<DataSourcePluginInfo> supportedDataSources() {
        return Collections.singleton(
                PostgresCDCDataSourceConfig.POSTGRES_CDC_DATASOURCE_PLUGIN_INFO);
    }

    @Override
    public DataSourceChannel createChannel() {
        return new PostgresCDCDataSourceChannel();
    }
}
