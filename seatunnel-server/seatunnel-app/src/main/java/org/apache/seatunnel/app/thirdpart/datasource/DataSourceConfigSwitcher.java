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

package org.apache.seatunnel.app.thirdpart.datasource;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.app.domain.request.connector.BusinessMode;
import org.apache.seatunnel.app.domain.request.job.DataSourceOption;
import org.apache.seatunnel.app.domain.request.job.SelectTableFields;
import org.apache.seatunnel.app.domain.response.datasource.VirtualTableDetailRes;
import org.apache.seatunnel.app.dynamicforms.FormStructure;
import org.apache.seatunnel.common.constants.PluginType;
import org.apache.seatunnel.shade.com.typesafe.config.Config;

import java.util.List;

public interface DataSourceConfigSwitcher {

    /**
     * 使用数据源的OptionRule去过滤连接器的OptionRule
     *
     * @param connectorName 连接器名称
     * @param dataSourceOptionRule 数据源的OptionRule
     * @param virtualTableOptionRule 虚拟表的OptionRule
     * @param businessMode 业务模式
     * @param pluginType Source or Sink
     * @param connectorOptionRule 连接器的OptionRule
     * @param excludedKeys 额外需要删除的字段
     * @return
     */
    FormStructure filterOptionRule(
            String connectorName,
            OptionRule dataSourceOptionRule,
            OptionRule virtualTableOptionRule,
            BusinessMode businessMode,
            PluginType pluginType,
            OptionRule connectorOptionRule,
            List<String> excludedKeys);

    /**
     * 使用数据源实例的参数 和 连接器配置的参数合并成最终连接器的参数
     *
     * @param dataSourceInstanceConfig 数据源参数
     * @param virtualTableDetail 虚拟表参数，如果是虚拟表的话
     * @param dataSourceOption 前端选的库表
     * @param selectTableFields 前端选的字段
     * @param businessMode 业务模式
     * @param pluginType source or sink
     * @param connectorConfig 连接器是参数
     * @return 返回合并了的参数列表
     */
    Config mergeDatasourceConfig(
            Config dataSourceInstanceConfig,
            VirtualTableDetailRes virtualTableDetail,
            DataSourceOption dataSourceOption,
            SelectTableFields selectTableFields,
            BusinessMode businessMode,
            PluginType pluginType,
            Config connectorConfig);
}
