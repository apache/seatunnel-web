package com.apache.seatunnel.datasource.plugin.doris.jdbc;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginException;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.whaleops.datasource.plugin.jdbc.common.JdbcDataSourceLRUConnectionPool;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.NonNull;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class DorisJdbcDataSourceChannel extends JdbcDataSourceLRUConnectionPool
        implements DataSourceChannel {

    @Override
    public OptionRule getDataSourceOptions(@NonNull String pluginName) {
        return DorisOptionRule.optionRule();
    }

    @Override
    public OptionRule getVirtualTableOptions(@NonNull String pluginName) {
        return DorisOptionRule.metadataRule();
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
                                .getTables(database, null, filterName, new String[] {"TABLE"})) {
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
        } catch (SQLException e) {
            throw new DataSourcePluginException("get table names failed", e);
        }
    }

    @Override
    public List<String> getDatabases(
            @NonNull String pluginName, @NonNull Map<String, String> requestParams) {
        List<String> dbNames = new ArrayList<>();
        try (Connection connection = getConnection(requestParams);
                PreparedStatement statement = connection.prepareStatement("SHOW DATABASES;");
                ResultSet re = statement.executeQuery()) {
            // filter system databases
            while (re.next()) {
                String dbName = re.getString("database");
                if (StringUtils.isNotBlank(dbName)
                        && !DorisDataSourceConfig.DORIS_SYSTEM_DATABASES.contains(dbName)) {
                    dbNames.add(dbName);
                }
            }
            return dbNames;
        } catch (SQLException e) {
            throw new DataSourcePluginException("Get databases failed", e);
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
        try (Connection connection = getConnection(requestParams)) {
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
        } catch (SQLException e) {
            throw new DataSourcePluginException("get table fields failed", e);
        }
        return tableFields;
    }

    private String getPrimaryKey(DatabaseMetaData metaData, String dbName, String tableName)
            throws SQLException {
        try (ResultSet primaryKeysInfo = metaData.getPrimaryKeys(dbName, "%", tableName)) {
            while (primaryKeysInfo.next()) {
                return primaryKeysInfo.getString("COLUMN_NAME");
            }
        }
        return null;
    }

    @Override
    protected HikariDataSource createDataSource(final Map<String, String> requestParams) {
        checkNotNull(requestParams.get(DorisOptionRule.BASE_URL.key()), "Jdbc url cannot be null");
        String url = requestParams.get(DorisOptionRule.BASE_URL.key());
        String username = requestParams.get(DorisOptionRule.USERNAME.key());
        String password = requestParams.get(DorisOptionRule.PASSWORD.key());

        HikariConfig config = new HikariConfig();
        config.setPoolName(String.format("datasource-plugin-doris[%s, %s]", url, username));
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setConnectionTestQuery("SELECT 1");
        return new HikariDataSource(config);
    }

    @Override
    protected String datasourceCacheKey(Map<String, String> requestParams) {
        String url = requestParams.get(DorisOptionRule.BASE_URL.key());
        if (null == requestParams.get(DorisOptionRule.BASE_URL.key())) {
            throw new DataSourcePluginException("Jdbc url is null");
        }
        String username = requestParams.get(DorisOptionRule.USERNAME.key());
        return String.format("{jdbc-url=%s, username=%s}", url, username);
    }

    @Override
    public Pair<String, String> getTableSyncMaxValue(
            String pluginName,
            Map<String, String> requestParams,
            String databaseName,
            String tableName,
            String updateFieldType) {
        String updateFieldName = requestParams.get("updateFieldName");
        StringBuilder sql = new StringBuilder();
        String replaceTableName = parseDatabaseTableName(tableName, databaseName);
        sql.append("SELECT MAX(`")
                .append(updateFieldName)
                .append("`) AS `")
                .append(updateFieldName)
                .append("` FROM ")
                .append(replaceTableName)
                .append(";");

        if (sql.toString().equals("")) {
            return null;
        }
        try (Connection connection = getConnection(requestParams);
                PreparedStatement statement = connection.prepareStatement(sql.toString());
                ResultSet re = statement.executeQuery()) {
            ResultSetMetaData metaData = re.getMetaData();
            String columnTypeName = metaData.getColumnTypeName(1);
            while (re.next()) {
                String fieldName = re.getString(updateFieldName);
                if (StringUtils.isEmpty(fieldName)
                        || StringUtils.equalsIgnoreCase("null", fieldName)) {
                    throw new RuntimeException(updateFieldName + " max value is null");
                }
                if (StringUtils.equals(updateFieldType, "primary")) {
                    return Pair.of(fieldName, columnTypeName);
                } else if (StringUtils.equals(updateFieldType, "datetime")) {
                    return Pair.of(fieldName, columnTypeName);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("get table max value sync failed", ex);
        }
        return DataSourceChannel.super.getTableSyncMaxValue(
                pluginName, requestParams, databaseName, tableName, updateFieldType);
    }

    private String parseDatabaseTableName(String tableName, String databaseName) {
        return "`" + databaseName + "`.`" + tableName + "`";
    }

    @Override
    public Connection getConnection(String pluginName, Map<String, String> requestParams) {
        try {
            return getConnection(requestParams);
        } catch (Exception e) {
            throw new DataSourcePluginException("Get connection failed", e);
        }
    }
}
