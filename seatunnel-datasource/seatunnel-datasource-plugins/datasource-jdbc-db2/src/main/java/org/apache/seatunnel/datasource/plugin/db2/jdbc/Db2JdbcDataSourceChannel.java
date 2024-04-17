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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

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
            filterName = null;
        }
        try (Connection connection = getConnection(requestParams);
                ResultSet resultSet =
                        connection
                                .getMetaData()
                                .getTables(null, null, "%", new String[] {"TABLE"})) {
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
        // Hardcoded list of example database names
        List<String> dbNames = Arrays.asList("default");
        return dbNames;
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
            try (ResultSet resultSet = metaData.getColumns(null, null, table, null)) {

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
        } catch (SQLException e) {
            // Log the exception and rethrow as DataSourcePluginException
            System.out.println("Error while retrieving table fields: " + e);
            throw new DataSourcePluginException("Failed to get table fields", e);
        } catch (ClassNotFoundException e) {
            // Log the exception and rethrow as DataSourcePluginException
            System.out.println("JDBC driver class not found" + e);
            throw new DataSourcePluginException("JDBC driver class not found", e);
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
        while (primaryKeysInfo.next()) {
            return primaryKeysInfo.getString("COLUMN_NAME");
        }
        return null;
    }

    private Connection getConnection(Map<String, String> requestParams)
            throws SQLException, ClassNotFoundException {
        // Ensure the DB2 JDBC driver is loaded
        Class.forName("com.ibm.db2.jcc.DB2Driver");
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
