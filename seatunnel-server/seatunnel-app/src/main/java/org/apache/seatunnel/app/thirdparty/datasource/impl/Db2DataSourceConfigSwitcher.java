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

package org.apache.seatunnel.app.thirdparty.datasource.impl;

import org.apache.seatunnel.shade.com.typesafe.config.Config;
import org.apache.seatunnel.shade.com.typesafe.config.ConfigFactory;
import org.apache.seatunnel.shade.com.typesafe.config.ConfigValueFactory;

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.app.domain.request.connector.BusinessMode;
import org.apache.seatunnel.app.domain.request.job.DataSourceOption;
import org.apache.seatunnel.app.domain.request.job.SelectTableFields;
import org.apache.seatunnel.app.domain.response.datasource.VirtualTableDetailRes;
import org.apache.seatunnel.app.thirdparty.datasource.DataSourceConfigSwitcher;
import org.apache.seatunnel.common.constants.PluginType;

import com.google.auto.service.AutoService;

import java.util.List;
import java.util.Optional;

import static org.apache.seatunnel.app.domain.request.connector.BusinessMode.DATA_INTEGRATION;
import static org.apache.seatunnel.app.domain.request.connector.BusinessMode.DATA_REPLICA;

@AutoService(DataSourceConfigSwitcher.class)
public class Db2DataSourceConfigSwitcher extends BaseJdbcDataSourceConfigSwitcher {

    private static final String QUERY_KEY = "query";

    private static final String GENERATE_SINK_SQL = "generate_sink_sql";

    private static final String URL_KEY = "url";

    // for catalog
    private static final String CATALOG = "catalog";
    private static final String FACTORY = "factory";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String BASE_URL = "base-url";
    private static final String CATALOG_SCHEMA = "schema";

    private static final Option<String> DATABASE_SCHEMA =
            Options.key("database_schema")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("the default database used during automated table creation.");
    private static final String CATALOG_NAME = "Db2";

    public Db2DataSourceConfigSwitcher() {}

    protected Optional<String> getCatalogName() {
        return Optional.of(CATALOG_NAME);
    }

    protected boolean isSupportPrefixOrSuffix() {
        return true;
    }

    protected boolean isSupportToggleCase() {
        return true;
    }

    public Config mergeDatasourceConfig(
            Config dataSourceInstanceConfig,
            VirtualTableDetailRes virtualTableDetail,
            DataSourceOption dataSourceOption,
            SelectTableFields selectTableFields,
            BusinessMode businessMode,
            PluginType pluginType,
            Config connectorConfig) {

        // 替换url中的database
        if (dataSourceOption.getDatabases().size() == 1) {
            String databaseName = dataSourceOption.getDatabases().get(0);
            String url = dataSourceInstanceConfig.getString(URL_KEY);
            String newUrl = replaceDatabaseNameInUrl(url, databaseName);
            dataSourceInstanceConfig =
                    dataSourceInstanceConfig.withValue(
                            URL_KEY, ConfigValueFactory.fromAnyRef(newUrl));
        }
        if (businessMode.equals(DATA_INTEGRATION)) {

            String databaseName = dataSourceOption.getDatabases().get(0);

            String tableName = dataSourceOption.getTables().get(0);

            // 将schema转换成sql
            if (pluginType.equals(PluginType.SOURCE)) {

                List<String> tableFields = selectTableFields.getTableFields();

                String sql = tableFieldsToSql(tableFields, databaseName, tableName);

                connectorConfig =
                        connectorConfig.withValue(QUERY_KEY, ConfigValueFactory.fromAnyRef(sql));
            } else if (pluginType.equals(PluginType.SINK)) {

                List<String> tableFields = selectTableFields.getTableFields();

                String sql = generateDb2(tableFields, databaseName, tableName);

                connectorConfig =
                        connectorConfig.withValue(QUERY_KEY, ConfigValueFactory.fromAnyRef(sql));
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
        } else if (businessMode.equals(DATA_REPLICA)) {
            String databaseName = dataSourceOption.getDatabases().get(0);
            if (pluginType.equals(PluginType.SINK)) {
                if (getCatalogName().isPresent()) {
                    Config config = ConfigFactory.empty();
                    config =
                            config.withValue(
                                    FACTORY, ConfigValueFactory.fromAnyRef(getCatalogName().get()));
                    config =
                            config.withValue(
                                    USERNAME,
                                    ConfigValueFactory.fromAnyRef(
                                            dataSourceInstanceConfig.getString("user")));
                    config =
                            config.withValue(
                                    PASSWORD,
                                    ConfigValueFactory.fromAnyRef(
                                            dataSourceInstanceConfig.getString(PASSWORD)));
                    config =
                            config.withValue(
                                    BASE_URL,
                                    ConfigValueFactory.fromAnyRef(
                                            dataSourceInstanceConfig.getString(URL_KEY)));
                    if (isSupportDefaultSchema()
                            && connectorConfig.hasPath(DATABASE_SCHEMA.key())) {
                        config =
                                config.withValue(
                                        CATALOG_SCHEMA,
                                        ConfigValueFactory.fromAnyRef(
                                                connectorConfig.getString(DATABASE_SCHEMA.key())));
                    }

                    connectorConfig = connectorConfig.withValue(CATALOG, config.root());
                }
                return super.mergeDatasourceConfig(
                        dataSourceInstanceConfig,
                        virtualTableDetail,
                        dataSourceOption,
                        selectTableFields,
                        businessMode,
                        pluginType,
                        connectorConfig);
            } else {
                throw new UnsupportedOperationException(
                        "JDBC DATA_REPLICA Unsupported plugin type: " + pluginType);
            }

        } else {
            throw new UnsupportedOperationException("Unsupported businessMode : " + businessMode);
        }
    }

    protected String tableFieldsToSql(List<String> tableFields, String database, String table) {
        return generateSql(tableFields, database, null, table);
    }

    protected String generateDb2(List<String> tableFields, String database, String table) {
        return generateSinkSql(tableFields, database, null, table);
    }

    protected String generateSql(
            List<String> tableFields, String database, String schema, String table) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        for (int i = 0; i < tableFields.size(); i++) {
            sb.append(quoteIdentifier(tableFields.get(i)));
            if (i < tableFields.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(" FROM ").append(quoteIdentifier(table));

        return sb.toString();
    }

    protected String quoteIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }

    protected String generateSinkSql(
            List<String> tableFields, String database, String schema, String table) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(quoteIdentifier(table)).append(" (");

        // Append column names
        for (int i = 0; i < tableFields.size(); i++) {
            sb.append(quoteIdentifier(tableFields.get(i)));
            if (i < tableFields.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append(") VALUES (");

        // Append placeholders
        for (int i = 0; i < tableFields.size(); i++) {
            sb.append("?");
            if (i < tableFields.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append(");");
        return sb.toString();
    }

    @Override
    public String getDataSourceName() {
        return "JDBC-DB2";
    }
}
