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

package org.apache.seatunnel.datasource.plugin.db2.jdbc;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginException;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;

import org.apache.commons.lang3.StringUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class Db2JdbcDataSourceChannel implements DataSourceChannel {
    @Override
    public OptionRule getDataSourceOptions(@NonNull String pluginName) {
        return Db2DataSourceConfig.OPTION_RULE;
    }

    @Override
    public OptionRule getDatasourceMetadataFieldsByDataSourceName(@NonNull String pluginName) {
        return Db2DataSourceConfig.METADATA_RULE;
    }

    @Override
    public List<String> getTables(
            @NonNull String pluginName,
            Map<String, String> requestParams,
            String database,
            Map<String, String> options) {
        List<String> tableNames = new ArrayList<>();
        String filterName = options.get("filterName");
        String size = options.get("size");
        boolean isSize = StringUtils.isNotEmpty(size);
        if (StringUtils.isNotEmpty(filterName) && !filterName.contains("%")) {
            filterName = "%" + filterName + "%";
        } else if (StringUtils.equals(filterName, "")) {
            filterName = "%";
        }
        try (Connection connection = getConnection(requestParams);
                ResultSet resultSet =
                        connection
                                .getMetaData()
                                .getTables(null, database, filterName, new String[] {"TABLE"})) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if (StringUtils.isNotBlank(tableName)) {
                    tableNames.add(tableName);
                    if (isSize && tableNames.size() >= Integer.parseInt(size)) {
                        break;
                    }
                }
            }
            return tableNames;
        } catch (ClassNotFoundException | SQLException e) {
            throw new DataSourcePluginException("get table names failed", e);
        }
    }

    @Override
    public List<String> getDatabases(
            @NonNull String pluginName, @NonNull Map<String, String> requestParams) {
        List<String> dbNames = new ArrayList<>();
        try (Connection connection = getConnection(requestParams);
                Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT SCHEMANAME FROM SYSCAT.SCHEMATA");
            while (resultSet.next()) {
                String dbName = resultSet.getString("SCHEMANAME");
                if (StringUtils.isBlank(dbName)) {
                    continue;
                }
                dbName = dbName.trim();
                if (isNotSystemDatabase(dbName)) {
                    dbNames.add(dbName);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new DataSourcePluginException("Failed to get database names", e);
        }
        return dbNames;
    }

    private boolean isNotSystemDatabase(String dbName) {
        return !Db2DataSourceConfig.DB2_SYSTEM_DATABASES.contains(dbName.toUpperCase());
    }

    @Override
    public boolean checkDataSourceConnectivity(
            @NonNull String pluginName, @NonNull Map<String, String> requestParams) {
        try (Connection ignored = getConnection(requestParams)) {
            return true;
        } catch (Exception e) {
            throw new DataSourcePluginException("check jdbc connectivity failed", e);
        }
    }

    @Override
    public List<TableField> getTableFields(
            @NonNull String pluginName,
            @NonNull Map<String, String> requestParams,
            @NonNull String database,
            @NonNull String table) {
        List<TableField> tableFields = new ArrayList<>();
        try (Connection connection = getConnection(requestParams)) {
            DatabaseMetaData metaData = connection.getMetaData();

            // Retrieve primary key information
            String primaryKey = getPrimaryKey(metaData, database, table);

            // Retrieve column information
            try (ResultSet resultSet = metaData.getColumns(null, database, table, null)) {

                while (resultSet.next()) {
                    TableField tableField = new TableField();
                    String columnName = resultSet.getString("COLUMN_NAME");

                    // Set primary key flag
                    tableField.setPrimaryKey(primaryKey != null && primaryKey.equals(columnName));

                    // Set other field attributes
                    tableField.setName(columnName);
                    tableField.setType(resultSet.getString("TYPE_NAME"));
                    tableField.setComment(resultSet.getString("REMARKS"));

                    // Set nullable flag
                    String isNullable = resultSet.getString("IS_NULLABLE");
                    tableField.setNullable("YES".equalsIgnoreCase(isNullable));

                    tableFields.add(tableField);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            log.error("Error while retrieving table fields", e);
            throw new DataSourcePluginException("Failed to get table fields", e);
        }
        return tableFields;
    }

    @Override
    public Map<String, List<TableField>> getTableFields(
            @NonNull String pluginName,
            @NonNull Map<String, String> requestParams,
            @NonNull String database,
            @NonNull List<String> tables) {
        return tables.parallelStream()
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                table ->
                                        getTableFields(
                                                pluginName, requestParams, database, table)));
    }

    private String getPrimaryKey(DatabaseMetaData metaData, String dbName, String tableName)
            throws SQLException {
        ResultSet primaryKeysInfo = metaData.getPrimaryKeys(null, dbName, tableName);
        if (primaryKeysInfo.next()) {
            return primaryKeysInfo.getString("COLUMN_NAME");
        }
        return null;
    }

    private Connection getConnection(Map<String, String> requestParams)
            throws SQLException, ClassNotFoundException {
        String driverClass =
                requestParams.getOrDefault(
                        Db2OptionRule.DRIVER.key(),
                        Db2OptionRule.DriverType.DB2.getDriverClassName());
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new DataSourcePluginException("DB2 jdbc driver " + driverClass + " not found", e);
        }
        checkNotNull(requestParams.get(Db2OptionRule.URL.key()), "Jdbc url cannot be null");
        String url = requestParams.get(Db2OptionRule.URL.key());
        if (requestParams.containsKey(Db2OptionRule.USER.key())) {
            String username = requestParams.get(Db2OptionRule.USER.key());
            String password = requestParams.get(Db2OptionRule.PASSWORD.key());
            return DriverManager.getConnection(url, username, password);
        }
        return DriverManager.getConnection(url);
    }
}
