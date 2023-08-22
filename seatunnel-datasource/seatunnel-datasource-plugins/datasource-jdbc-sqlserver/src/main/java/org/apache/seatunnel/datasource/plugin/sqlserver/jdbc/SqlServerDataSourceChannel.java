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

package org.apache.seatunnel.datasource.plugin.sqlserver.jdbc;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginException;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;
import org.apache.seatunnel.datasource.plugin.api.utils.JdbcUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class SqlServerDataSourceChannel implements DataSourceChannel {
    @Override
    public OptionRule getDataSourceOptions(@NonNull String pluginName) {
        return SqlServerDataSourceConfig.OPTION_RULE;
    }

    @Override
    public OptionRule getDatasourceMetadataFieldsByDataSourceName(@NonNull String pluginName) {
        return SqlServerDataSourceConfig.METADATA_RULE;
    }

    @Override
    public List<String> getTables(
            @NonNull String pluginName,
            Map<String, String> requestParams,
            String database,
            Map<String, String> options) {
        List<String> tableNames = new ArrayList<>();
        StringBuilder queryWhere = new StringBuilder();
        String query =
                String.format(
                        "SELECT SCHEMA_NAME(schema_id) AS schema_name, name AS table_name\n"
                                + "FROM %s.sys.tables \n"
                                + "WHERE type = 'U' AND is_ms_shipped = 0 \n",
                        database);
        queryWhere.append(query);
        String filterName = options.get("filterName");
        if (StringUtils.isNotEmpty(filterName)) {
            String[] split = filterName.split("\\.");
            if (split.length == 2) {
                String formatStr =
                        " AND (name LIKE '"
                                + (split[1].contains("%") ? split[1] : "%" + split[1] + "%")
                                + "' AND SCHEMA_NAME(schema_id) LIKE '"
                                + (split[0].contains("%") ? split[0] : "%" + split[0] + "%")
                                + "')";
                queryWhere.append(formatStr);
            } else {
                String filterNameRep =
                        filterName.contains("%") ? filterName : "%" + filterName + "%";
                String formatStr =
                        " AND (name LIKE '"
                                + filterNameRep
                                + "' OR SCHEMA_NAME(schema_id) LIKE '"
                                + filterNameRep
                                + "')";
                queryWhere.append(formatStr);
            }
        }
        queryWhere.append("ORDER BY schema_name, table_name");
        String size = options.get("size");
        if (StringUtils.isNotEmpty(size)) {
            queryWhere.append(" OFFSET 0 ROWS FETCH NEXT ").append(size).append(" ROWS ONLY");
        }
        log.info("execute sql :{}", queryWhere.toString());
        long start = System.currentTimeMillis();
        try (Connection connection = getConnection(requestParams)) {
            long end = System.currentTimeMillis();
            log.info("connection, cost {}ms for sqlserver", end - start);
            start = System.currentTimeMillis();
            try (Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(queryWhere.toString())) {
                end = System.currentTimeMillis();
                log.info("execute sql, cost {}ms for sqlserver", end - start);
                start = System.currentTimeMillis();
                while (resultSet.next()) {
                    String schemaName = resultSet.getString("SCHEMA_NAME");
                    String tableName = resultSet.getString("table_name");
                    if (StringUtils.isNotBlank(schemaName)
                            && !SqlServerDataSourceConfig.SQLSERVER_SYSTEM_DATABASES.contains(
                                    schemaName)) {
                        tableNames.add(schemaName + "." + tableName);
                    }
                }
                end = System.currentTimeMillis();
                log.info("while result set, cost {}ms for sqlserver", end - start);
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
                PreparedStatement statement =
                        connection.prepareStatement(
                                "SELECT name FROM sys.databases WHERE name NOT IN ('master', 'tempdb', 'model', 'msdb');");
                ResultSet re = statement.executeQuery()) {
            // filter system databases
            while (re.next()) {
                String dbName = re.getString("name");
                if (StringUtils.isNotBlank(dbName)
                        && !SqlServerDataSourceConfig.SQLSERVER_SYSTEM_DATABASES.contains(dbName)) {
                    dbNames.add(dbName);
                }
            }
            return dbNames;
        } catch (Exception ex) {
            throw new RuntimeException("get databases failed", ex);
        }
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
        Pair<String, String> pair = parseSchemaAndTable(table);
        return getTableFields(requestParams, database, pair.getLeft(), pair.getRight());
    }

    private List<TableField> getTableFields(
            Map<String, String> requestParams, String dbName, String schemaName, String tableName) {
        List<TableField> tableFields = new ArrayList<>();
        try (Connection connection = getConnection(requestParams); ) {
            DatabaseMetaData metaData = connection.getMetaData();
            String primaryKey = getPrimaryKey(metaData, dbName, schemaName, tableName);
            try (ResultSet resultSet = metaData.getColumns(dbName, schemaName, tableName, null)) {
                while (resultSet.next()) {
                    TableField tableField = new TableField();
                    String columnName = resultSet.getString("COLUMN_NAME");
                    tableField.setPrimaryKey(false);
                    if (StringUtils.isNotBlank(primaryKey) && primaryKey.equals(columnName)) {
                        tableField.setPrimaryKey(true);
                    }
                    tableField.setName(columnName);
                    String typeString = resultSet.getString("TYPE_NAME");
                    String[] parts = typeString.split(" ");
                    String baseType = parts.length > 0 ? parts[0] : "";
                    tableField.setType(baseType);
                    tableField.setComment(resultSet.getString("REMARKS"));
                    Object nullable = resultSet.getObject("IS_NULLABLE");
                    boolean isNullable = convertToBoolean(nullable);
                    tableField.setNullable(isNullable);
                    tableFields.add(tableField);
                }
            }
        } catch (Exception e) {
            throw new DataSourcePluginException("get table fields failed", e);
        }
        return tableFields;
    }

    private boolean convertToBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return value.equals("TRUE");
        }
        return false;
    }

    @Override
    public Map<String, List<TableField>> getTableFields(
            @NonNull String pluginName,
            @NonNull Map<String, String> requestParams,
            @NonNull String database,
            @NonNull List<String> tables) {
        return null;
    }

    private String getPrimaryKey(
            DatabaseMetaData metaData, String dbName, String schemaName, String tableName)
            throws SQLException {
        try (ResultSet primaryKeysInfo = metaData.getPrimaryKeys(dbName, schemaName, tableName)) {
            while (primaryKeysInfo.next()) {
                return primaryKeysInfo.getString("COLUMN_NAME");
            }
        }
        return null;
    }

    private Connection getConnection(Map<String, String> requestParams)
            throws SQLException, ClassNotFoundException {
        return getConnection(requestParams, null);
    }

    private Connection getConnection(Map<String, String> requestParams, String databaseName)
            throws SQLException, ClassNotFoundException {
        checkNotNull(requestParams.get(SqlServerOptionRule.DRIVER.key()));
        checkNotNull(requestParams.get(SqlServerOptionRule.URL.key()), "Jdbc url cannot be null");
        String url =
                JdbcUtils.replaceDatabase(
                        requestParams.get(SqlServerOptionRule.URL.key()), databaseName);
        if (requestParams.containsKey(SqlServerOptionRule.USER.key())) {
            String username = requestParams.get(SqlServerOptionRule.USER.key());
            String password = requestParams.get(SqlServerOptionRule.PASSWORD.key());
            return DriverManager.getConnection(url, username, password);
        }
        return DriverManager.getConnection(url);
    }

    private Pair<String, String> parseSchemaAndTable(String tableName) {
        String[] schemaAndTable = tableName.split("\\.");
        if (schemaAndTable.length != 2) {
            throw new DataSourcePluginException("table name is invalid");
        }
        return Pair.of(schemaAndTable[0], schemaAndTable[1]);
    }
}
