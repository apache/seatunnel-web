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

package org.apache.seatunnel.app.dal.dao.impl;

import org.apache.seatunnel.app.dal.dao.IDatasourceDao;
import org.apache.seatunnel.app.dal.entity.Datasource;
import org.apache.seatunnel.app.dal.mapper.DatasourceMapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.seatunnel.app.utils.ServletUtils.getCurrentWorkspaceId;

@Repository
public class DatasourceDaoImpl implements IDatasourceDao {

    @Resource private DatasourceMapper datasourceMapper;

    @Override
    public boolean insertDatasource(Datasource datasource) {
        return datasourceMapper.insert(datasource) > 0;
    }

    @Override
    public Datasource selectDatasourceById(Long id) {
        return datasourceMapper.selectOne(
                new QueryWrapper<Datasource>()
                        .eq("id", id)
                        .eq("workspace_id", getCurrentWorkspaceId()));
    }

    @Override
    public boolean deleteDatasourceById(Long id) {
        return datasourceMapper.delete(
                        new QueryWrapper<Datasource>()
                                .eq("id", id)
                                .eq("workspace_id", getCurrentWorkspaceId()))
                > 0;
    }

    @Override
    public Datasource queryDatasourceByName(String name) {
        return datasourceMapper.selectOne(
                new QueryWrapper<Datasource>()
                        .eq("datasource_name", name)
                        .eq("workspace_id", getCurrentWorkspaceId()));
    }

    @Override
    public boolean updateDatasourceById(Datasource datasource) {
        return datasourceMapper.update(
                        datasource,
                        new QueryWrapper<Datasource>()
                                .eq("id", datasource.getId())
                                .eq("workspace_id", getCurrentWorkspaceId()))
                > 0;
    }

    @Override
    public boolean checkDatasourceNameUnique(String dataSourceName, Long dataSourceId) {
        QueryWrapper<Datasource> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("datasource_name", dataSourceName);
        queryWrapper.ne("id", dataSourceId);
        queryWrapper.eq("workspace_id", getCurrentWorkspaceId());
        return datasourceMapper.selectList(queryWrapper).isEmpty();
    }

    @Override
    public IPage<Datasource> selectDatasourcePage(Page<Datasource> page) {
        return datasourceMapper.selectPage(
                page, new QueryWrapper<Datasource>().eq("workspace_id", getCurrentWorkspaceId()));
    }

    @Override
    public IPage<Datasource> selectDatasourceByParam(
            Page<Datasource> page,
            List<Long> availableDatasourceIds,
            String searchVal,
            String pluginName) {

        QueryWrapper<Datasource> datasourceQueryWrapper = new QueryWrapper<>();
        if (availableDatasourceIds != null) {
            datasourceQueryWrapper.in("id", availableDatasourceIds);
        }
        datasourceQueryWrapper.eq("workspace_id", getCurrentWorkspaceId());
        if (searchVal != null
                && !searchVal.isEmpty()
                && pluginName != null
                && !pluginName.isEmpty()) {
            return datasourceMapper.selectPage(
                    page,
                    datasourceQueryWrapper
                            .eq("plugin_name", pluginName)
                            .like("datasource_name", searchVal));
        }
        if (searchVal != null && !searchVal.isEmpty()) {
            return datasourceMapper.selectPage(
                    page, datasourceQueryWrapper.like("datasource_name", searchVal));
        }
        if (pluginName != null && !pluginName.isEmpty()) {
            return datasourceMapper.selectPage(
                    page, datasourceQueryWrapper.eq("plugin_name", pluginName));
        }
        return datasourceMapper.selectPage(page, datasourceQueryWrapper);
    }

    @Override
    public String queryDatasourceNameById(Long id) {
        return datasourceMapper
                .selectOne(
                        new QueryWrapper<Datasource>()
                                .eq("id", id)
                                .eq("workspace_id", getCurrentWorkspaceId()))
                .getDatasourceName();
    }

    @Override
    public List<Datasource> selectDatasourceByPluginName(String pluginName, String pluginVersion) {
        return datasourceMapper.selectList(
                new QueryWrapper<Datasource>()
                        .eq("plugin_name", pluginName)
                        .eq("plugin_version", pluginVersion)
                        .eq("workspace_id", getCurrentWorkspaceId()));
    }

    @Override
    public List<Datasource> selectDatasourceByIds(List<Long> ids) {
        return datasourceMapper.selectList(
                new QueryWrapper<Datasource>()
                        .in("id", ids)
                        .eq("workspace_id", getCurrentWorkspaceId()));
    }

    @Override
    public List<Datasource> queryAll() {
        return datasourceMapper.selectList(
                new QueryWrapper<Datasource>().eq("workspace_id", getCurrentWorkspaceId()));
    }

    @Override
    public List<Datasource> selectByIds(List<Long> ids) {
        return datasourceMapper.selectList(
                new QueryWrapper<Datasource>()
                        .in("id", ids)
                        .eq("workspace_id", getCurrentWorkspaceId()));
    }

    @Override
    public List<Datasource> selectDatasourceByUserId(int userId) {
        return datasourceMapper.selectList(
                new QueryWrapper<Datasource>()
                        .eq("create_user_id", userId)
                        .eq("workspace_id", getCurrentWorkspaceId()));
    }

    @Override
    public List<String> getDatasourceNames(Long workspaceId, String searchName) {
        QueryWrapper<Datasource> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("workspace_id", workspaceId);
        if (searchName != null) {
            queryWrapper.like("datasource_name", "%" + searchName + "%");
        }
        return datasourceMapper.selectList(queryWrapper).stream()
                .map(Datasource::getDatasourceName)
                .collect(Collectors.toList());
    }
}
