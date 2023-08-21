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

package org.apache.seatunnel.datasource.plugin.oracle.jdbc;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginException;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;
import org.apache.seatunnel.datasource.plugin.api.utils.JdbcUtils;

import org.apache.commons.lang3.StringUtils;

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
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class OracleDataSourceChannel implements DataSourceChannel {

    @Override
    public OptionRule getDataSourceOptions(@NonNull String pluginName) {
        return OracleDataSourceConfig.OPTION_RULE;
    }

    @Override
    public OptionRule getDatasourceMetadataFieldsByDataSourceName(@NonNull String pluginName) {
        return OracleDataSourceConfig.METADATA_RULE;
    }

    @Override
    public List<String> getTables(
            @NonNull String pluginName,
            Map<String, String> requestParams,
            String database,
            Map<String, String> options) {
        StringBuilder sqlWhere = new StringBuilder();
        final String sql =
                "SELECT * FROM ( SELECT OWNER, TABLE_NAME FROM ALL_TABLES\n"
                        + "WHERE TABLE_NAME NOT LIKE 'MDRT_%'\n"
                        + "  AND TABLE_NAME NOT LIKE 'MDRS_%'\n"
                        + "  AND TABLE_NAME NOT LIKE 'MDXT_%'\n"
                        + "  AND (TABLE_NAME NOT LIKE 'SYS_IOT_OVER_%' AND IOT_NAME IS NULL)"
                        + "AND OWNER NOT IN ('APPQOSSYS', 'AUDSYS', 'CTXSYS', 'DVSYS', 'DBSFWUSER', 'DBSNMP',\n"
                        + "                    'GSMADMIN_INTERNAL', 'LBACSYS', 'MDSYS', 'OJVMSYS', 'OLAPSYS',\n"
                        + "                    'ORDDATA', 'ORDSYS', 'OUTLN', 'SYS', 'SYSTEM', 'WMSYS',\n"
                        + "                    'XDB', 'EXFSYS', 'SYSMAN')";
        sqlWhere.append(sql);
        String filterName = options.get("filterName");
        if (StringUtils.isNotEmpty(filterName)) {
            String[] split = filterName.split("\\.");
            if (split.length == 2) {
                sqlWhere.append(" AND (TABLE_NAME LIKE '")
                        .append(
                                split[1].contains("%")
                                        ? split[1].toUpperCase(Locale.ROOT)
                                        : "%" + split[1].toUpperCase(Locale.ROOT) + "%")
                        .append("'")
                        .append(" AND OWNER LIKE '")
                        .append(
                                split[0].contains("%")
                                        ? split[0].toUpperCase(Locale.ROOT)
                                        : "%" + split[0].toUpperCase(Locale.ROOT) + "%")
                        .append("')");
            } else {
                String filterNameRep =
                        filterName.contains("%")
                                ? filterName.toUpperCase(Locale.ROOT)
                                : "%" + filterName.toUpperCase(Locale.ROOT) + "%";
                sqlWhere.append(" AND (TABLE_NAME LIKE '%")
                        .append(filterNameRep)
                        .append("%'")
                        .append(" OR OWNER LIKE '%")
                        .append(filterNameRep)
                        .append("%')");
            }
        }
        sqlWhere.append(" ORDER BY OWNER, TABLE_NAME ) ");
        String size = options.get("size");
        if (StringUtils.isNotEmpty(size)) {
            sqlWhere.append("WHERE ROWNUM <= ").append(size);
        }
        log.info("execute sql :{}", sqlWhere.toString());
        List<String> tableNames = new ArrayList<>();
        long start = System.currentTimeMillis();
        try (Connection connection = getConnection(requestParams); ) {
            long end = System.currentTimeMillis();
            log.info("connection, cost {}ms for oracle", end - start);
            start = System.currentTimeMillis();
            try (Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(sqlWhere.toString())) {
                end = System.currentTimeMillis();
                log.info("statement execute sql, cost {}ms for oracle", end - start);
                start = System.currentTimeMillis();
                while (resultSet.next()) {
                    String schemaName = resultSet.getString("OWNER");
                    String tableName = resultSet.getString("TABLE_NAME");
                    tableNames.add(schemaName + "." + tableName);
                }
                end = System.currentTimeMillis();
                log.info("while result set, cost {}ms for oracle", end - start);
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
                        connection.prepareStatement("SELECT NAME FROM v$database");
                ResultSet re = statement.executeQuery()) {
            while (re.next()) {
                String dbName = re.getString("NAME");
                if (StringUtils.isNotBlank(dbName)) {
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
        List<TableField> tableFields = new ArrayList<>();
        try (Connection connection = getConnection(requestParams, database)) {
            DatabaseMetaData metaData = connection.getMetaData();
            String primaryKey = getPrimaryKey(metaData, database, table);
            try (ResultSet resultSet = metaData.getColumns(database, null, table, null)) {
                while (resultSet.next()) {
                    TableField tableField = new TableField();
                    String columnName = resultSet.getString("COLUMN_NAME");
                    tableField.setPrimaryKey(false);
                    if (StringUtils.isNotBlank(primaryKey) && primaryKey.equals(columnName)) {
                        tableField.setPrimaryKey(true);
                    }
                    tableField.setName(columnName);
                    tableField.setType(resultSet.getString("TYPE_NAME"));
                    tableField.setComment(resultSet.getString("REMARKS"));
                    Object nullable = resultSet.getObject("IS_NULLABLE");
                    tableField.setNullable(Boolean.TRUE.toString().equals(nullable.toString()));
                    tableFields.add(tableField);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new DataSourcePluginException("get table fields failed", e);
        }
        return tableFields;
    }

    @Override
    public Map<String, List<TableField>> getTableFields(
            @NonNull String pluginName,
            @NonNull Map<String, String> requestParams,
            @NonNull String database,
            @NonNull List<String> tables) {
        return null;
    }

    private String getPrimaryKey(DatabaseMetaData metaData, String dbName, String tableName)
            throws SQLException {
        ResultSet primaryKeysInfo = metaData.getPrimaryKeys(dbName, "%", tableName);
        while (primaryKeysInfo.next()) {
            return primaryKeysInfo.getString("COLUMN_NAME");
        }
        return null;
    }

    private Connection getConnection(Map<String, String> requestParams)
            throws SQLException, ClassNotFoundException {
        return getConnection(requestParams, null);
    }

    private Connection getConnection(Map<String, String> requestParams, String databaseName)
            throws SQLException, ClassNotFoundException {
        checkNotNull(requestParams.get(OracleOptionRule.DRIVER.key()));
        checkNotNull(requestParams.get(OracleOptionRule.URL.key()), "Jdbc url cannot be null");
        String url =
                JdbcUtils.replaceDatabase(
                        requestParams.get(OracleOptionRule.URL.key()), databaseName);
        if (requestParams.containsKey(OracleOptionRule.USER.key())) {
            String username = requestParams.get(OracleOptionRule.USER.key());
            String password = requestParams.get(OracleOptionRule.PASSWORD.key());
            return DriverManager.getConnection(url, username, password);
        }
        return DriverManager.getConnection(url);
    }
}
