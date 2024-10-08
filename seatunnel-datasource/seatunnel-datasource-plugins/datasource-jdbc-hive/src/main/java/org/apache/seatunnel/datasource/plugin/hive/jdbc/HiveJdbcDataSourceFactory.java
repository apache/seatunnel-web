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

import org.apache.seatunnel.datasource.plugin.api.DataSourceChannel;
import org.apache.seatunnel.datasource.plugin.api.DataSourceFactory;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginInfo;
import org.apache.seatunnel.datasource.plugin.api.DatasourcePluginTypeEnum;

import java.util.HashSet;
import java.util.Set;

public class HiveJdbcDataSourceFactory implements DataSourceFactory {
    @Override
    public String factoryIdentifier() {
        return HiveJdbcConstants.PLUGIN_NAME;
    }

    @Override
    public Set<DataSourcePluginInfo> supportedDataSources() {
        DataSourcePluginInfo dataSourcePluginInfo =
                DataSourcePluginInfo.builder()
                        .name(HiveJdbcConstants.PLUGIN_NAME)
                        .type(DatasourcePluginTypeEnum.DATABASE.getCode())
                        .version("1.0.0")
                        .icon(HiveJdbcConstants.PLUGIN_NAME)
                        .supportVirtualTables(false)
                        .build();
        Set<DataSourcePluginInfo> dataSourceInfos = new HashSet<>();
        dataSourceInfos.add(dataSourcePluginInfo);
        return dataSourceInfos;
    }

    @Override
    public DataSourceChannel createChannel() {
        return new HiveJdbcDataSourceChannel();
    }
}
