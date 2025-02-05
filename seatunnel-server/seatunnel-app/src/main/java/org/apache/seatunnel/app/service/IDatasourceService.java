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

package org.apache.seatunnel.app.service;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.app.domain.response.PageInfo;
import org.apache.seatunnel.app.domain.response.datasource.DatasourceDetailRes;
import org.apache.seatunnel.app.domain.response.datasource.DatasourceRes;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginInfo;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;
import org.apache.seatunnel.server.common.CodeGenerateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IDatasourceService {
    /**
     * create datasource
     *
     * @param datasourceName is required //todo datasourceName global is required
     * @param pluginName is required
     * @param pluginVersion is required
     * @param description is optional
     * @param datasourceConfig is required
     * @return datasourceId
     */
    String createDatasource(
            String datasourceName,
            String pluginName,
            String pluginVersion,
            String description,
            Map<String, String> datasourceConfig)
            throws CodeGenerateUtils.CodeGenerateException;

    /**
     * update datasource
     *
     * @param datasourceId datasource id
     * @param datasourceName datasourceName
     * @param description description
     * @param datasourceConfig datasourceConfig
     * @return boolean
     */
    boolean updateDatasource(
            Long datasourceId,
            String datasourceName,
            String description,
            Map<String, String> datasourceConfig);

    /**
     * delete datasource
     *
     * @param datasourceId datasourceId
     * @return boolean
     */
    boolean deleteDatasource(Long datasourceId);

    /**
     * test datasource is used
     *
     * @param pluginName pluginName
     * @param pluginVersion pluginVersion default is 1.0.0
     * @param datasourceConfig datasourceConfig
     * @return boolean
     */
    boolean testDatasourceConnectionAble(
            String pluginName, String pluginVersion, Map<String, String> datasourceConfig);

    /**
     * test datasource is used
     *
     * @param datasourceId datasourceId
     * @return boolean
     */
    boolean testDatasourceConnectionAble(Long datasourceId);

    /**
     * checkDatasourceNameUnique
     *
     * @param datasourceName datasourceName
     * @param dataSourceId dataSourceId
     * @return boolean
     */
    boolean checkDatasourceNameUnique(String datasourceName, Long dataSourceId);

    /**
     * queryDatasourceList
     *
     * @param pluginName pluginName
     * @param pageNo pageNo
     * @param pageSize pageSize
     * @return PageInfo DatasourceRes
     */
    PageInfo<DatasourceRes> queryDatasourceList(
            String searchVal, String pluginName, Integer pageNo, Integer pageSize);

    /**
     * datasourceId query detail
     *
     * @param datasourceId datasourceId
     * @return DatasourceDetailRes
     */
    DatasourceDetailRes queryDatasourceDetailById(String datasourceId);

    /**
     * datasourceName query detail
     *
     * @param datasourceName datasourceName
     * @return DatasourceDetailRes
     */
    DatasourceDetailRes queryDatasourceDetailByDatasourceName(String datasourceName);

    /**
     * datasourceIds
     *
     * @param datasourceIds datasourceIds
     * @return List DatasourceDetailRes
     */
    List<DatasourceDetailRes> queryDatasourceDetailListByDatasourceIds(List<String> datasourceIds);

    /**
     * Note: This interface is only used for exporting data, please evaluate it in advance for other
     * scenarios!!! Querying all data source instances is limited to exporting data for use, please
     * evaluate in advance for other scenarios!!!
     *
     * @return all datasource instance
     */
    @Deprecated
    List<DatasourceDetailRes> queryAllDatasourcesInstance();

    /**
     * datasourceName query config
     *
     * @param datasourceId datasourceId
     * @return Map
     */
    Map<String, String> queryDatasourceConfigById(String datasourceId);

    /**
     * plugin name datasourceName and id @liuli
     *
     * @param pluginName pluginName
     * @return List String key: datasourceId value: datasourceName
     */
    Map<String, String> queryDatasourceNameByPluginName(String pluginName);

    /**
     * plugin name query config
     *
     * @param pluginName pluginName
     * @return OptionRule @liuli
     */
    OptionRule queryOptionRuleByPluginName(String pluginName);

    /**
     * plugin name OptionRule
     *
     * @param pluginName pluginName
     * @return OptionRule
     */
    OptionRule queryVirtualTableOptionRuleByPluginName(String pluginName);

    /**
     * query all datasource
     *
     * @return list
     */
    List<DataSourcePluginInfo> queryAllDatasources();

    /**
     * query all datasource by type
     *
     * @param type @see org.apache.seatunnel.datasource.plugin.api.DatasourcePluginTypeEnum
     * @return List DataSourcePluginInfo
     */
    List<DataSourcePluginInfo> queryAllDatasourcesByType(Integer type);

    /**
     * all datasource
     *
     * @param onlyShowVirtualDataSource onlyShowVirtualDataSource
     * @return key: type, value: List DataSourcePluginInfo
     */
    Map<Integer, List<DataSourcePluginInfo>> queryAllDatasourcesGroupByType(
            Boolean onlyShowVirtualDataSource);

    /**
     * query by id
     *
     * @param datasourceId datasourceId
     * @return name
     */
    String queryDatasourceNameById(String datasourceId);

    /**
     * query dynamic form by pluginName
     *
     * @param pluginName pluginName
     * @return String json
     */
    String getDynamicForm(String pluginName);

    /**
     * queryDatabaseByDatasourceName
     *
     * @param datasourceName datasourceName
     * @return List String databaseName
     */
    List<String> queryDatabaseByDatasourceName(String datasourceName);

    /**
     * queryTableNames
     *
     * @param datasourceName datasourceName
     * @param databaseName databaseName
     * @return List tableName
     */
    List<String> queryTableNames(String datasourceName, String databaseName);

    /**
     * queryTableSchema
     *
     * @param datasourceName datasourceName
     * @param databaseName databaseName
     * @param tableName tableName
     * @return List tableField
     */
    List<TableField> queryTableSchema(String datasourceName, String databaseName, String tableName);

    default List<String> queryTableNames(
            String datasourceName, String databaseName, String filterName, Integer size) {
        return new ArrayList<>();
    }
}
