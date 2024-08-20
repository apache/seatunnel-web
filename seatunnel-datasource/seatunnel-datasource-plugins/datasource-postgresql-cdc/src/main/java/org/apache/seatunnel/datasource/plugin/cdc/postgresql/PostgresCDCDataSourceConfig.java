package org.apache.seatunnel.datasource.plugin.cdc.postgresql;

import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginInfo;
import org.apache.seatunnel.datasource.plugin.api.DatasourcePluginTypeEnum;

public class PostgresCDCDataSourceConfig {

    public static final String PLUGIN_NAME = "Postgres-CDC";

    public static final DataSourcePluginInfo POSTGRES_CDC_DATASOURCE_PLUGIN_INFO =
            DataSourcePluginInfo.builder()
                    .name(PLUGIN_NAME)
                    .icon(PLUGIN_NAME)
                    .version("1.0")
                    .type(DatasourcePluginTypeEnum.DATABASE.getCode())
                    .build();
}
