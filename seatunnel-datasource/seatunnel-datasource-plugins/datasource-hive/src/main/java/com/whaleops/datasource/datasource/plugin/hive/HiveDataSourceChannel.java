package com.whaleops.datasource.datasource.plugin.hive;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginException;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;

import org.apache.commons.lang.StringUtils;

import com.whaleops.datasource.datasource.plugin.hive.client.HiveClient;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HiveDataSourceChannel implements DataSourceChannel {

    @Override
    public OptionRule getDataSourceOptions(@NonNull String pluginName) {
        return HiveOptionRule.optionRule();
    }

    @Override
    public OptionRule getDatasourceMetadataFieldsByDataSourceName(@NonNull String pluginName) {
        return HiveOptionRule.metadataRule();
    }

    public List<String> getTables(
            @NonNull String pluginName,
            Map<String, String> requestParams,
            String database,
            Map<String, String> options) {

        try (HiveClient hiveClient = HiveClient.createInstance(requestParams)) {
            String size = options.get("size");
            Integer sizeInt = StringUtils.isNotEmpty(size) ? Integer.parseInt(size) : 0;
            return hiveClient.getAllTables(database, options.get("filterName"), sizeInt);
        }
    }

    @Override
    public List<String> getDatabases(
            @NonNull String pluginName, @NonNull Map<String, String> requestParams) {
        try (HiveClient hiveClient = HiveClient.createInstance(requestParams); ) {
            return hiveClient.getAllDatabases();
        }
    }

    @Override
    public boolean checkDataSourceConnectivity(
            @NonNull String pluginName, @NonNull Map<String, String> requestParams) {
        return checkHiveConnectivity(requestParams);
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

    protected boolean checkHiveConnectivity(Map<String, String> requestParams) {
        try (HiveClient ignored = HiveClient.createInstance(requestParams)) {
            System.out.println(ignored.getAllDatabases());
            return true;
        } catch (Exception e) {
            throw new DataSourcePluginException(
                    "check hive connectivity failed, " + e.getMessage(), e);
        }
    }

    protected List<TableField> getTableFields(
            Map<String, String> requestParams, String dbName, String tableName) {
        try (HiveClient hiveClient = HiveClient.createInstance(requestParams)) {
            return hiveClient.getFields(dbName, tableName);
        }
    }

    private static boolean checkHostConnectable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isNotSystemDatabase(String pluginName, String dbName) {
        // FIXME,filters system databases
        return true;
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
