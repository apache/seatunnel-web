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

package org.apache.seatunnel.app.thirdparty.datasource;

import org.apache.seatunnel.shade.com.typesafe.config.Config;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.app.domain.request.connector.BusinessMode;
import org.apache.seatunnel.app.domain.request.job.DataSourceOption;
import org.apache.seatunnel.app.domain.request.job.SelectTableFields;
import org.apache.seatunnel.app.domain.response.datasource.VirtualTableDetailRes;
import org.apache.seatunnel.app.dynamicforms.FormStructure;
import org.apache.seatunnel.common.constants.PluginType;
import org.apache.seatunnel.common.utils.SeaTunnelException;

import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

public class DataSourceConfigSwitcherUtils {

    public static FormStructure filterOptionRule(
            String datasourceName,
            String connectorName,
            OptionRule dataSourceOptionRule,
            OptionRule virtualTableOptionRule,
            PluginType pluginType,
            BusinessMode businessMode,
            OptionRule connectorOptionRule) {
        DataSourceConfigSwitcher dataSourceConfigSwitcher =
                getDataSourceConfigSwitcher(datasourceName.toUpperCase());
        return dataSourceConfigSwitcher.filterOptionRule(
                connectorName,
                dataSourceOptionRule,
                virtualTableOptionRule,
                businessMode,
                pluginType,
                connectorOptionRule,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
    }

    public static Config mergeDatasourceConfig(
            String datasourceName,
            Config dataSourceInstanceConfig,
            VirtualTableDetailRes virtualTableDetail,
            DataSourceOption dataSourceOption,
            SelectTableFields selectTableFields,
            BusinessMode businessMode,
            PluginType pluginType,
            Config connectorConfig) {
        DataSourceConfigSwitcher dataSourceConfigSwitcher =
                getDataSourceConfigSwitcher(datasourceName.toUpperCase());
        return dataSourceConfigSwitcher.mergeDatasourceConfig(
                dataSourceInstanceConfig,
                virtualTableDetail,
                dataSourceOption,
                selectTableFields,
                businessMode,
                pluginType,
                connectorConfig);
    }

    private static DataSourceConfigSwitcher getDataSourceConfigSwitcher(String datasourceName) {
        checkNotNull(datasourceName, "datasourceName cannot be null");
        DataSourceConfigSwitcher configSwitcher =
                DatasourceConfigSwitcherProvider.INSTANCE.getConfigSwitcher(
                        datasourceName.toUpperCase());
        if (configSwitcher == null) {
            throw new SeaTunnelException(
                    "data source : "
                            + datasourceName
                            + " is no implementation class for DataSourceConfigSwitcher");
        }
        return configSwitcher;
    }
}
