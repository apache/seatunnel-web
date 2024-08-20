package org.apache.seatunnel.datasource.plugin.cdc.postgresql;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginException;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;
import org.apache.seatunnel.datasource.plugin.api.utils.JdbcUtils;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class PostgresCDCDataSourceChannel implements DataSourceChannel {

    @Override
    public boolean canAbleGetSchema() {
        return true;
    }

    @Override
    public OptionRule getDataSourceOptions(String pluginName) {
        return PostgresCDCOptionRule.optionRule();
    }

    @Override
    public OptionRule getDatasourceMetadataFieldsByDataSourceName(String pluginName) {
        return PostgresCDCOptionRule.metadataRule();
    }

    @Override
    public List<String> getTables(
            String pluginName,
            Map<String, String> requestParams,
            String database,
            Map<String, String> options) {
        return this.getTableNames(requestParams, database, options);
    }

    @Override
    public List<String> getDatabases(String pluginName, Map<String, String> requestParams) {
        try {
            return this.getDataBaseNames(requestParams);
        } catch (SQLException e) {
            throw new DataSourcePluginException("get databases failed", e);
        }
    }

    @Override
    public boolean checkDataSourceConnectivity(
            String pluginName, Map<String, String> requestParams) {
        return this.checkJdbcConnectivity(requestParams);
    }

    @Override
    public List<TableField> getTableFields(
            String pluginName, Map<String, String> requestParams, String database, String table) {
        return getTableFields(requestParams, database, table);
    }

    @Override
    public Map<String, List<TableField>> getTableFields(
            String pluginName,
            Map<String, String> requestParams,
            String database,
            List<String> tables) {
        Map<String, List<TableField>> tableFields = new HashMap<>(tables.size());
        for (String table : tables) {
            tableFields.put(table, getTableFields(requestParams, database, table));
        }
        return tableFields;
    }

    protected boolean checkJdbcConnectivity(Map<String, String> requestParams) {
        try (Connection connection = init(requestParams);
                Statement statement = connection.createStatement()) {

            try (ResultSet resultSet = statement.executeQuery("SELECT 1")) {
                return resultSet.next();
            }

        } catch (Exception e) {
            throw new DataSourcePluginException(
                    "check jdbc connectivity failed, " + e.getMessage(), e);
        }
    }

    protected Connection init(Map<String, String> requestParams) throws SQLException {
        if (null == requestParams.get(PostgresCDCOptionRule.BASE_URL.key())) {
            throw new DataSourcePluginException("Jdbc url is null");
        }
        String url = requestParams.get(PostgresCDCOptionRule.BASE_URL.key());

        Properties info = new java.util.Properties();
        if (null != requestParams.get(PostgresCDCOptionRule.PASSWORD.key())
                && null != requestParams.get(PostgresCDCOptionRule.USERNAME.key())) {
            info.put("user", requestParams.get(PostgresCDCOptionRule.USERNAME.key()));
            info.put("password", requestParams.get(PostgresCDCOptionRule.PASSWORD.key()));
        }
        return DriverManager.getConnection(url, info);
    }

    protected List<String> getDataBaseNames(Map<String, String> requestParams) throws SQLException {
        List<String> dbNames = new ArrayList<>();
        try (Connection connection = init(requestParams);
                PreparedStatement statement =
                        connection.prepareStatement(
                                "SELECT datname FROM pg_database WHERE datistemplate = false;");
                ResultSet re = statement.executeQuery()) {
            while (re.next()) {
                String dbName = re.getString("datname");
                if (StringUtils.isNotBlank(dbName)) {
                    dbNames.add(dbName);
                }
            }
            return dbNames;
        }
    }

    protected List<String> getTableNames(
            Map<String, String> requestParams, String dbName, Map<String, String> options) {
        List<String> tableNames = new ArrayList<>();
        StringBuilder queryWhere = new StringBuilder();
        String query =
                "SELECT table_schema, table_name FROM information_schema.tables\n"
                        + "WHERE table_schema NOT IN ('information_schema', 'pg_catalog', 'root', 'pg_toast', 'pg_temp_1', 'pg_toast_temp_1', 'postgres', 'template0', 'template1')\n";
        queryWhere.append(query);
        String filterName = options.get("filterName");
        if (StringUtils.isNotEmpty(filterName)) {
            String[] split = filterName.split("\\.");
            if (split.length == 2) {
                queryWhere
                        .append("AND (table_schema LIKE '")
                        .append(split[0].contains("%") ? split[0] : "%" + split[0] + "%")
                        .append("'")
                        .append(" AND table_name LIKE '")
                        .append(split[1].contains("%") ? split[1] : "%" + split[1] + "%")
                        .append("')");
            } else {
                String filterNameRep =
                        filterName.contains("%") ? filterName : "%" + filterName + "%";
                queryWhere
                        .append(" AND (table_schema LIKE '")
                        .append(filterNameRep)
                        .append("'")
                        .append(" OR table_name LIKE '")
                        .append(filterNameRep)
                        .append("')");
            }
        }
        String size = options.get("size");
        if (StringUtils.isNotEmpty(size)) {
            queryWhere.append(" LIMIT ").append(size);
        }
        log.info(queryWhere.toString());
        requestParams.put(
                PostgresCDCOptionRule.BASE_URL.key(),
                JdbcUtils.replaceDatabase(
                        requestParams.get(PostgresCDCOptionRule.BASE_URL.key()), dbName));
        try (Connection connection = init(requestParams)) {
            try (Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(queryWhere.toString())) {
                while (resultSet.next()) {
                    String schemaName = resultSet.getString("table_schema");
                    String tableName = resultSet.getString("table_name");
                    if (StringUtils.isNotBlank(schemaName)) {
                        tableNames.add(schemaName + "." + tableName);
                    }
                }
            }
            return tableNames;
        } catch (SQLException e) {
            throw new DataSourcePluginException("get table names failed", e);
        }
    }

    private Connection getConnection(Map<String, String> requestParams)
            throws SQLException, ClassNotFoundException {
        return getConnection(requestParams, null);
    }

    private Connection getConnection(Map<String, String> requestParams, String databaseName)
            throws SQLException, ClassNotFoundException {
        checkNotNull(requestParams.get(PostgresCDCOptionRule.DRIVER.key()));
        checkNotNull(requestParams.get(PostgresCDCOptionRule.URL.key()), "Jdbc url cannot be null");
        String url =
                JdbcUtils.replaceDatabase(
                        requestParams.get(PostgresCDCOptionRule.URL.key()), databaseName);
        if (requestParams.containsKey(PostgresCDCOptionRule.USER.key())) {
            String username = requestParams.get(PostgresCDCOptionRule.USER.key());
            String password = requestParams.get(PostgresCDCOptionRule.PASSWORD.key());
            return DriverManager.getConnection(url, username, password);
        }
        return DriverManager.getConnection(url);
    }

    protected List<TableField> getTableFields(
            Map<String, String> requestParams, String dbName, String tableName) {
        List<TableField> tableFields = new ArrayList<>();
        try (Connection connection = init(requestParams)) {
            DatabaseMetaData metaData = connection.getMetaData();
            String primaryKey = getPrimaryKey(metaData, dbName, tableName);
            String[] split = tableName.split("\\.");
            if (split.length != 2) {
                throw new DataSourcePluginException(
                        "Postgresql tableName should composed by schemaName.tableName");
            }
            try (ResultSet resultSet = metaData.getColumns(dbName, split[0], split[1], null)) {
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
        ResultSet primaryKeysInfo = metaData.getPrimaryKeys(dbName, "%", tableName);
        while (primaryKeysInfo.next()) {
            return primaryKeysInfo.getString("COLUMN_NAME");
        }
        return null;
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
