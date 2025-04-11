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
import org.apache.seatunnel.app.controller.JobDefinitionControllerWrapper;
import org.apache.seatunnel.app.controller.ResourceNameProviderControllerWrapper;
import org.apache.seatunnel.app.controller.SeatunnelDatasourceControllerWrapper;
import org.apache.seatunnel.app.controller.UserControllerWrapper;
import org.apache.seatunnel.app.controller.WorkspaceControllerWrapper;
import org.apache.seatunnel.app.domain.request.user.UserLoginReq;
import org.apache.seatunnel.app.domain.response.user.AddUserRes;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceNameProviderControllerTest {
    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static ResourceNameProviderControllerWrapper resourceWrapper;
    private static WorkspaceControllerWrapper workspaceControllerWrapper;
    private static UserControllerWrapper userControllerWrapper;
    private static SeatunnelDatasourceControllerWrapper datasourceControllerWrapper;
    private static JobDefinitionControllerWrapper jobDefinitionControllerWrapper;
    private static final String uniqueId = "_" + System.currentTimeMillis();

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        resourceWrapper = new ResourceNameProviderControllerWrapper();
        workspaceControllerWrapper = new WorkspaceControllerWrapper();
        userControllerWrapper = new UserControllerWrapper();
        datasourceControllerWrapper = new SeatunnelDatasourceControllerWrapper();
        jobDefinitionControllerWrapper = new JobDefinitionControllerWrapper();
    }

    @Test
    public void testGetWorkspaces() {
        String prefix = System.currentTimeMillis() + "_";
        String postfix = "_" + System.currentTimeMillis();
        String workspaceName = prefix + "workspace" + uniqueId + "_name" + postfix;
        createWorkspace(workspaceName);

        // search with infix
        Result<List<String>> result = resourceWrapper.getWorkspaces(uniqueId);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().contains(workspaceName));
        // search with prefix
        result = resourceWrapper.getWorkspaces(prefix);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().contains(workspaceName));

        // search with postfix
        result = resourceWrapper.getWorkspaces(postfix);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().contains(workspaceName));

        // search with null
        result = resourceWrapper.getWorkspaces(null);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().contains(workspaceName));
    }

    @Test
    public void testGetUserNames() {
        String prefix = System.currentTimeMillis() + "_";
        String postfix = "_" + System.currentTimeMillis();
        String userName = prefix + "user" + uniqueId + "_name" + postfix;
        String password = "password";
        String workspaceName = "workspace_user" + uniqueId;
        createWorkspaceAndUser(workspaceName, userName, password);

        // search with infix
        Result<List<String>> result = resourceWrapper.getUserNames(uniqueId);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().contains(userName));

        // search with prefix
        result = resourceWrapper.getUserNames(prefix);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().contains(userName));

        // search with postfix
        result = resourceWrapper.getUserNames(postfix);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().contains(userName));

        // search with null
        result = resourceWrapper.getUserNames(null);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().contains(userName));
    }

    @Test
    public void testGetDatasource() {
        String prefix = System.currentTimeMillis() + "_";
        String postfix = "_" + System.currentTimeMillis();
        String user1 = "user_get_datasource_11" + uniqueId;
        String user2 = "user_get_datasource_12" + uniqueId;
        String pass = "somePassword";
        String workspace1 = "workspace_get_datasource_11" + uniqueId;
        String workspace2 = "workspace_get_datasource_12" + uniqueId;

        createWorkspaceAndUser(workspace1, user1, pass);
        createWorkspaceAndUser(workspace2, user2, pass);

        userControllerWrapper.loginAndSetCurrentUser(new UserLoginReq(user1, pass, workspace1));
        String datasourceName1 = prefix + "ds-1" + uniqueId + "_get" + postfix;
        datasourceControllerWrapper.createFakeSourceDatasource(datasourceName1);

        // search with infix
        Result<List<String>> result = resourceWrapper.getDatasources(workspace1, uniqueId);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().contains(datasourceName1));

        // search with prefix
        result = resourceWrapper.getDatasources(workspace1, prefix);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().contains(datasourceName1));

        // search with postfix
        result = resourceWrapper.getDatasources(workspace1, postfix);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().contains(datasourceName1));

        // search without searchName
        result = resourceWrapper.getDatasources(workspace1, null);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().contains(datasourceName1));

        // search without workspaceName
        result = resourceWrapper.getDatasources(null, null);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().contains(datasourceName1));

        // logout and login with another user with another namespace
        userControllerWrapper.logout();
        String datasourceName2 = prefix + "ds-2" + uniqueId + "_get" + postfix;
        userControllerWrapper.loginAndSetCurrentUser(new UserLoginReq(user2, pass, workspace2));
        datasourceControllerWrapper.createFakeSourceDatasource(datasourceName2);

        // Admin user will automatically log in when API is invoked
        userControllerWrapper.logout();
        result = resourceWrapper.getDatasources(workspace1, uniqueId);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().contains(datasourceName1));

        result = resourceWrapper.getDatasources(workspace2, uniqueId);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().contains(datasourceName2));

        // search without workspaceName
        result = resourceWrapper.getDatasources(null, uniqueId);
        assertTrue(result.isSuccess());
        // if workspace is not passed, default workspace will be used and have not added any
        // datasource to default workspace
        assertEquals(0, result.getData().size());

        result = resourceWrapper.getDatasources("non_existing_workspace", uniqueId);
        assertFalse(result.isSuccess());
        assertEquals(SeatunnelErrorEnum.RESOURCE_NOT_FOUND.getCode(), result.getCode());
        assertEquals("Workspace with name non_existing_workspace not found.", result.getMsg());
    }

    @Test
    public void testGetJobDefinitions() {
        String prefix = System.currentTimeMillis() + "_";
        String postfix = "_" + System.currentTimeMillis();
        String user1 = "user_get_job_11" + uniqueId;
        String user2 = "user_get_job_12" + uniqueId;
        String pass = "somePassword";
        String workspace1 = "workspace_get_job_11" + uniqueId;
        String workspace2 = "workspace_get_job_12" + uniqueId;

        createWorkspaceAndUser(workspace1, user1, pass);
        createWorkspaceAndUser(workspace2, user2, pass);

        userControllerWrapper.loginAndSetCurrentUser(new UserLoginReq(user1, pass, workspace1));
        String jobName1 = prefix + "job1" + uniqueId + "_get" + postfix;
        jobDefinitionControllerWrapper.createJobDefinition(jobName1);

        // search with infix
        Result<List<String>> result = resourceWrapper.getJobDefinitions(workspace1, uniqueId);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().contains(jobName1));

        // search with prefix
        result = resourceWrapper.getJobDefinitions(workspace1, prefix);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().contains(jobName1));

        // search with postfix
        result = resourceWrapper.getJobDefinitions(workspace1, postfix);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().contains(jobName1));

        // search without searchName
        result = resourceWrapper.getJobDefinitions(workspace1, null);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().contains(jobName1));

        // search without workspaceName
        result = resourceWrapper.getJobDefinitions(null, null);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().contains(jobName1));

        // logout and login with another user with another namespace
        userControllerWrapper.logout();
        String jobName2 = prefix + "job2" + uniqueId + "_get" + postfix;
        userControllerWrapper.loginAndSetCurrentUser(new UserLoginReq(user2, pass, workspace2));
        jobDefinitionControllerWrapper.createJobDefinition(jobName2);

        // Admin user will automatically log in when API is invoked
        userControllerWrapper.logout();
        result = resourceWrapper.getJobDefinitions(workspace1, uniqueId);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().contains(jobName1));

        result = resourceWrapper.getJobDefinitions(workspace2, uniqueId);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().contains(jobName2));

        // search without workspaceName
        result = resourceWrapper.getJobDefinitions(null, uniqueId);
        assertTrue(result.isSuccess());
        // if workspace is not passed, default workspace will be used and have not added any job to
        // default workspace
        assertEquals(0, result.getData().size());

        result = resourceWrapper.getJobDefinitions("non_existing_workspace", uniqueId);
        assertFalse(result.isSuccess());
        assertEquals(SeatunnelErrorEnum.RESOURCE_NOT_FOUND.getCode(), result.getCode());
        assertEquals("Workspace with name non_existing_workspace not found.", result.getMsg());
    }

    private void createWorkspace(String workspaceName) {
        Result<Long> result = workspaceControllerWrapper.createWorkspace(workspaceName);
        assertTrue(result.isSuccess());
        assertTrue(result.getData() > 0);
    }

    private void createWorkspaceAndUser(String workspaceName, String username, String password) {
        workspaceControllerWrapper.createWorkspaceAndVerify(workspaceName);
        Result<AddUserRes> result = userControllerWrapper.addUser(username, password);
        assertTrue(result.isSuccess());
    }

    @AfterEach
    public void cleanup() {
        userControllerWrapper.logout();
    }

    @AfterAll
    public static void tearDown() {
        seaTunnelWebCluster.stop();
    }
}
