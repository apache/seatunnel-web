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

import org.apache.seatunnel.app.dal.dao.IWorkspaceDao;
import org.apache.seatunnel.app.dal.entity.Workspace;
import org.apache.seatunnel.app.dal.mapper.WorkspaceMapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import javax.annotation.Resource;

import java.util.List;

@Repository
public class WorkspaceDaoImpl implements IWorkspaceDao {
    @Resource private WorkspaceMapper workspaceMapper;

    @Override
    public void insertWorkspace(Workspace workspace) {
        workspaceMapper.insert(workspace);
    }

    @Override
    public Workspace selectWorkspaceById(Long id) {
        return workspaceMapper.selectById(id);
    }

    public Workspace selectWorkspaceByName(String workspaceName) {
        return workspaceMapper.selectOne(
                new QueryWrapper<Workspace>().eq("workspace_name", workspaceName));
    }

    @Override
    public boolean updateWorkspaceById(Workspace workspace) {
        return workspaceMapper.updateById(workspace) > 0;
    }

    @Override
    public boolean deleteWorkspaceById(Long id) {
        return workspaceMapper.deleteById(id) > 0;
    }

    @Override
    public List<Workspace> selectAllWorkspaces() {
        return workspaceMapper.selectList(new QueryWrapper<>());
    }

    @Override
    public List<String> getWorkspaceNames(String searchName) {
        return workspaceMapper.getWorkspaceNames(searchName);
    }
}
