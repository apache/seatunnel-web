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

package org.apache.seatunnel.app.service.impl;

import org.apache.seatunnel.app.dal.dao.IWorkspaceDao;
import org.apache.seatunnel.app.dal.entity.Workspace;
import org.apache.seatunnel.app.domain.request.workspace.WorkspaceReq;
import org.apache.seatunnel.app.service.WorkspaceService;
import org.apache.seatunnel.app.utils.ServletUtils;
import org.apache.seatunnel.server.common.CodeGenerateUtils;
import org.apache.seatunnel.server.common.ParamValidationException;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class WorkspaceServiceImpl extends SeatunnelBaseServiceImpl implements WorkspaceService {
    @Autowired private IWorkspaceDao workspaceDao;

    @Override
    public Long createWorkspace(WorkspaceReq workspaceReq) {
        validateWorkspaceParam(workspaceReq);
        Workspace workspaceByName =
                workspaceDao.selectWorkspaceByName(workspaceReq.getWorkspaceName());
        if (workspaceByName != null) {
            throw new SeatunnelException(
                    SeatunnelErrorEnum.RESOURCE_ALREADY_EXISTS,
                    "Workspace with name " + workspaceReq.getWorkspaceName() + " already exists.");
        }
        Workspace workspace = new Workspace();
        long id = CodeGenerateUtils.getInstance().genCode();
        workspace.setId(id);
        workspace.setWorkspaceName(workspaceReq.getWorkspaceName());
        workspace.setDescription(workspaceReq.getDescription());
        workspaceDao.insertWorkspace(workspace);
        return id;
    }

    private void validateWorkspaceParam(WorkspaceReq workspaceReq) {
        if (StringUtils.isEmpty(workspaceReq.getWorkspaceName())) {
            throw new ParamValidationException(
                    SeatunnelErrorEnum.PARAM_CAN_NOT_BE_NULL, "workspace");
        }
    }

    @Override
    public Workspace getWorkspace(Long id) {
        Workspace workspace = workspaceDao.selectWorkspaceById(id);
        if (null == workspace) {
            throw new SeatunnelException(
                    SeatunnelErrorEnum.RESOURCE_NOT_FOUND,
                    "Workspace with id " + id + " not found.");
        }
        return workspace;
    }

    @Override
    public Workspace getWorkspace(String workspaceName) {
        Workspace workspace = workspaceDao.selectWorkspaceByName(workspaceName);
        if (null == workspace) {
            throw new SeatunnelException(
                    SeatunnelErrorEnum.RESOURCE_NOT_FOUND,
                    "Workspace with name " + workspaceName + " not found.");
        }
        return workspace;
    }

    @Override
    public boolean updateWorkspace(Long id, WorkspaceReq workspaceReq) {
        Workspace workspace = workspaceDao.selectWorkspaceById(id);
        if (workspace == null) {
            throw new SeatunnelException(
                    SeatunnelErrorEnum.RESOURCE_NOT_FOUND,
                    "Workspace with id " + id + " not found.");
        }
        validateWorkspaceParam(workspaceReq);

        // Check if the workspace name is being changed and if it already exists in the database
        if (!workspace.getWorkspaceName().equals(workspaceReq.getWorkspaceName())
                && workspaceDao.selectWorkspaceByName(workspaceReq.getWorkspaceName()) != null) {
            throw new SeatunnelException(
                    SeatunnelErrorEnum.RESOURCE_ALREADY_EXISTS,
                    "Workspace with name " + workspaceReq.getWorkspaceName() + " already exists.");
        }

        workspace.setWorkspaceName(workspaceReq.getWorkspaceName());
        workspace.setDescription(workspaceReq.getDescription());
        workspace.setUpdateTime(new Date());
        return workspaceDao.updateWorkspaceById(workspace);
    }

    @Override
    public boolean deleteWorkspace(Long id) {
        Workspace workspace = workspaceDao.selectWorkspaceById(id);
        if (null != workspace) {
            return workspaceDao.deleteWorkspaceById(id);
        }
        return false;
    }

    @Override
    public List<Workspace> getAllWorkspaces() {
        return workspaceDao.selectAllWorkspaces();
    }

    @Override
    public Workspace getDefaultWorkspace() {
        return getWorkspace("default");
    }

    @Override
    public Long getWorkspaceIdOrDefault(Long workspaceId) {
        if (workspaceId == null || workspaceId == 0 || workspaceId == 1) {
            return getDefaultWorkspace().getId();
        } else {
            // Check if the workspace exists
            Workspace workspaceById = getWorkspace(workspaceId);
            if (workspaceById == null) {
                throw new SeatunnelException(
                        SeatunnelErrorEnum.RESOURCE_NOT_FOUND,
                        "Workspace with id " + workspaceId + " not found.");
            }
            return workspaceById.getId();
        }
    }

    public Long getWorkspaceIdOrCurrent(String workspaceName) {
        if (StringUtils.isEmpty(workspaceName)) {
            // get names from current workspace
            return ServletUtils.getCurrentWorkspaceId();
        } else {
            return getWorkspace(workspaceName).getId();
        }
    }
}
