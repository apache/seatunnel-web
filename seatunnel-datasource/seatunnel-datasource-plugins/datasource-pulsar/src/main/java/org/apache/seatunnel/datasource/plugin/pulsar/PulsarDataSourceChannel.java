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

package org.apache.seatunnel.datasource.plugin.pulsar;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginException;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.api.PulsarClientException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
public class PulsarDataSourceChannel implements DataSourceChannel {

    private static final String TENANT = "public";
    private static final String DATABASE = "public/default";

    @Override
    public OptionRule getDataSourceOptions(@NonNull String pluginName) {
        return PulsarOptionRule.optionRule();
    }

    @Override
    public OptionRule getDatasourceMetadataFieldsByDataSourceName(@NonNull String pluginName) {
        return PulsarOptionRule.metadataRule();
    }

    @Override
    public List<String> getTables(
            @NonNull String pluginName,
            Map<String, String> requestParams,
            String database,
            Map<String, String> option) {
        checkArgument(StringUtils.equalsIgnoreCase(database, DATABASE), "database must be default");
        try (PulsarAdmin pulsarAdmin = createPulsarAdmin(requestParams)) {
            return pulsarAdmin.topics().getList(database);
        } catch (Exception ex) {
            throw new DataSourcePluginException(
                    "check Pulsar connectivity failed, " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<String> getDatabases(
            @NonNull String pluginName, @NonNull Map<String, String> requestParams) {
        try (PulsarAdmin pulsarAdmin = createPulsarAdmin(requestParams)) {
            return pulsarAdmin.namespaces().getNamespaces(TENANT);
        } catch (Exception ex) {
            throw new DataSourcePluginException(
                    "check Pulsar connectivity failed, " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean checkDataSourceConnectivity(
            @NonNull String pluginName, @NonNull Map<String, String> requestParams) {
        try (PulsarAdmin pulsarAdmin = createPulsarAdmin(requestParams)) {
            // just test the connection
            List<String> clusters = pulsarAdmin.clusters().getClusters();
            return CollectionUtils.isNotEmpty(clusters);
        } catch (Exception ex) {
            throw new DataSourcePluginException(
                    "check Pulsar connectivity failed, " + ex.getMessage(), ex);
        }
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
    public Map<String, List<TableField>> getTableFields(
            @NonNull String pluginName,
            @NonNull Map<String, String> requestParams,
            @NonNull String database,
            @NonNull List<String> tables) {
        checkArgument(StringUtils.equalsIgnoreCase(database, DATABASE), "database must be default");
        return Collections.emptyMap();
    }

    private PulsarAdmin createPulsarAdmin(Map<String, String> requestParams)
            throws PulsarClientException {
        return PulsarAdmin.builder()
                .serviceHttpUrl(requestParams.get("admin.service-url"))
                .loadConf(getConfMap(requestParams))
                .build();
    }

    private HashMap<String, Object> getConfMap(Map<String, String> requestParams) {
        HashMap<String, Object> confMap = new HashMap<>();
        if (requestParams.size() == 0) {
            return confMap;
        }
        if (requestParams.get("pulsar.config") != null) {
            String[] lines = requestParams.get("pulsar.config").split(";");
            for (String line : lines) {
                String[] keyValue = line.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    confMap.put(key, value);
                }
            }
        }
        Set<String> pSet = new HashSet<>();
        pSet.add("client.service-url");
        pSet.add("admin.service-url");
        pSet.add("topic");
        pSet.add("topic-pattern");
        Map<String, Object> filteredParams =
                requestParams.entrySet().stream()
                        .filter(entry -> !pSet.contains(entry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        confMap.putAll(filteredParams);
        return confMap;
    }
}
