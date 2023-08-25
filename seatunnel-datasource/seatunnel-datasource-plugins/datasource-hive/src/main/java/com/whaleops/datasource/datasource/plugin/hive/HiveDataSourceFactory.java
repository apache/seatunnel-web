package com.whaleops.datasource.datasource.plugin.hive;

import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourceFactory;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginInfo;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class HiveDataSourceFactory implements DataSourceFactory {
    @Override
    public String factoryIdentifier() {
        return "Hive";
    }

    @Override
    public Set<DataSourcePluginInfo> supportedDataSources() {
        return Sets.newHashSet(HiveConfig.HIVE_DATASOURCE_PLUGIN_INFO);
    }

    @Override
    public DataSourceChannel createChannel() {
        return new HiveDataSourceChannel();
    }
}
