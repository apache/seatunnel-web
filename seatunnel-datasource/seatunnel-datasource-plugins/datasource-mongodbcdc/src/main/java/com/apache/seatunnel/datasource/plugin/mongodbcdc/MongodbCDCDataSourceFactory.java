package com.apache.seatunnel.datasource.plugin.mongodbcdc;

import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourceFactory;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginInfo;
import org.apache.seatunnel.datasource.plugin.api.DatasourcePluginTypeEnum;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;

import java.util.Set;

@AutoService(DataSourceFactory.class)
public class MongodbCDCDataSourceFactory implements DataSourceFactory {
    public static final String MONGO_PLUGIN_NAME = "MongoDB-CDC";
    public static final String MONGO_PLUGIN_ICON = "MongoDB-CDC";
    public static final String MONGO_PLUGIN_VERSION = "1.0.0";

    @Override
    public String factoryIdentifier() {
        return MONGO_PLUGIN_NAME;
    }

    @Override
    public Set<DataSourcePluginInfo> supportedDataSources() {
        return Sets.newHashSet(
                DataSourcePluginInfo.builder()
                        .name(MONGO_PLUGIN_NAME)
                        .icon(MONGO_PLUGIN_ICON)
                        .version(MONGO_PLUGIN_VERSION)
                        .supportVirtualTables(true)
                        .type(DatasourcePluginTypeEnum.NO_STRUCTURED.getCode())
                        .build());
    }

    @Override
    public DataSourceChannel createChannel() {
        return new MongodbCDCDataSourceChannel();
    }
}
