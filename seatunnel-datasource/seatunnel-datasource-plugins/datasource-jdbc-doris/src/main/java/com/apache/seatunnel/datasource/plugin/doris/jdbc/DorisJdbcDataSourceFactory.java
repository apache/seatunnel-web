package com.apache.seatunnel.datasource.plugin.doris.jdbc;

import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourceFactory;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginInfo;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@AutoService(DataSourceFactory.class)
public class DorisJdbcDataSourceFactory implements DataSourceFactory {

    @Override
    public String factoryIdentifier() {
        return DorisDataSourceConfig.PLUGIN_NAME;
    }

    @Override
    public Set<DataSourcePluginInfo> supportedDataSources() {
        return Sets.newHashSet(DorisDataSourceConfig.DORIS_DATASOURCE_PLUGIN_INFO);
    }

    @Override
    public DataSourceChannel createChannel() {
        return new DorisJdbcDataSourceChannel();
    }
}
