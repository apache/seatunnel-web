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

import java.util.List;
import java.util.Map;

public interface IDatasourceService {

    /**
     * 创建数据源
     *
     * @param userId 用户id
     * @param datasourceName 数据源名称 is required //todo datasourceName 全局唯一 必须
     * @param pluginName is required
     * @param pluginVersion is required
     * @param description is optional
     * @param datasourceConfig is required
     * @return datasourceId
     */
    String createDatasource(
            Integer userId,
            String datasourceName,
            String pluginName,
            String pluginVersion,
            String description,
            Map<String, String> datasourceConfig)
            throws CodeGenerateUtils.CodeGenerateException;

    /**
     * 更新数据源
     *
     * @param userId 用户id
     * @param datasourceId 数据源id
     * @param datasourceName 数据源名称
     * @param description 数据源描述
     * @param datasourceConfig 数据源配�
     * @return boolean
     */
    boolean updateDatasource(
            Integer userId,
            Long datasourceId,
            String datasourceName,
            String description,
            Map<String, String> datasourceConfig);

    /**
     * 删除数据源
     *
     * @param userId 用户id
     * @param datasourceId 数据源id
     * @return boolean
     */
    boolean deleteDatasource(Integer userId, Long datasourceId);

    /**
     * 测试数据源是否可用
     *
     * @param userId 用户id
     * @param pluginName 数据源插件名称
     * @param pluginVersion 数据源插件版本 default is 1.0.0
     * @param datasourceConfig 数据源配置
     * @return boolean
     */
    boolean testDatasourceConnectionAble(
            Integer userId,
            String pluginName,
            String pluginVersion,
            Map<String, String> datasourceConfig);

    /**
     * 测试数据源是否可用
     *
     * @param userId 用户id
     * @param datasourceId 数据源id
     * @return boolean
     */
    boolean testDatasourceConnectionAble(Integer userId, Long datasourceId);

    /**
     * 查询数据源实例名称是否唯一
     *
     * @param userId 用户id
     * @param datasourceName 数据源名称
     * @param dataSourceId 数据源id
     * @return
     */
    boolean checkDatasourceNameUnique(Integer userId, String datasourceName, Long dataSourceId);

    /**
     * 查询数据源列表
     *
     * @param userId 用户id
     * @param pluginName 数据源插件名称
     * @param pageNo 页码
     * @param pageSize 每页条数
     * @return PageInfo<DatasourceRes>
     */
    PageInfo<DatasourceRes> queryDatasourceList(
            Integer userId, String searchVal, String pluginName, Integer pageNo, Integer pageSize);

    /**
     * 根据datasourceId 查询详情
     *
     * @param userId 用户id
     * @param datasourceId 数据源id
     * @return DatasourceDetailRes
     */
    DatasourceDetailRes queryDatasourceDetailById(Integer userId, String datasourceId);

    /**
     * 根据datasourceId 查询详情
     *
     * @param datasourceId 数据源id
     * @return DatasourceDetailRes
     */
    DatasourceDetailRes queryDatasourceDetailById(String datasourceId);

    /**
     * 根据datasourceName 查询详情
     *
     * @param datasourceName 数据源名称
     * @return DatasourceDetailRes
     */
    DatasourceDetailRes queryDatasourceDetailByDatasourceName(String datasourceName);

    /**
     * 根据datasourceIds 查询所有详情
     *
     * @param datasourceIds datasourceIds
     * @return List<DatasourceDetailRes>
     */
    List<DatasourceDetailRes> queryDatasourceDetailListByDatasourceIds(List<String> datasourceIds);

    /**
     * 注意：该接口仅限于导出数据使用，其他场景需要使用请提前评估！！！ 查询所有数据源实例 仅限于导出数据使用，其他场景需要使用请提前评估！！！
     *
     * @return all datasource instance
     */
    @Deprecated
    List<DatasourceDetailRes> queryAllDatasourcesInstance();

    /**
     * 根据datasourceName 查询配置参数
     *
     * @param datasourceId 数据源id
     * @return Map<String, String>
     */
    Map<String, String> queryDatasourceConfigById(String datasourceId);

    /**
     * 根据plugin name 查询datasourceName and id @liuli
     *
     * @param pluginName 数据源插件名称
     * @return List<String> key: datasourceId value: datasourceName
     */
    Map<String, String> queryDatasourceNameByPluginName(String pluginName);

    /**
     * 根据plugin name 查询配置参数
     *
     * @param pluginName 数据源插件名称
     * @return OptionRule @liuli
     */
    OptionRule queryOptionRuleByPluginName(String pluginName);

    /**
     * 根据plugin name 查询虚拟表的OptionRule
     *
     * @param pluginName 数据源插件名称
     * @return OptionRule
     */
    OptionRule queryVirtualTableOptionRuleByPluginName(String pluginName);

    /**
     * 查询所有支持的数据源
     *
     * @return
     */
    List<DataSourcePluginInfo> queryAllDatasources();

    /**
     * 按照类型查询所有支持的数据源
     *
     * @param type @see com.whaleops.datasource.plugin.api.DatasourcePluginTypeEnum
     * @return List<DataSourcePluginInfo>
     */
    List<DataSourcePluginInfo> queryAllDatasourcesByType(Integer type);

    /**
     * 所有支持的数据源
     *
     * @param onlyShowVirtualDataSource 是否只显示虚拟数据源
     * @return key: type, value: List<DataSourcePluginInfo> 按照类型进行返回
     */
    Map<Integer, List<DataSourcePluginInfo>> queryAllDatasourcesGroupByType(
            Boolean onlyShowVirtualDataSource);

    /**
     * 根据数据源id查询数据源名称
     *
     * @param datasourceId
     * @return
     */
    String queryDatasourceNameById(String datasourceId);

    /**
     * 根据数据源插件名称查询动态表单
     *
     * @param pluginName
     * @return String json
     */
    String getDynamicForm(String pluginName);

    /**
     * 根据数据源名称查询数据库名称
     *
     * @param datasourceName datasourceName
     * @return List<String> databaseName
     */
    List<String> queryDatabaseByDatasourceName(String datasourceName);

    /**
     * 根据数据源名称和数据库名称查询表名称
     *
     * @param datasourceName datasourceName
     * @param databaseName databaseName
     * @return List<String> tableName
     */
    List<String> queryTableNames(String datasourceName, String databaseName);

    /**
     * 根据数据源名称和数据库名称查询表结构
     *
     * @param datasourceName datasourceName
     * @param databaseName databaseName
     * @param tableName tableName
     * @return List<TableField> tableField
     */
    List<TableField> queryTableSchema(String datasourceName, String databaseName, String tableName);
}