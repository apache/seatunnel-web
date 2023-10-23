package com.apache.seatunnel.datasource.plugin.mongodbcdc;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginException;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoIterable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.apache.seatunnel.datasource.plugin.mongodbcdc.MongodbCDCOptionRule.buildConnectionString;
import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
public class MongodbCDCDataSourceChannel implements DataSourceChannel {

    private static final String DATABASE = "default";

    @Override
    public OptionRule getDataSourceOptions(@NonNull String pluginName) {
        return MongodbCDCOptionRule.optionRule();
    }

    @Override
    public OptionRule getVirtualTableOptions(@NonNull String pluginName) {
        return MongodbCDCOptionRule.metadataRule();
    }

    public List<String> getTables(
            @NonNull String pluginName,
            Map<String, String> requestParams,
            String database,
            Map<String, String> options) {
        checkArgument(StringUtils.equalsIgnoreCase(database, DATABASE), "database must be default");

        return Collections.emptyList();
    }

    @Override
    public List<String> getDatabases(
            @NonNull String pluginName, @NonNull Map<String, String> requestParams) {
        return ImmutableList.of(DATABASE);
    }

    @Override
    public List<TableField> getTableFields(
            @NonNull String pluginName,
            @NonNull Map<String, String> requestParams,
            @NonNull String database,
            @NonNull String table) {
        checkArgument(StringUtils.equalsIgnoreCase(database, DATABASE), "database must be default");
        return Collections.emptyList();
    }

    @Override
    public boolean checkDataSourceConnectivity(
            @NonNull String pluginName, @NonNull Map<String, String> requestParams) {

        try (MongoClient mongoClient = createMongoClient(requestParams)) {
            // Verify if the connection to mongodb was successful
            MongoIterable<String> databaseNames = mongoClient.listDatabaseNames();
            if (databaseNames.iterator().hasNext()) {
                log.info("mongoDB connection successful");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new DataSourcePluginException(
                    "check MongoDB connectivity failed, " + e.getMessage(), e);
        }
    }

    // Resolve the URI in requestParams of Map type
    private MongoClient createMongoClient(Map<String, String> requestParams) {

        checkArgument(
                requestParams.containsKey(MongodbCDCOptionRule.HOSTS.key()),
                String.format("Missing %s in requestParams", MongodbCDCOptionRule.HOSTS.key()));

        ConnectionString connectionString =
                buildConnectionString(
                        requestParams.get(MongodbCDCOptionRule.USERNAME.key()),
                        requestParams.get(MongodbCDCOptionRule.PASSWORD.key()),
                        requestParams.get(MongodbCDCOptionRule.HOSTS.key()),
                        requestParams.get(MongodbCDCOptionRule.CONNECTION_OPTIONS.key()));

        return MongoClients.create(connectionString);
    }
}
