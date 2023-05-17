/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.app.thirdpart.datasource.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.app.domain.request.connector.BusinessMode;
import org.apache.seatunnel.app.domain.request.job.DataSourceOption;
import org.apache.seatunnel.app.domain.request.job.SelectTableFields;
import org.apache.seatunnel.app.domain.response.datasource.VirtualTableDetailRes;
import org.apache.seatunnel.app.dynamicforms.FormStructure;
import org.apache.seatunnel.app.thirdpart.datasource.AbstractDataSourceConfigSwitcher;
import org.apache.seatunnel.common.constants.PluginType;
import org.apache.seatunnel.shade.com.typesafe.config.Config;
import org.apache.seatunnel.shade.com.typesafe.config.ConfigFactory;
import org.apache.seatunnel.shade.com.typesafe.config.ConfigValueFactory;

import java.util.ArrayList;
import java.util.List;

public class KafkaKingbaseDataSourceConfigSwitcher extends AbstractDataSourceConfigSwitcher {

    private static final KafkaKingbaseDataSourceConfigSwitcher INSTANCE =
            new KafkaKingbaseDataSourceConfigSwitcher();

    private static final String SCHEMA = "schema";
    private static final String TOPIC = "topic";
    private static final String FORMAT = "format";
    private static final String PATTERN = "pattern";

    private static final String FACTORY = "factory";

    private static final String CATALOG = "catalog";

    private static final String TABLE_NAMES = "table-names";

    private static final String URL = "url";

    private static final String USER = "user";

    private static final String PASSWORD = "password";

    private static final String DATABASE_NAMES = "database-names";

    @Override
    public FormStructure filterOptionRule(
            String connectorName,
            OptionRule dataSourceOptionRule,
            OptionRule virtualTableOptionRule,
            BusinessMode businessMode,
            PluginType pluginType,
            OptionRule connectorOptionRule,
            List<String> excludedKeys) {
        if (pluginType == PluginType.SOURCE) {
            excludedKeys.add(SCHEMA);
            excludedKeys.add(TOPIC);
            excludedKeys.add(PATTERN);
            excludedKeys.add(FORMAT);
        } else {
            throw new UnsupportedOperationException("Unsupported plugin type: " + pluginType);
        }
        return super.filterOptionRule(
                connectorName,
                dataSourceOptionRule,
                virtualTableOptionRule,
                businessMode,
                pluginType,
                connectorOptionRule,
                excludedKeys);
    }

    @Override
    public Config mergeDatasourceConfig(
            Config dataSourceInstanceConfig,
            VirtualTableDetailRes virtualTableDetail,
            DataSourceOption dataSourceOption,
            SelectTableFields selectTableFields,
            BusinessMode businessMode,
            PluginType pluginType,
            Config connectorConfig) {
        if (pluginType == PluginType.SOURCE) {
            // Use field to generate the schema
            String topics = String.join(",", dataSourceOption.getDatabases());
            connectorConfig =
                    connectorConfig.withValue(TOPIC, ConfigValueFactory.fromAnyRef(topics));

            connectorConfig =
                    connectorConfig.withValue(
                            FORMAT, ConfigValueFactory.fromAnyRef("KINGBASE_JSON"));
            connectorConfig =
                    connectorConfig.withValue(
                            DATABASE_NAMES,
                            ConfigValueFactory.fromIterable(dataSourceOption.getDatabases()));
            connectorConfig =
                    connectorConfig.withValue(
                            TABLE_NAMES,
                            ConfigValueFactory.fromIterable(
                                    mergeDatabaseAndTables(dataSourceOption)));
            Config config = ConfigFactory.empty();
            config = config.withValue(FACTORY, ConfigValueFactory.fromAnyRef("kingbase"));
            config = config.withValue(URL, dataSourceInstanceConfig.getValue(URL));
            config = config.withValue(USER, dataSourceInstanceConfig.getValue(USER));
            config = config.withValue(PASSWORD, dataSourceInstanceConfig.getValue(PASSWORD));
            connectorConfig = connectorConfig.withValue(CATALOG, config.root());
        } else {
            throw new UnsupportedOperationException("Unsupported plugin type: " + pluginType);
        }

        return super.mergeDatasourceConfig(
                dataSourceInstanceConfig,
                virtualTableDetail,
                dataSourceOption,
                selectTableFields,
                businessMode,
                pluginType,
                connectorConfig);
    }

    private static List<String> mergeDatabaseAndTables(DataSourceOption dataSourceOption) {
        List<String> tables = new ArrayList<>();
        dataSourceOption
                .getDatabases()
                .forEach(
                        database -> {
                            dataSourceOption
                                    .getTables()
                                    .forEach(
                                            table -> {
                                                if (StringUtils.countMatches(table, ".") > 1) {
                                                    tables.add(table);
                                                } else {
                                                    tables.add(
                                                            getDatabaseAndTable(database, table));
                                                }
                                            });
                        });
        return tables;
    }

    private static String getDatabaseAndTable(String database, String table) {
        return String.format("%s.%s", database, table);
    }

    private KafkaKingbaseDataSourceConfigSwitcher() {}

    public static KafkaKingbaseDataSourceConfigSwitcher getInstance() {
        return INSTANCE;
    }
}
