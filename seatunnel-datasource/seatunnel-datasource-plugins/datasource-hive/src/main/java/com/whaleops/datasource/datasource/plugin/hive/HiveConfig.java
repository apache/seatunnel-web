package com.whaleops.datasource.datasource.plugin.hive;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginInfo;
import org.apache.seatunnel.datasource.plugin.api.DatasourcePluginTypeEnum;

import com.google.common.collect.Sets;

import java.util.Set;

public class HiveConfig {
    public static final String PLUGIN_NAME = "Hive";

    public static final DataSourcePluginInfo HIVE_DATASOURCE_PLUGIN_INFO =
            DataSourcePluginInfo.builder()
                    .name(PLUGIN_NAME)
                    .icon(PLUGIN_NAME)
                    .version("1.0.0")
                    .type(DatasourcePluginTypeEnum.DATABASE.getCode())
                    .build();

    public static final Set<String> HIVE_SYSTEM_DATABASES = Sets.newHashSet();

    public static final OptionRule OPTION_RULE =
            OptionRule.builder().required(HiveOptionRule.METASTORE_URI).build();
}
