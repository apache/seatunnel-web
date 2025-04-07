package com.apache.seatunnel.datasource.plugin.iceberg;

import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourceFactory;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginInfo;
import org.apache.seatunnel.datasource.plugin.api.DatasourcePluginTypeEnum;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;

import java.util.Set;

@AutoService(DataSourceFactory.class)
public class IcebergDataSourceFactory implements DataSourceFactory {
    public static final String ICEBERG_PLUGIN_NAME = "Iceberg";
    public static final String ICEBERG_PLUGIN_ICON = "Iceberg";
    public static final String ICEBERG_PLUGIN_VERSION = "1.0.0";

    @Override
    public String factoryIdentifier() {
        return ICEBERG_PLUGIN_NAME;
    }

    @Override
    public Set<DataSourcePluginInfo> supportedDataSources() {
        return Sets.newHashSet(
                DataSourcePluginInfo.builder()
                        .name(ICEBERG_PLUGIN_NAME)
                        .icon(ICEBERG_PLUGIN_ICON)
                        .version(ICEBERG_PLUGIN_VERSION)
                        .supportVirtualTables(false)
                        .type(DatasourcePluginTypeEnum.NO_STRUCTURED.getCode())
                        .build());
    }

    @Override
    public DataSourceChannel createChannel() {
        return new IcebergDataSourceChannel();
    }
}
