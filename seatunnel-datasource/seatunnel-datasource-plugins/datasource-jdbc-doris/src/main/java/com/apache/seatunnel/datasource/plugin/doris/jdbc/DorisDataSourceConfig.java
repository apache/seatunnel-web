package com.apache.seatunnel.datasource.plugin.doris.jdbc;

import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginInfo;
import org.apache.seatunnel.datasource.plugin.api.DatasourcePluginTypeEnum;

import com.google.common.collect.Sets;

import java.util.Set;

public class DorisDataSourceConfig {

    public static final String PLUGIN_NAME = "Doris";

    public static final DataSourcePluginInfo DORIS_DATASOURCE_PLUGIN_INFO =
            DataSourcePluginInfo.builder()
                    .name(PLUGIN_NAME)
                    .icon(PLUGIN_NAME)
                    .version("1.0.0")
                    .type(DatasourcePluginTypeEnum.DATABASE.getCode())
                    .build();

    public static final Set<String> DORIS_SYSTEM_DATABASES =
            Sets.newHashSet("information_schema", "__internal_schema");
}
