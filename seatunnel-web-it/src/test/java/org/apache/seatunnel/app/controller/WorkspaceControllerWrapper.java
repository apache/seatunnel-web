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

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.common.SeatunnelWebTestingBase;
import org.apache.seatunnel.app.dal.entity.Workspace;
import org.apache.seatunnel.app.domain.request.workspace.WorkspaceReq;
import org.apache.seatunnel.app.utils.JSONTestUtils;
import org.apache.seatunnel.common.utils.JsonUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkspaceControllerWrapper extends SeatunnelWebTestingBase {

    public Result<List<Workspace>> getAllWorkspaces() {
        String response = sendRequest(url("workspace/list"));
        return JSONTestUtils.parseObject(response, new TypeReference<Result<List<Workspace>>>() {});
    }

    public Result<Workspace> getWorkspacesById(Long workspaceId) {
        String response = sendRequest(url("workspace/list/" + workspaceId));
        return JSONTestUtils.parseObject(response, new TypeReference<Result<Workspace>>() {});
    }

    public Result<Long> createWorkspace(String workspaceName) {
        WorkspaceReq workspaceReq = new WorkspaceReq(workspaceName, workspaceName + " description");
        String requestBody = JsonUtils.toJsonString(workspaceReq);
        String response = sendRequest(url("workspace/create"), requestBody, "POST");
        return JSONTestUtils.parseObject(response, new TypeReference<Result<Long>>() {});
    }

    public void createWorkspaceAndVerify(String workspaceName) {
        Result<Long> workspaceCreationResult = createWorkspace(workspaceName);
        assertTrue(workspaceCreationResult.isSuccess());
        assertTrue(workspaceCreationResult.getData() > 0);
    }

    public Result<Boolean> updateWorkspace(Long workspaceId, WorkspaceReq workspaceReq) {
        String requestBody = JsonUtils.toJsonString(workspaceReq);
        String response = sendRequest(url("workspace/update/" + workspaceId), requestBody, "PUT");
        return JSONTestUtils.parseObject(response, new TypeReference<Result<Boolean>>() {});
    }

    public Result<Boolean> deleteWorkspace(Long workspaceId) {
        String response = sendRequest(url("workspace/delete/" + workspaceId), null, "DELETE");
        return JSONTestUtils.parseObject(response, new TypeReference<Result<Boolean>>() {});
    }
}
