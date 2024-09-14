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

package com.apache.seatunnel.datasource.plugin.mongodb;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginException;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MongoDataSourceChannel implements DataSourceChannel {

    @Override
    public OptionRule getDataSourceOptions(@NonNull String pluginName) {
        return MongoOptionRule.optionRule();
    }

    @Override
    public OptionRule getDatasourceMetadataFieldsByDataSourceName(@NonNull String pluginName) {
        return MongoOptionRule.metadataRule();
    }

    public List<String> getTables(
            @NonNull String pluginName,
            Map<String, String> requestParams,
            String database,
            Map<String, String> options) {
        List<String> tableNames = new ArrayList<>();
        try (MongoClient mongoClient = createMongoClient(requestParams)) {
            // list databases in this mongodb
            MongoIterable<String> namesIterator =
                    mongoClient.getDatabase(database).listCollectionNames();
            try (MongoCursor<String> iterator = namesIterator.iterator()) {
                while (iterator.hasNext()) {
                    tableNames.add(iterator.next());
                }
            }
            return tableNames;
        } catch (Exception e) {
            throw new DataSourcePluginException("get tables failed", e);
        }
    }

    @Override
    public List<String> getDatabases(
            @NonNull String pluginName, @NonNull Map<String, String> requestParams) {
        List<String> dbNames = new ArrayList<>();
        try (MongoClient mongoClient = createMongoClient(requestParams)) {
            // list databases in this mongodb
            MongoIterable<String> databaseNames = mongoClient.listDatabaseNames();
            try (MongoCursor<String> iterator = databaseNames.iterator()) {
                while (iterator.hasNext()) {
                    dbNames.add(iterator.next());
                }
            }
            return dbNames;
        } catch (Exception e) {
            throw new DataSourcePluginException("get databases failed", e);
        }
    }

    @Override
    public List<TableField> getTableFields(
            @NonNull String pluginName,
            @NonNull Map<String, String> requestParams,
            @NonNull String database,
            @NonNull String table) {
        try (MongoClient mongoClient = createMongoClient(requestParams)) {
            MongoCollection<Document> mongoCollection =
                    mongoClient.getDatabase(database).getCollection(table);
            // read first 100 rows from the table, and combine all keys to get the field types
            Map<String, Class<?>> mongoTypes = new HashMap<>();
            for (Document bson : mongoCollection.find().limit(100)) {
                bson.keySet().forEach(key -> mongoTypes.putIfAbsent(key, bson.get(key).getClass()));
            }
            List<TableField> tableFields = new ArrayList<>();
            mongoTypes.forEach(
                    (key, type) -> tableFields.add(createTableField(key, type.getSimpleName())));
            return tableFields;
        } catch (Exception e) {
            throw new DataSourcePluginException("get table fields failed", e);
        }
    }

    private TableField createTableField(String name, String type) {
        TableField field = new TableField();
        field.setType(type);
        field.setName(name);
        field.setPrimaryKey("_id".equals(name));
        field.setNullable(!"_id".equals(name));
        return field;
    }

    @Override
    public boolean checkDataSourceConnectivity(
            @NonNull String pluginName, @NonNull Map<String, String> requestParams) {
        try (MongoClient mongoClient = createMongoClient(requestParams)) {
            // Verify if the connection to mongodb was successful
            MongoIterable<String> databaseNames = mongoClient.listDatabaseNames();
            try (MongoCursor<String> iterator = databaseNames.iterator()) {
                if (iterator.hasNext()) {
                    log.info("mongoDB connection successful");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new DataSourcePluginException("check MongoDB connectivity failed", e);
        }
    }

    // Resolve the URI in requestParams of Map type
    private MongoClient createMongoClient(Map<String, String> requestParams) {
        return MongoClients.create(
                MongoRequestParamsUtils.parseStringFromRequestParams(requestParams));
    }
}
