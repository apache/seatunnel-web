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
import org.apache.seatunnel.app.controller.JobExecutorControllerWrapper;
import org.apache.seatunnel.app.controller.SeatunnelDatasourceControllerWrapper;
import org.apache.seatunnel.app.controller.TaskInstanceControllerWrapper;
import org.apache.seatunnel.app.controller.UserControllerWrapper;
import org.apache.seatunnel.app.controller.WorkspaceControllerWrapper;
import org.apache.seatunnel.app.domain.dto.job.SeaTunnelJobInstanceDto;
import org.apache.seatunnel.app.domain.request.connector.BusinessMode;
import org.apache.seatunnel.app.domain.request.datasource.DatasourceReq;
import org.apache.seatunnel.app.domain.request.job.JobReq;
import org.apache.seatunnel.app.domain.request.user.AddUserReq;
import org.apache.seatunnel.app.domain.request.user.UserLoginReq;
import org.apache.seatunnel.app.domain.response.user.AddUserRes;
import org.apache.seatunnel.app.domain.response.user.UserSimpleInfoRes;
import org.apache.seatunnel.app.utils.JobTestingUtils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkspaceTest {
    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static final String uniqueId = "_" + System.currentTimeMillis();
    private static SeatunnelDatasourceControllerWrapper datasourceControllerWrapper;
    private static WorkspaceControllerWrapper workspaceControllerWrapper;
    private static UserControllerWrapper userControllerWrapper;
    private static JobDefinitionControllerWrapper jobDefinitionControllerWrapper;
    private static JobExecutorControllerWrapper jobExecutorControllerWrapper;
    private static TaskInstanceControllerWrapper taskInstanceControllerWrapper;

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        datasourceControllerWrapper = new SeatunnelDatasourceControllerWrapper();
        workspaceControllerWrapper = new WorkspaceControllerWrapper();
        userControllerWrapper = new UserControllerWrapper();
        jobDefinitionControllerWrapper = new JobDefinitionControllerWrapper();
        jobExecutorControllerWrapper = new JobExecutorControllerWrapper();
        taskInstanceControllerWrapper = new TaskInstanceControllerWrapper();
    }

    @Test
    public void testDatasourceIsolation_WithinWorkspace() {
        String user1 = "user_namespace_11" + uniqueId;
        String user2 = "user_namespace_12" + uniqueId;
        String pass = "somePassword";
        String workspace1 = "workspace_namespace_11" + uniqueId;
        String workspace2 = "workspace_namespace_12" + uniqueId;

        createWorkspaceAndUser(workspace1, user1, pass);
        createWorkspaceAndUser(workspace2, user2, pass);

        login(new UserLoginReq(user1, pass, workspace1));
        String datasourceName = "datasource_namespace_11" + uniqueId;
        datasourceControllerWrapper.createFakeSourceDatasource(datasourceName);

        // logout and login with another user with another namespace, should be able to create
        // datasource with same name
        userControllerWrapper.logout();
        login(new UserLoginReq(user2, pass, workspace2));
        DatasourceReq req = datasourceControllerWrapper.getFakeSourceDatasourceReq(datasourceName);
        Result<String> dataSourceCreationResult = datasourceControllerWrapper.createDatasource(req);
        assertTrue(dataSourceCreationResult.isSuccess());

        // if create datasource with same name in same workspace, should fail
        DatasourceReq req2 = datasourceControllerWrapper.getFakeSourceDatasourceReq(datasourceName);
        Result<String> dataSourceCreationResult2 =
                datasourceControllerWrapper.createDatasource(req2);
        assertFalse(dataSourceCreationResult2.isSuccess());
    }

    @Test
    public void testJobDefinitionIsolation_WithinWorkspace() {
        String user1 = "user_workspace_21" + uniqueId;
        String user2 = "user_workspace_22" + uniqueId;
        String pass = "somePassword";
        String workspace1 = "workspace_21" + uniqueId;
        String workspace2 = "workspace_22" + uniqueId;

        createWorkspaceAndUser(workspace1, user1, pass);
        createWorkspaceAndUser(workspace2, user2, pass);

        login(new UserLoginReq(user1, pass, workspace1));
        String jobName = "job_definition_namespace_21" + uniqueId;
        JobReq jobReq = createJobReq(jobName);
        Result<Long> jobDefinition = jobDefinitionControllerWrapper.createJobDefinition(jobReq);
        assertTrue(jobDefinition.isSuccess());

        // logout and login with another user with another workspace, should be able to create
        // job definition with same name
        userControllerWrapper.logout();
        login(new UserLoginReq(user2, pass, workspace2));
        jobDefinition = jobDefinitionControllerWrapper.createJobDefinition(jobReq);
        assertTrue(jobDefinition.isSuccess(), jobDefinition.getMsg());

        // if create job definition with same name in same workspace, should fail
        jobDefinition = jobDefinitionControllerWrapper.createJobDefinition(jobReq);
        assertFalse(jobDefinition.isSuccess());
    }

    @Test
    public void testJobExecutionIsolation_WithinWorkspace() {
        String user1 = "user_workspace_31" + uniqueId;
        String user2 = "user_workspace_32" + uniqueId;
        String pass = "somePassword";
        String workspace1 = "workspace_31" + uniqueId;
        String workspace2 = "workspace_32" + uniqueId;

        createWorkspaceAndUser(workspace1, user1, pass);
        createWorkspaceAndUser(workspace2, user2, pass);

        login(new UserLoginReq(user1, pass, workspace1));
        String jobName = "execJob_namespace_31" + uniqueId;
        long jobVersionId_1 = JobTestingUtils.createJob(jobName);
        Result<Long> executionResult = jobExecutorControllerWrapper.jobExecutor(jobVersionId_1);
        assertTrue(executionResult.isSuccess());
        JobTestingUtils.waitForJobCompletion(executionResult.getData());
        List<SeaTunnelJobInstanceDto> taskInstanceList =
                taskInstanceControllerWrapper.getTaskInstanceList(jobName);
        assertEquals(1, taskInstanceList.size());

        // logout and login with another user with another workspace, should be able to create
        // job execution with same name
        userControllerWrapper.logout();
        login(new UserLoginReq(user2, pass, workspace2));
        long jobVersionId_2 = JobTestingUtils.createJob(jobName);
        executionResult = jobExecutorControllerWrapper.jobExecutor(jobVersionId_2);
        assertTrue(executionResult.isSuccess());
        JobTestingUtils.waitForJobCompletion(executionResult.getData());

        taskInstanceList = taskInstanceControllerWrapper.getTaskInstanceList(jobName);
        // should be 1 because second time we have run in separate name space
        assertEquals(1, taskInstanceList.size());
    }

    private void createWorkspaceAndUser(String workspaceName, String username, String password) {
        workspaceControllerWrapper.createWorkspaceAndVerify(workspaceName);
        Result<AddUserRes> result =
                userControllerWrapper.addUser(getAddUserReq(username, password));
        assertTrue(result.isSuccess());
    }

    private JobReq createJobReq(String jobName) {
        JobReq jobReq = new JobReq();
        jobReq.setName(jobName);
        jobReq.setDescription(jobName + " description");
        jobReq.setJobType(BusinessMode.DATA_INTEGRATION);
        return jobReq;
    }

    private static void login(UserLoginReq userLoginReq) {
        Result<UserSimpleInfoRes> login = userControllerWrapper.login(userLoginReq, null, true);
        assertTrue(login.isSuccess(), login.getMsg());
    }

    private AddUserReq getAddUserReq(String user, String pass) {
        AddUserReq addUserReq = new AddUserReq();
        addUserReq.setUsername(user);
        addUserReq.setPassword(pass);
        addUserReq.setStatus((byte) 0);
        addUserReq.setType((byte) 0);
        return addUserReq;
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
