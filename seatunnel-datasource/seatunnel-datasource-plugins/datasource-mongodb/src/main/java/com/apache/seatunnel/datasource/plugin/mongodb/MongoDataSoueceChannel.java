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

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoIterable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
public class MongoDataSoueceChannel implements DataSourceChannel {

    private static final String DATABASE = "default";

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

        return MongoClients.create(
                MongoRequestParamsUtils.parseStringFromRequestParams(requestParams));
    }
}
