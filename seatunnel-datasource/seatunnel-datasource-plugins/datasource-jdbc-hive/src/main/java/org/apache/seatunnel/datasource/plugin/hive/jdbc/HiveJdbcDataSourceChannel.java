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

package org.apache.seatunnel.datasource.plugin.hive.jdbc;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginException;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class HiveJdbcDataSourceChannel implements DataSourceChannel {

    @Override
    public OptionRule getDataSourceOptions(@NonNull String pluginName) {
        return HiveJdbcOptionRule.optionRule();
    }

    @Override
    public OptionRule getDatasourceMetadataFieldsByDataSourceName(@NonNull String pluginName) {
        return HiveJdbcOptionRule.metadataRule();
    }

    @Override
    public List<String> getTables(
            @NonNull String pluginName,
            Map<String, String> requestParams,
            String database,
            Map<String, String> option) {
        return getTableNames(requestParams, database);
    }

    @Override
    public List<String> getDatabases(
            @NonNull String pluginName, @NonNull Map<String, String> requestParams) {
        try {
            return getDataBaseNames(requestParams);
        } catch (SQLException | IOException e) {
            log.error("Query Hive databases error, request params is {}", requestParams, e);
            throw new DataSourcePluginException("Query Hive databases error,", e);
        }
    }

    @Override
    public boolean checkDataSourceConnectivity(
            @NonNull String pluginName, @NonNull Map<String, String> requestParams) {
        return checkJdbcConnectivity(requestParams);
    }

    @Override
    public List<TableField> getTableFields(
            @NonNull String pluginName,
            @NonNull Map<String, String> requestParams,
            @NonNull String database,
            @NonNull String table) {
        return getTableFields(requestParams, database, table);
    }

    @Override
    public Map<String, List<TableField>> getTableFields(
            @NonNull String pluginName,
            @NonNull Map<String, String> requestParams,
            @NonNull String database,
            @NonNull List<String> tables) {
        Map<String, List<TableField>> tableFields = new HashMap<>(tables.size());
        for (String table : tables) {
            tableFields.put(table, getTableFields(requestParams, database, table));
        }
        return tableFields;
    }

    protected boolean checkJdbcConnectivity(Map<String, String> requestParams) {
        try (Connection ignored = getHiveConnection(requestParams)) {
            return true;
        } catch (Exception e) {
            throw new DataSourcePluginException(
                    "check jdbc connectivity failed, " + e.getMessage(), e);
        }
    }

    protected Connection getHiveConnection(Map<String, String> requestParams)
            throws IOException, SQLException {
        if (MapUtils.isEmpty(requestParams)) {
            throw new DataSourcePluginException(
                    "Hive jdbc request params is null, please check your config");
        }
        String driverClass =
                requestParams.getOrDefault(
                        HiveJdbcOptionRule.DRIVER.key(), "org.apache.hive.jdbc.HiveDriver");
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new DataSourcePluginException(
                    "Hive jdbc driver " + driverClass + " not found", e);
        }
        Properties connProps = new Properties();
        boolean isKerberosEnabled =
                Boolean.parseBoolean(requestParams.get(HiveJdbcOptionRule.USE_KERBEROS.key()));
        if (isKerberosEnabled) {
            String krb5ConfPath = requestParams.get(HiveJdbcOptionRule.KRB5_PATH.key());
            if (StringUtils.isNotEmpty(krb5ConfPath)) {
                System.setProperty("java.security.krb5.conf", krb5ConfPath);
            }
            Configuration conf = new Configuration();
            conf.set("hadoop.security.authentication", "Kerberos");
            UserGroupInformation.setConfiguration(conf);
            String principal = requestParams.get(HiveJdbcOptionRule.KERBEROS_PRINCIPAL.key());
            connProps.setProperty("principal", principal);
            String keytabPath = requestParams.get(HiveJdbcOptionRule.KERBEROS_KEYTAB_PATH.key());
            UserGroupInformation.loginUserFromKeytab(principal, keytabPath);
        }

        String user = requestParams.get(HiveJdbcOptionRule.USER.key());
        String password = requestParams.get(HiveJdbcOptionRule.PASSWORD.key());
        if (StringUtils.isNotEmpty(user)) {
            connProps.setProperty("user", user);
        }
        if (StringUtils.isNotEmpty(password)) {
            connProps.setProperty("password", password);
        }

        String jdbcUrl = requestParams.get(HiveJdbcOptionRule.URL.key());
        return DriverManager.getConnection(jdbcUrl, connProps);
    }

    protected List<String> getDataBaseNames(Map<String, String> requestParams)
            throws SQLException, IOException {
        List<String> dbNames = new ArrayList<>();
        try (Connection connection = getHiveConnection(requestParams);
                Statement statement = connection.createStatement()) {
            ResultSet re = statement.executeQuery("SHOW DATABASES");
            // filter system databases
            while (re.next()) {
                String dbName = re.getString("database_name");
                if (StringUtils.isNotBlank(dbName) && isNotSystemDatabase(dbName)) {
                    dbNames.add(dbName);
                }
            }
            return dbNames;
        }
    }

    protected List<String> getTableNames(Map<String, String> requestParams, String dbName) {
        List<String> tableNames = new ArrayList<>();
        try (Connection connection = getHiveConnection(requestParams)) {
            ResultSet resultSet =
                    connection
                            .getMetaData()
                            .getTables(dbName, dbName, null, new String[] {"TABLE"});
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if (StringUtils.isNotBlank(tableName)) {
                    tableNames.add(tableName);
                }
            }
            return tableNames;
        } catch (SQLException | IOException e) {
            throw new DataSourcePluginException("get table names failed", e);
        }
    }

    protected List<TableField> getTableFields(
            Map<String, String> requestParams, String dbName, String tableName) {
        List<TableField> tableFields = new ArrayList<>();
        try (Connection connection = getHiveConnection(requestParams)) {
            DatabaseMetaData metaData = connection.getMetaData();
            String primaryKey = getPrimaryKey(metaData, dbName, tableName);
            ResultSet resultSet = metaData.getColumns(dbName, null, tableName, null);
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
                boolean isNullable = convertToBoolean(nullable);
                tableField.setNullable(isNullable);
                tableFields.add(tableField);
            }
        } catch (SQLException | IOException e) {
            throw new DataSourcePluginException("get table fields failed", e);
        }
        return tableFields;
    }

    private String getPrimaryKey(DatabaseMetaData metaData, String dbName, String tableName)
            throws SQLException {
        ResultSet primaryKeysInfo = metaData.getPrimaryKeys(dbName, "%", tableName);
        if (primaryKeysInfo.next()) {
            return primaryKeysInfo.getString("COLUMN_NAME");
        }
        return null;
    }

    private boolean isNotSystemDatabase(String dbName) {
        return !HiveJdbcConstants.HIVE_SYSTEM_DATABASES.contains(dbName.toLowerCase());
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
}
