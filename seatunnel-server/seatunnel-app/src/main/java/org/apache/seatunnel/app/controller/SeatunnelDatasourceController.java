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

package org.apache.seatunnel.app.controller;

import org.apache.seatunnel.app.common.Constants;
import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.dal.dao.IUserDao;
import org.apache.seatunnel.app.dal.entity.User;
import org.apache.seatunnel.app.domain.dto.datasource.DatabaseTableFields;
import org.apache.seatunnel.app.domain.dto.datasource.DatabaseTables;
import org.apache.seatunnel.app.domain.dto.datasource.TableInfo;
import org.apache.seatunnel.app.domain.request.datasource.DatasourceCheckReq;
import org.apache.seatunnel.app.domain.request.datasource.DatasourceReq;
import org.apache.seatunnel.app.domain.response.PageInfo;
import org.apache.seatunnel.app.domain.response.datasource.DatasourceDetailRes;
import org.apache.seatunnel.app.domain.response.datasource.DatasourceRes;
import org.apache.seatunnel.app.service.IDatasourceService;
import org.apache.seatunnel.app.utils.CartesianProductUtils;
import org.apache.seatunnel.app.utils.PropertyUtils;
import org.apache.seatunnel.common.utils.JsonUtils;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginInfo;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/seatunnel/api/v1/datasource")
public class SeatunnelDatasourceController extends BaseController {

    @Autowired private IDatasourceService datasourceService;

    @Resource(name = "userDaoImpl")
    private IUserDao userMapper;

    private static final String DEFAULT_PLUGIN_VERSION = "1.0.0";
    private static final String WS_SOURCE = "WS";

    private static final List<String> wsSupportDatasources =
            PropertyUtils.getList(Constants.WS_SUPPORT_DATASOURCES, Constants.COMMA);

    @ApiOperation(value = "create datasource", notes = "create datasource")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "datasourceName",
                value = "datasource name",
                required = true,
                dataType = "String",
                paramType = "query"),
        @ApiImplicitParam(
                name = "pluginName",
                value = "plugin name",
                required = true,
                dataType = "String",
                paramType = "query"),
        @ApiImplicitParam(
                name = "description",
                value = "description",
                required = false,
                dataType = "String",
                paramType = "query"),
        @ApiImplicitParam(
                name = "datasourceConfig",
                value = "datasource config",
                required = true,
                dataType = "String",
                paramType = "query")
    })
    @PostMapping("/create")
    Result<String> createDatasource(@RequestBody DatasourceReq req) {
        String datasourceConfig = req.getDatasourceConfig();
        Map<String, String> stringStringMap = JsonUtils.toMap(datasourceConfig);
        return Result.success(
                datasourceService.createDatasource(
                        req.getDatasourceName(),
                        req.getPluginName(),
                        DEFAULT_PLUGIN_VERSION,
                        req.getDescription(),
                        stringStringMap));
    }

    @ApiOperation(value = "test datasource connection", notes = "test datasource connection")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "pluginName",
                value = "plugin name",
                required = true,
                dataType = "String",
                paramType = "query"),
        @ApiImplicitParam(
                name = "datasourceConfig",
                value = "datasource config",
                required = true,
                dataType = "String",
                paramType = "query")
    })
    @PostMapping("/check/connect")
    Result<Boolean> testConnect(@RequestBody DatasourceCheckReq req) {
        return Result.success(
                datasourceService.testDatasourceConnectionAble(
                        req.getPluginName(), DEFAULT_PLUGIN_VERSION, req.getDatasourceConfig()));
    }

    @ApiOperation(value = "update datasource", notes = "update datasource")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "datasourceName",
                value = "datasource name",
                required = false,
                dataType = "String",
                paramType = "query"),
        @ApiImplicitParam(
                name = "description",
                value = "description",
                required = false,
                dataType = "String",
                paramType = "query"),
        @ApiImplicitParam(
                name = "datasourceConfig",
                value = "datasource config",
                required = false,
                dataType = "String",
                paramType = "query")
    })
    @PutMapping("/{id}")
    Result<Boolean> updateDatasource(
            @PathVariable("id") String id, @RequestBody DatasourceReq req) {
        Map<String, String> stringStringMap = JsonUtils.toMap(req.getDatasourceConfig());
        Long datasourceId = Long.parseLong(id);
        return Result.success(
                datasourceService.updateDatasource(
                        datasourceId,
                        req.getDatasourceName(),
                        req.getDescription(),
                        stringStringMap));
    }

    @ApiOperation(value = "delete datasource by id", notes = "delete datasource by id")
    @DeleteMapping("/{id}")
    Result<Boolean> deleteDatasource(@PathVariable("id") String id) {
        Long datasourceId = Long.parseLong(id);
        return Result.success(datasourceService.deleteDatasource(datasourceId));
    }

    @ApiOperation(value = "get datasource detail", notes = "get datasource detail")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "userId",
                value = "user id",
                required = true,
                dataType = "String",
                paramType = "query"),
    })
    @GetMapping("/{id}")
    Result<DatasourceDetailRes> getDatasource(@PathVariable("id") String id) {
        return Result.success(datasourceService.queryDatasourceDetailById(id));
    }

    @ApiOperation(value = "get datasource list", notes = "get datasource list")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "searchVal",
                value = "search value",
                required = false,
                dataType = "String",
                paramType = "query"),
        @ApiImplicitParam(
                name = "pluginName",
                value = "plugin name",
                required = false,
                dataType = "String",
                paramType = "query"),
        @ApiImplicitParam(
                name = "pageNo",
                value = "page no",
                required = false,
                dataType = "Integer",
                paramType = "query"),
        @ApiImplicitParam(
                name = "pageSize",
                value = "page size",
                required = false,
                dataType = "Integer",
                paramType = "query")
    })
    @GetMapping("/list")
    Result<PageInfo<DatasourceRes>> getDatasourceList(
            @RequestParam("searchVal") String searchVal,
            @RequestParam("pluginName") String pluginName,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("pageSize") Integer pageSize) {
        PageInfo<DatasourceRes> datasourceResPageInfo =
                datasourceService.queryDatasourceList(searchVal, pluginName, pageNo, pageSize);
        if (CollectionUtils.isNotEmpty(datasourceResPageInfo.getData())) {
            Map<Integer, String> userIdNameMap = userIdNameMap();
            datasourceResPageInfo
                    .getData()
                    .forEach(
                            datasourceRes -> {
                                Map<String, String> datasourceConfig =
                                        datasourceRes.getDatasourceConfig();
                                Optional.ofNullable(
                                                MapUtils.getString(
                                                        datasourceConfig, Constants.PASSWORD))
                                        .ifPresent(
                                                password -> {
                                                    datasourceConfig.put(
                                                            Constants.PASSWORD,
                                                            CartesianProductUtils.maskPassword(
                                                                    password));
                                                });
                                datasourceRes.setDatasourceConfig(datasourceConfig);
                                datasourceRes.setCreateUserName(
                                        userIdNameMap.getOrDefault(
                                                datasourceRes.getCreateUserId(), ""));
                                datasourceRes.setUpdateUserName(
                                        userIdNameMap.getOrDefault(
                                                datasourceRes.getUpdateUserId(), ""));
                            });
        }
        return Result.success(datasourceResPageInfo);
    }

    @ApiOperation(value = "get datasource type list", notes = "get datasource type list")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "showVirtualDataSource",
                value = "show virtual datasource",
                required = false,
                defaultValue = "true",
                dataType = "Boolean",
                paramType = "query")
    })
    @GetMapping("/support-datasources")
    Result<Map<Integer, List<DataSourcePluginInfo>>> getSupportDatasources(
            @RequestParam("showVirtualDataSource") Boolean showVirtualDataSource,
            @RequestParam(value = "source", required = false) String source) {
        Map<Integer, List<DataSourcePluginInfo>> allDatasources =
                datasourceService.queryAllDatasourcesGroupByType(showVirtualDataSource);
        // default source is WS
        if (StringUtils.isEmpty(source) || source.equals(WS_SOURCE)) {
            allDatasources.forEach(
                    (k, typeList) -> {
                        typeList =
                                typeList.stream()
                                        .filter(
                                                plugin ->
                                                        wsSupportDatasources.contains(
                                                                plugin.getName()))
                                        .collect(Collectors.toList());
                        allDatasources.put(k, typeList);
                    });
        }
        return Result.success(allDatasources);
    }

    @GetMapping("/dynamic-form")
    Result<String> getDynamicForm(@RequestParam("pluginName") String pluginName) {
        return Result.success(datasourceService.getDynamicForm(pluginName));
    }

    @GetMapping("/databases")
    Result<List<String>> getDatabases(@RequestParam("datasourceName") String datasourceName) {
        return Result.success(datasourceService.queryDatabaseByDatasourceName(datasourceName));
    }

    @GetMapping("/tables")
    Result<List<String>> getTableNames(
            @RequestParam("datasourceName") String datasourceName,
            @RequestParam("databaseName") String databaseName,
            @RequestParam("filterName") String filterName,
            @RequestParam("size") Integer size) {
        return Result.success(
                datasourceService.queryTableNames(datasourceName, databaseName, filterName, size));
    }

    @GetMapping("/schema")
    Result<List<TableField>> getTableFields(
            @RequestParam("datasourceId") String datasourceId,
            @RequestParam(value = "databaseName", required = false) String databaseName,
            @RequestParam("tableName") String tableName) {
        DatasourceDetailRes res = datasourceService.queryDatasourceDetailById(datasourceId);
        if (StringUtils.isEmpty(databaseName)) {
            throw new SeatunnelException(
                    SeatunnelErrorEnum.INVALID_DATASOURCE, res.getDatasourceName());
        }
        List<TableField> tableFields =
                datasourceService.queryTableSchema(
                        res.getDatasourceName(), databaseName, tableName);
        return Result.success(tableFields);
    }

    @PostMapping("/schemas")
    Result<List<DatabaseTableFields>> getMultiTableFields(
            @RequestParam("datasourceId") String datasourceId,
            @RequestBody List<DatabaseTables> tableNames) {
        DatasourceDetailRes res = datasourceService.queryDatasourceDetailById(datasourceId);
        List<DatabaseTableFields> tableFields = new ArrayList<>();
        tableNames.forEach(
                database -> {
                    List<TableInfo> tableInfos = new ArrayList<>();
                    database.getTables()
                            .forEach(
                                    tableName -> {
                                        List<TableField> tableField =
                                                datasourceService.queryTableSchema(
                                                        res.getDatasourceName(),
                                                        database.getDatabase(),
                                                        tableName);
                                        tableInfos.add(new TableInfo(tableName, tableField));
                                    });
                    tableFields.add(new DatabaseTableFields(database.getDatabase(), tableInfos));
                });
        return Result.success(tableFields);
    }

    @GetMapping("/all-tables")
    Result<List<DatabaseTables>> getTables(@RequestParam("datasourceId") String datasourceId) {
        DatasourceDetailRes res = datasourceService.queryDatasourceDetailById(datasourceId);
        List<DatabaseTables> tables = new ArrayList<>();
        List<String> databases =
                datasourceService.queryDatabaseByDatasourceName(res.getDatasourceName());
        databases.forEach(
                database -> {
                    tables.add(
                            new DatabaseTables(
                                    database,
                                    datasourceService.queryTableNames(
                                            res.getDatasourceName(), database)));
                });
        return Result.success(tables);
    }

    public Map<Integer, String> userIdNameMap() {
        return userMapper.queryEnabledUsers().stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
    }
}
