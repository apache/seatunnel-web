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

package org.apache.seatunnel.app.test;

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.common.SeaTunnelWebCluster;
import org.apache.seatunnel.app.controller.WorkspaceControllerWrapper;
import org.apache.seatunnel.app.dal.entity.Workspace;
import org.apache.seatunnel.app.domain.request.workspace.WorkspaceReq;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkspaceControllerTest {
    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static WorkspaceControllerWrapper workspaceControllerWrapper;
    private static final String uniqueId = "_" + System.currentTimeMillis();

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        workspaceControllerWrapper = new WorkspaceControllerWrapper();
    }

    @Test
    public void testCreateWorkspace() {
        String workspaceName = "workspace_create" + uniqueId;
        Long workspaceId = createWorkspace(workspaceName);
        Workspace workspacesById = getWorkspace(workspaceId);
        assertEquals(workspaceName, workspacesById.getWorkspaceName());
        // test duplicated workspace name is not allowed
        Result<Long> result = workspaceControllerWrapper.createWorkspace(workspaceName);
        assertFalse(result.isSuccess());
        assertEquals(SeatunnelErrorEnum.RESOURCE_ALREADY_EXISTS.getCode(), result.getCode());

        // create workspace with empty workspace name
        result = workspaceControllerWrapper.createWorkspace("");
        assertFalse(result.isSuccess());
        assertEquals(SeatunnelErrorEnum.PARAM_CAN_NOT_BE_NULL.getCode(), result.getCode());
    }

    @Test
    public void testUpdateWorkspace() {
        String workspaceName = "workspace_update" + uniqueId;
        Long workspaceId = createWorkspace(workspaceName);

        // update workspace
        Workspace workspacesById = getWorkspace(workspaceId);
        String newName = workspacesById.getWorkspaceName() + "_newName";
        String newDescription = workspacesById.getDescription() + "_newDescription";
        WorkspaceReq workspaceReq = new WorkspaceReq(newName, newDescription);
        Result<Boolean> updateResult =
                workspaceControllerWrapper.updateWorkspace(workspaceId, workspaceReq);
        assertTrue(updateResult.isSuccess());
        assertTrue(updateResult.getData());

        workspacesById = getWorkspace(workspaceId);
        assertEquals(workspacesById.getWorkspaceName(), newName);
        assertEquals(workspacesById.getDescription(), newDescription);

        // update workspace with empty workspace name
        workspaceReq = new WorkspaceReq("", newDescription);
        updateResult = workspaceControllerWrapper.updateWorkspace(workspaceId, workspaceReq);
        assertFalse(updateResult.isSuccess());
        assertEquals(SeatunnelErrorEnum.PARAM_CAN_NOT_BE_NULL.getCode(), updateResult.getCode());

        // update workspace with duplicated workspace name
        String workspaceName2 = "workspace_update2" + uniqueId;
        createWorkspace(workspaceName2);

        workspaceReq = new WorkspaceReq(workspaceName2, newDescription);
        updateResult = workspaceControllerWrapper.updateWorkspace(workspaceId, workspaceReq);
        assertFalse(updateResult.isSuccess());
        assertEquals(SeatunnelErrorEnum.RESOURCE_ALREADY_EXISTS.getCode(), updateResult.getCode());
    }

    private static Long createWorkspace(String workspaceName) {
        Result<Long> result = workspaceControllerWrapper.createWorkspace(workspaceName);
        assertTrue(result.isSuccess());
        assertTrue(result.getData() > 0);
        return result.getData();
    }

    @Test
    public void testDeleteWorkspace() {
        String workspaceName = "workspace_delete" + uniqueId;
        Long workspaceId = createWorkspace(workspaceName);
        Result<Boolean> result = workspaceControllerWrapper.deleteWorkspace(workspaceId);
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
    }

    @Test
    public void testListWorkspaces() {
        String workspaceName = "workspace_list_1" + uniqueId;
        Long workspaceId1 = createWorkspace(workspaceName);
        String workspaceName2 = "workspace_list_2" + uniqueId;
        Long workspaceId2 = createWorkspace(workspaceName2);

        Result<Workspace> workspacesById =
                workspaceControllerWrapper.getWorkspacesById(workspaceId1);
        assertTrue(workspacesById.isSuccess());
        assertNotNull(workspacesById.getData());
        assertEquals(workspacesById.getData().getWorkspaceName(), workspaceName);

        Result<List<Workspace>> allWorkspaces = workspaceControllerWrapper.getAllWorkspaces();
        assertTrue(allWorkspaces.isSuccess());
        assertTrue(allWorkspaces.getData().size() > 2 || allWorkspaces.getData().size() == 2);

        List<Long> workspaceIds =
                allWorkspaces.getData().stream().map(Workspace::getId).collect(Collectors.toList());
        assertTrue(workspaceIds.contains(workspaceId1));
        assertTrue(workspaceIds.contains(workspaceId2));

        // get workspace which do not exist, assuming 123456789 does not exist
        workspacesById = workspaceControllerWrapper.getWorkspacesById(123456789L);
        assertFalse(workspacesById.isSuccess());
        assertEquals(SeatunnelErrorEnum.RESOURCE_NOT_FOUND.getCode(), workspacesById.getCode());
    }

    private Workspace getWorkspace(Long workspaceId) {
        Result<Workspace> workspacesById =
                workspaceControllerWrapper.getWorkspacesById(workspaceId);
        assertTrue(workspacesById.isSuccess());
        assertNotNull(workspacesById.getData());
        return workspacesById.getData();
    }

    @AfterAll
    public static void tearDown() {
        seaTunnelWebCluster.stop();
    }
}
