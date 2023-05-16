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

package org.apache.seatunnel.datasource;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.datasource.exception.DataSourceSDKException;
import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourceFactory;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginInfo;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;
import org.apache.seatunnel.datasource.service.DataSourceService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractDataSourceClient implements DataSourceService {

    private Map<String, DataSourcePluginInfo> supportedDataSourceInfo = new HashMap<>();

    private Map<String, Integer> supportedDataSourceIndex = new HashMap<>();

    protected List<DataSourcePluginInfo> supportedDataSources = new ArrayList<>();

    private List<DataSourceChannel> dataSourceChannels = new ArrayList<>();

    protected AbstractDataSourceClient() {
        AtomicInteger dataSourceIndex = new AtomicInteger();
        ServiceLoader.load(DataSourceFactory.class)
                .forEach(
                        seaTunnelDataSourceFactory -> {
                            seaTunnelDataSourceFactory
                                    .supportedDataSources()
                                    .forEach(
                                            dataSourceInfo -> {
                                                supportedDataSourceInfo.put(
                                                        dataSourceInfo.getName().toUpperCase(),
                                                        dataSourceInfo);
                                                supportedDataSourceIndex.put(
                                                        dataSourceInfo.getName().toUpperCase(),
                                                        dataSourceIndex.get());
                                                supportedDataSources.add(dataSourceInfo);
                                            });
                            dataSourceChannels.add(seaTunnelDataSourceFactory.createChannel());
                            dataSourceIndex.getAndIncrement();
                        });
        if (supportedDataSourceInfo.isEmpty()) {
            throw new DataSourceSDKException("No supported data source found");
        }
    }

    @Override
    public Boolean checkDataSourceConnectivity(
            String pluginName, Map<String, String> dataSourceParams) {
        return getDataSourceChannel(pluginName)
                .checkDataSourceConnectivity(pluginName, dataSourceParams);
    }

    @Override
    public List<DataSourcePluginInfo> listAllDataSources() {
        return supportedDataSources;
    }

    protected DataSourceChannel getDataSourceChannel(String pluginName) {
        checkNotNull(pluginName, "pluginName cannot be null");
        Integer index = supportedDataSourceIndex.get(pluginName.toUpperCase());
        if (index == null) {
            throw new DataSourceSDKException(
                    "The %s plugin is not supported or plugin not exist.", pluginName);
        }
        return dataSourceChannels.get(index);
    }

    @Override
    public OptionRule queryDataSourceFieldByName(String pluginName) {
        return getDataSourceChannel(pluginName).getDataSourceOptions(pluginName);
    }

    @Override
    public OptionRule queryMetadataFieldByName(String pluginName) {
        return getDataSourceChannel(pluginName)
                .getDatasourceMetadataFieldsByDataSourceName(pluginName);
    }

    @Override
    public List<String> getTables(
            String pluginName, String databaseName, Map<String, String> requestParams) {
        return getDataSourceChannel(pluginName).getTables(pluginName, requestParams, databaseName);
    }

    @Override
    public List<String> getDatabases(String pluginName, Map<String, String> requestParams) {
        return getDataSourceChannel(pluginName).getDatabases(pluginName, requestParams);
    }

    @Override
    public List<TableField> getTableFields(
            String pluginName,
            Map<String, String> requestParams,
            String databaseName,
            String tableName) {
        return getDataSourceChannel(pluginName)
                .getTableFields(pluginName, requestParams, databaseName, tableName);
    }

    @Override
    public Map<String, List<TableField>> getTableFields(
            String pluginName,
            Map<String, String> requestParams,
            String databaseName,
            List<String> tableNames) {
        return getDataSourceChannel(pluginName)
                .getTableFields(pluginName, requestParams, databaseName, tableNames);
    }
}
