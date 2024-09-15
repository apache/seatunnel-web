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
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        Set<String> nullables = new HashSet<>();
        Map<String, Class<?>> mongoTypes = new HashMap<>();
        try (MongoClient mongoClient = createMongoClient(requestParams)) {
            MongoCollection<Document> mongoCollection =
                    mongoClient.getDatabase(database).getCollection(table);
            // read first 100 rows from the table, and combine all keys to get the field types
            for (Document bson : mongoCollection.find().limit(100)) {
                bson.keySet()
                        .forEach(
                                key -> {
                                    Object val = bson.get(key);
                                    if (val == null) {
                                        nullables.add(key);
                                    } else {
                                        mongoTypes.putIfAbsent(key, val.getClass());
                                    }
                                });
            }
        } catch (Exception e) {
            throw new DataSourcePluginException("get table fields failed", e);
        }

        List<TableField> tableFields = new ArrayList<>();
        mongoTypes.forEach(
                (key, type) ->
                        tableFields.add(createTableField(key, type, nullables.contains(key))));
        return tableFields;
    }

    private TableField createTableField(String name, Class<?> type, boolean nullable) {
        TableField field = new TableField();
        field.setName(name);
        field.setType(toSeaTunnelType(type));
        field.setPrimaryKey("_id".equals(name));
        field.setNullable(nullable);
        return field;
    }

    private String toSeaTunnelType(Class<?> type) {
        if (type.equals(ObjectId.class)
                || type.equals(String.class)
                || type.isAssignableFrom(Map.class)) {
            return "string";
        } else if (type.equals(Byte.class)) {
            return "tinyint";
        } else if (type.equals(Short.class)) {
            return "smallint";
        } else if (type.equals(Integer.class)) {
            return "int";
        } else if (type.equals(Long.class)) {
            return "bigint";
        } else if (type.equals(Float.class)) {
            return "float";
        } else if (type.equals(Double.class)) {
            return "double";
        } else if (type.equals(Decimal128.class)) {
            return "decimal(38, 18)";
        } else if (type.equals(Boolean.class)) {
            return "boolean";
        } else if (type.equals(java.util.Date.class) || type.equals(java.sql.Date.class)) {
            return "date";
        } else if (type.equals(java.sql.Time.class)) {
            return "time";
        } else if (type.equals(Timestamp.class)) {
            return "timestamp";
        } else if (type.equals(byte[].class)) {
            return "bytes";
        } else if (type.equals(int[].class)) {
            return "array<int>";
        } else if (type.equals(long[].class)) {
            return "array<bigint>";
        } else if (type.equals(String[].class)) {
            return "array<string>";
        } else if (type.isAssignableFrom(List.class)) {
            return "array<string>";
        } else {
            log.warn("unsupported type: {}", type);
            return null;
        }
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
