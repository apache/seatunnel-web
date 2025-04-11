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

import org.apache.seatunnel.app.common.AccessControllerTestingImp;
import org.apache.seatunnel.app.common.ResourcePermissionData;
import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.common.SeaTunnelWebCluster;
import org.apache.seatunnel.app.controller.JobConfigControllerWrapper;
import org.apache.seatunnel.app.controller.JobControllerWrapper;
import org.apache.seatunnel.app.controller.JobDefinitionControllerWrapper;
import org.apache.seatunnel.app.controller.JobExecutorControllerWrapper;
import org.apache.seatunnel.app.controller.SeatunnelDatasourceControllerWrapper;
import org.apache.seatunnel.app.controller.UserControllerWrapper;
import org.apache.seatunnel.app.controller.WorkspaceControllerWrapper;
import org.apache.seatunnel.app.dal.entity.Workspace;
import org.apache.seatunnel.app.domain.request.datasource.DatasourceReq;
import org.apache.seatunnel.app.domain.request.job.JobConfig;
import org.apache.seatunnel.app.domain.request.job.JobCreateReq;
import org.apache.seatunnel.app.domain.request.user.AddUserReq;
import org.apache.seatunnel.app.domain.request.user.UpdateUserReq;
import org.apache.seatunnel.app.domain.request.user.UserLoginReq;
import org.apache.seatunnel.app.domain.request.workspace.WorkspaceReq;
import org.apache.seatunnel.app.domain.response.PageInfo;
import org.apache.seatunnel.app.domain.response.datasource.DatasourceDetailRes;
import org.apache.seatunnel.app.domain.response.datasource.DatasourceRes;
import org.apache.seatunnel.app.domain.response.job.JobConfigRes;
import org.apache.seatunnel.app.domain.response.job.JobDefinitionRes;
import org.apache.seatunnel.app.domain.response.job.JobRes;
import org.apache.seatunnel.app.domain.response.user.AddUserRes;
import org.apache.seatunnel.app.domain.response.user.UserSimpleInfoRes;
import org.apache.seatunnel.app.utils.JobTestingUtils;
import org.apache.seatunnel.common.access.AccessType;
import org.apache.seatunnel.common.access.ResourceType;
import org.apache.seatunnel.common.constants.JobMode;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SeatunnelAccessControllerTest {
    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static WorkspaceControllerWrapper workspaceControllerWrapper;
    private static UserControllerWrapper userControllerWrapper;
    private static SeatunnelDatasourceControllerWrapper datasourceControllerWrapper;
    private static JobDefinitionControllerWrapper jobDefinitionControllerWrapper;
    private static JobConfigControllerWrapper jobConfigControllerWrapper;
    private static JobControllerWrapper jobControllerWrapper;
    private static JobExecutorControllerWrapper jobExecutorControllerWrapper;
    private static final String uniqueId = "_" + System.currentTimeMillis();

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        workspaceControllerWrapper = new WorkspaceControllerWrapper();
        userControllerWrapper = new UserControllerWrapper();
        datasourceControllerWrapper = new SeatunnelDatasourceControllerWrapper();
        jobDefinitionControllerWrapper = new JobDefinitionControllerWrapper();
        jobConfigControllerWrapper = new JobConfigControllerWrapper();
        jobControllerWrapper = new JobControllerWrapper();
        jobExecutorControllerWrapper = new JobExecutorControllerWrapper();
        AccessControllerTestingImp.enableAccessController();
    }

    @Test
    public void testWorkspaceAccessPermission() {
        String user1 = "admin";
        String workspaceName = "workspace_access_workspace" + uniqueId;

        Result<Long> createWorkspaceResult =
                workspaceControllerWrapper.createWorkspace(workspaceName);
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), createWorkspaceResult.getCode());

        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(
                        null,
                        workspaceName,
                        ResourceType.WORKSPACE,
                        Collections.singletonList(AccessType.CREATE)));
        createWorkspaceResult = workspaceControllerWrapper.createWorkspace(workspaceName);
        assertTrue(createWorkspaceResult.isSuccess());

        // Handle read operation
        AccessControllerTestingImp.clearPermission();
        Result<List<Workspace>> getWorkspaces = workspaceControllerWrapper.getAllWorkspaces();
        assertTrue(getWorkspaces.isSuccess());
        assertEquals(0, getWorkspaces.getData().size());

        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(
                        null,
                        workspaceName,
                        ResourceType.WORKSPACE,
                        Collections.singletonList(AccessType.READ)));

        getWorkspaces = workspaceControllerWrapper.getAllWorkspaces();
        assertTrue(getWorkspaces.isSuccess());
        assertEquals(1, getWorkspaces.getData().size());

        String anotherWorkspace = "another_workspace_access" + uniqueId;
        AccessControllerTestingImp.clearPermission();
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(
                        null,
                        anotherWorkspace,
                        ResourceType.WORKSPACE,
                        Collections.singletonList(AccessType.CREATE)));
        Result<Long> anotherCreateWorkspaceResult =
                workspaceControllerWrapper.createWorkspace(anotherWorkspace);
        assertTrue(anotherCreateWorkspaceResult.isSuccess());

        getWorkspaces = workspaceControllerWrapper.getAllWorkspaces();
        assertTrue(anotherCreateWorkspaceResult.isSuccess());
        assertEquals(0, getWorkspaces.getData().size());

        AccessControllerTestingImp.clearPermission();
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(
                        null,
                        workspaceName,
                        ResourceType.WORKSPACE,
                        Collections.singletonList(AccessType.READ)));
        AccessControllerTestingImp.addResourcePermission(
                user1,
                new ResourcePermissionData(
                        null,
                        anotherWorkspace,
                        ResourceType.WORKSPACE,
                        Collections.singletonList(AccessType.READ)));
        getWorkspaces = workspaceControllerWrapper.getAllWorkspaces();
        assertTrue(anotherCreateWorkspaceResult.isSuccess());
        assertEquals(2, getWorkspaces.getData().size());

        // Handle update operation
        AccessControllerTestingImp.clearPermission();
        WorkspaceReq updateWorkspaceReq =
                new WorkspaceReq(workspaceName + "_new", "new description");
        Result<Boolean> updateResult =
                workspaceControllerWrapper.updateWorkspace(
                        createWorkspaceResult.getData(), updateWorkspaceReq);
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), updateResult.getCode());

        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(
                        null,
                        workspaceName,
                        ResourceType.WORKSPACE,
                        Collections.singletonList(AccessType.UPDATE)));
        updateResult =
                workspaceControllerWrapper.updateWorkspace(
                        createWorkspaceResult.getData(), updateWorkspaceReq);
        assertTrue(updateResult.isSuccess(), updateResult.getMsg());

        // Handle delete operation
        AccessControllerTestingImp.clearPermission();
        Result<Boolean> deleteResult =
                workspaceControllerWrapper.deleteWorkspace(createWorkspaceResult.getData());
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), deleteResult.getCode());

        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(
                        null,
                        updateWorkspaceReq.getWorkspaceName(),
                        ResourceType.WORKSPACE,
                        Collections.singletonList(AccessType.DELETE)));
        deleteResult = workspaceControllerWrapper.deleteWorkspace(createWorkspaceResult.getData());
        assertTrue(deleteResult.isSuccess(), deleteResult.getMsg());
    }

    @Test
    public void testUserAccessPermission() {
        String user1 = "user_access_user_1" + uniqueId;
        String pass = "somePassword";
        String workspaceName = "workspace_access_user" + uniqueId;

        List<AccessType> accessTypes = new ArrayList<>();
        accessTypes.add(AccessType.CREATE);

        createWorkspaceAndUser(workspaceName, user1, pass);
        login(new UserLoginReq(user1, pass, workspaceName));

        // Handle create operation
        accessTypes.clear();
        String newUser = "new_user_access" + uniqueId;
        Result<AddUserRes> addUserResult =
                userControllerWrapper.addUser(getAddUserReq(newUser, pass));
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), addUserResult.getCode());
        accessTypes.add(AccessType.CREATE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(workspaceName, newUser, ResourceType.USER, accessTypes));
        // should be successful as user1 has access to create user
        addUserResult = userControllerWrapper.addUser(getAddUserReq(newUser, pass));
        assertTrue(addUserResult.isSuccess());

        // Handle read operation
        Result<PageInfo<UserSimpleInfoRes>> getUsers = userControllerWrapper.listUsers(newUser);
        assertTrue(getUsers.isSuccess());
        assertEquals(0, getUsers.getData().getData().size());

        accessTypes.clear();
        accessTypes.add(AccessType.READ);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(workspaceName, newUser, ResourceType.USER, accessTypes));
        getUsers = userControllerWrapper.listUsers(newUser);
        assertTrue(getUsers.isSuccess());
        assertEquals(1, getUsers.getData().getData().size());

        String anotherUser = "another_user_access" + uniqueId;
        accessTypes.clear();
        accessTypes.add(AccessType.CREATE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(
                        workspaceName, anotherUser, ResourceType.USER, accessTypes));
        Result<AddUserRes> anotherAddUserResult =
                userControllerWrapper.addUser(getAddUserReq(anotherUser, pass));
        assertTrue(anotherAddUserResult.isSuccess());

        Result<PageInfo<UserSimpleInfoRes>> listUsers = userControllerWrapper.listUsers();
        assertTrue(anotherAddUserResult.isSuccess());
        assertEquals(0, listUsers.getData().getData().size());

        accessTypes.clear();
        accessTypes.add(AccessType.READ);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(workspaceName, newUser, ResourceType.USER, accessTypes));
        AccessControllerTestingImp.addResourcePermission(
                user1,
                new ResourcePermissionData(
                        workspaceName, anotherUser, ResourceType.USER, accessTypes));
        listUsers = userControllerWrapper.listUsers();
        assertTrue(listUsers.isSuccess());
        assertEquals(2, listUsers.getData().getData().size());

        // Handle update operation
        UpdateUserReq updateUserReq = new UpdateUserReq();
        updateUserReq.setUsername(newUser);
        updateUserReq.setUserId(addUserResult.getData().getId());
        updateUserReq.setPassword("newPassword");
        updateUserReq.setStatus((byte) 0);
        updateUserReq.setType((byte) 0);
        Result<Void> updateResult =
                userControllerWrapper.updateUser(
                        String.valueOf(updateUserReq.getUserId()), updateUserReq);
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), updateResult.getCode());

        accessTypes.clear();
        accessTypes.add(AccessType.UPDATE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(workspaceName, newUser, ResourceType.USER, accessTypes));
        updateResult =
                userControllerWrapper.updateUser(
                        String.valueOf(updateUserReq.getUserId()), updateUserReq);
        assertTrue(updateResult.isSuccess(), updateResult.getMsg());

        // Handle disable operation
        AccessControllerTestingImp.clearPermission();
        Result<Void> disableResult =
                userControllerWrapper.disableUser(String.valueOf(updateUserReq.getUserId()));
        assertEquals(
                SeatunnelErrorEnum.ACCESS_DENIED.getCode(),
                disableResult.getCode(),
                disableResult.getMsg());

        accessTypes.clear();
        accessTypes.add(AccessType.UPDATE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(workspaceName, newUser, ResourceType.USER, accessTypes));
        disableResult =
                userControllerWrapper.disableUser(String.valueOf(updateUserReq.getUserId()));
        assertTrue(disableResult.isSuccess());

        // Handle enable operation
        AccessControllerTestingImp.clearPermission();
        Result<Void> enableResult =
                userControllerWrapper.enableUser(String.valueOf(updateUserReq.getUserId()));
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), enableResult.getCode());

        accessTypes.clear();
        accessTypes.add(AccessType.UPDATE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(workspaceName, newUser, ResourceType.USER, accessTypes));
        enableResult = userControllerWrapper.enableUser(String.valueOf(updateUserReq.getUserId()));
        assertTrue(enableResult.isSuccess());

        // Handle delete operation
        AccessControllerTestingImp.clearPermission();
        Result<Void> deleteResult =
                userControllerWrapper.deleteUser(String.valueOf(addUserResult.getData().getId()));
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), deleteResult.getCode());

        accessTypes.clear();
        accessTypes.add(AccessType.DELETE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(workspaceName, newUser, ResourceType.USER, accessTypes));
        deleteResult =
                userControllerWrapper.deleteUser(String.valueOf(addUserResult.getData().getId()));
        assertTrue(deleteResult.isSuccess());
    }

    @Test
    public void testDatasourceAccessPermission() {
        String user1 = "user_access_datasource_1" + uniqueId;
        String user2 = "user_access_datasource_2" + uniqueId;
        String pass = "somePassword";
        String workspaceName = "workspace_access_datasource" + uniqueId;

        // create workspaces and users using admin credentials
        createWorkspaceAndUser(workspaceName, user1, pass);
        createUserAndVerify(user2, pass);

        login(new UserLoginReq(user1, pass, workspaceName));

        // Handle create operation
        String datasourceName1 = "1_datasource_access" + uniqueId;
        datasourceControllerWrapper.createDatasourceExpectingFailure(datasourceName1);
        List<AccessType> accessTypes = new ArrayList<>();
        accessTypes.add(AccessType.CREATE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(
                        workspaceName, datasourceName1, ResourceType.DATASOURCE, accessTypes));
        // should be successful as user1 has access to create datasource
        String datasourceId1 =
                datasourceControllerWrapper.createFakeSourceDatasource(datasourceName1);

        Result<DatasourceDetailRes> getDataSource =
                datasourceControllerWrapper.getDatasource(datasourceId1);
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), getDataSource.getCode());

        // Handle read operation
        accessTypes.clear();
        accessTypes.add(AccessType.READ);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(
                        workspaceName, datasourceName1, ResourceType.DATASOURCE, accessTypes));
        getDataSource = datasourceControllerWrapper.getDatasource(datasourceId1);
        assertTrue(getDataSource.isSuccess());

        // Handle update operation
        DatasourceReq req = new DatasourceReq();
        req.setDescription(getDataSource.getData().getDescription() + " new description");
        Result<Boolean> updateResult =
                datasourceControllerWrapper.updateDatasource(datasourceId1, req);
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), updateResult.getCode());

        accessTypes.clear();
        accessTypes.add(AccessType.UPDATE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(
                        workspaceName, datasourceName1, ResourceType.DATASOURCE, accessTypes));
        updateResult = datasourceControllerWrapper.updateDatasource(datasourceId1, req);
        assertTrue(updateResult.isSuccess());

        // Handle delete operation
        Result<Boolean> deleteResult = datasourceControllerWrapper.deleteDatasource(datasourceId1);
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), deleteResult.getCode());

        accessTypes.clear();
        accessTypes.add(AccessType.DELETE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(
                        workspaceName, datasourceName1, ResourceType.DATASOURCE, accessTypes));
        deleteResult = datasourceControllerWrapper.deleteDatasource(datasourceId1);
        assertTrue(deleteResult.isSuccess());

        // create again to use in list datasource
        accessTypes.clear();
        accessTypes.add(AccessType.CREATE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(
                        workspaceName, datasourceName1, ResourceType.DATASOURCE, accessTypes));
        datasourceControllerWrapper.createFakeSourceDatasource(datasourceName1);

        // logout and login with another user
        userControllerWrapper.logout();
        login(new UserLoginReq(user2, pass, workspaceName));
        // Handle list operation
        String datasourceName2 = "2_datasource_access" + uniqueId;
        accessTypes.clear();
        accessTypes.add(AccessType.CREATE);
        AccessControllerTestingImp.resetResourcePermission(
                user2,
                new ResourcePermissionData(
                        workspaceName, datasourceName2, ResourceType.DATASOURCE, accessTypes));
        String datasourceId2 =
                datasourceControllerWrapper.createFakeSourceDatasource(datasourceName2);
        Result<PageInfo<DatasourceRes>> datasourceList =
                datasourceControllerWrapper.getDatasourceList(
                        "datasource_access" + uniqueId, "FakeSource", 1, 10);
        assertTrue(datasourceList.isSuccess());
        assertEquals(0, datasourceList.getData().getData().size());

        accessTypes.clear();
        accessTypes.add(AccessType.READ);
        AccessControllerTestingImp.resetResourcePermission(
                user2,
                new ResourcePermissionData(
                        workspaceName, datasourceName2, ResourceType.DATASOURCE, accessTypes));
        datasourceList =
                datasourceControllerWrapper.getDatasourceList(
                        "datasource_access" + uniqueId, "FakeSource", 1, 10);
        assertTrue(datasourceList.isSuccess());
        assertEquals(1, datasourceList.getData().getData().size());
        assertEquals(datasourceId2, datasourceList.getData().getData().get(0).getId());

        // Give permission to user2 on datasource created by user1
        AccessControllerTestingImp.addResourcePermission(
                user2,
                new ResourcePermissionData(
                        workspaceName, datasourceName1, ResourceType.DATASOURCE, accessTypes));
        datasourceList =
                datasourceControllerWrapper.getDatasourceList(
                        "datasource_access" + uniqueId, "FakeSource", 1, 10);
        assertTrue(datasourceList.isSuccess());
        assertEquals(2, datasourceList.getData().getData().size());
    }

    @Test
    public void testJobAccessPermission() {
        String user1 = "user_access_job_1" + uniqueId;
        String user2 = "user_access_job_2" + uniqueId;
        String pass = "somePassword";
        String workspaceName = "workspace_access_job" + uniqueId;

        // create workspaces and users using admin credentials
        createWorkspaceAndUser(workspaceName, user1, pass);
        createUserAndVerify(user2, pass);

        login(new UserLoginReq(user1, pass, workspaceName));

        // Handle create operation
        String jobName1 = "1_job_access" + uniqueId;
        jobDefinitionControllerWrapper.createJobExpectingFailure(jobName1);
        List<AccessType> accessTypes = new ArrayList<>();
        accessTypes.add(AccessType.CREATE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(workspaceName, jobName1, ResourceType.JOB, accessTypes));
        // should be successful as user1 has access to create job
        Long jobId = jobDefinitionControllerWrapper.createJobDefinition(jobName1);

        // Handle read operation
        Result<JobDefinitionRes> getJob =
                jobDefinitionControllerWrapper.getJobDefinitionById(jobId);
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), getJob.getCode());

        Result<JobConfigRes> getJobConfig = jobConfigControllerWrapper.getJobConfig(jobId);
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), getJobConfig.getCode());

        accessTypes.clear();
        accessTypes.add(AccessType.READ);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(workspaceName, jobName1, ResourceType.JOB, accessTypes));
        getJob = jobDefinitionControllerWrapper.getJobDefinitionById(jobId);
        assertTrue(getJob.isSuccess());

        getJobConfig = jobConfigControllerWrapper.getJobConfig(jobId);
        assertTrue(getJobConfig.isSuccess());

        // Handle update operation
        AccessControllerTestingImp.clearPermission();
        JobConfig jobConfig = jobConfigControllerWrapper.populateJobConfigObject(jobName1);
        Result<Void> updateResult = jobConfigControllerWrapper.updateJobConfig(jobId, jobConfig);
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), updateResult.getCode());

        accessTypes.clear();
        accessTypes.add(AccessType.UPDATE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(workspaceName, jobName1, ResourceType.JOB, accessTypes));
        updateResult = jobConfigControllerWrapper.updateJobConfig(jobId, jobConfig);
        assertTrue(updateResult.isSuccess());

        // Handle delete operation
        Result<Void> deleteResult = jobDefinitionControllerWrapper.deleteJobDefinition(jobId);
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), deleteResult.getCode());

        accessTypes.clear();
        accessTypes.add(AccessType.DELETE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(workspaceName, jobName1, ResourceType.JOB, accessTypes));
        deleteResult = jobDefinitionControllerWrapper.deleteJobDefinition(jobId);
        assertTrue(deleteResult.isSuccess());

        // create again to use in list job
        accessTypes.clear();
        accessTypes.add(AccessType.CREATE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(workspaceName, jobName1, ResourceType.JOB, accessTypes));
        jobDefinitionControllerWrapper.createJobDefinition(jobName1);

        // logout and login with another user
        userControllerWrapper.logout();
        login(new UserLoginReq(user2, pass, workspaceName));
        // Handle list operation
        String jobName2 = "2_job_access" + uniqueId;
        accessTypes.clear();
        accessTypes.add(AccessType.CREATE);
        AccessControllerTestingImp.resetResourcePermission(
                user2,
                new ResourcePermissionData(workspaceName, jobName2, ResourceType.JOB, accessTypes));
        Long jobId2 = jobDefinitionControllerWrapper.createJobDefinition(jobName2);
        Result<PageInfo<JobDefinitionRes>> jobList =
                jobDefinitionControllerWrapper.getJobDefinition(
                        "job_access" + uniqueId, 1, 10, JobMode.BATCH);
        assertTrue(jobList.isSuccess());
        assertEquals(0, jobList.getData().getData().size());

        accessTypes.clear();
        accessTypes.add(AccessType.READ);
        AccessControllerTestingImp.resetResourcePermission(
                user2,
                new ResourcePermissionData(workspaceName, jobName2, ResourceType.JOB, accessTypes));
        jobList =
                jobDefinitionControllerWrapper.getJobDefinition(
                        "job_access" + uniqueId, 1, 10, JobMode.BATCH);
        assertTrue(jobList.isSuccess());
        assertEquals(1, jobList.getData().getData().size());
        assertEquals(jobId2, jobList.getData().getData().get(0).getId());

        // Give permission to user2 on job created by user1
        AccessControllerTestingImp.addResourcePermission(
                user2,
                new ResourcePermissionData(workspaceName, jobName1, ResourceType.JOB, accessTypes));
        jobList =
                jobDefinitionControllerWrapper.getJobDefinition(
                        "job_access" + uniqueId, 1, 10, JobMode.BATCH);
        assertTrue(jobList.isSuccess());
        assertEquals(2, jobList.getData().getData().size());
    }

    @Test
    public void testJobExecutionAccessPermission() {
        String userName = "jobExec_user_access" + uniqueId;
        String pass = "somePassword";
        String workspaceName = "jobExec_workspace_access" + uniqueId;

        // create workspaces and users using admin credentials
        createWorkspaceAndUser(workspaceName, userName, pass);

        login(new UserLoginReq(userName, pass, workspaceName));

        String jobName = "execJob_access" + uniqueId;
        List<AccessType> accessTypes = new ArrayList<>();
        accessTypes.add(AccessType.CREATE);
        // job update api is called during job creation,
        accessTypes.add(AccessType.UPDATE);
        AccessControllerTestingImp.resetResourcePermission(
                userName,
                new ResourcePermissionData(workspaceName, jobName, ResourceType.JOB, accessTypes));
        AccessControllerTestingImp.addResourcePermission(
                userName,
                new ResourcePermissionData(
                        workspaceName,
                        "source_" + jobName,
                        ResourceType.DATASOURCE,
                        Arrays.asList(AccessType.CREATE, AccessType.READ)));

        AccessControllerTestingImp.addResourcePermission(
                userName,
                new ResourcePermissionData(
                        workspaceName,
                        "console_" + jobName,
                        ResourceType.DATASOURCE,
                        Arrays.asList(AccessType.CREATE, AccessType.READ)));

        long jobVersionId = JobTestingUtils.createJob(jobName);

        Result<Long> executionResult = jobExecutorControllerWrapper.jobExecutor(jobVersionId);
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), executionResult.getCode());

        accessTypes.add(AccessType.EXECUTE);
        AccessControllerTestingImp.resetResourcePermission(
                userName,
                new ResourcePermissionData(workspaceName, jobName, ResourceType.JOB, accessTypes));
        AccessControllerTestingImp.addResourcePermission(
                userName,
                new ResourcePermissionData(
                        workspaceName,
                        "source_" + jobName,
                        ResourceType.DATASOURCE,
                        Collections.singletonList(AccessType.READ)));

        AccessControllerTestingImp.addResourcePermission(
                userName,
                new ResourcePermissionData(
                        workspaceName,
                        "console_" + jobName,
                        ResourceType.DATASOURCE,
                        Collections.singletonList(AccessType.READ)));
        executionResult = jobExecutorControllerWrapper.jobExecutor(jobVersionId);
        assertTrue(executionResult.isSuccess(), executionResult.getMsg());
    }

    @Test
    public void testJobAccessPermissionForSingleJobCreateAPI() {
        String user1 = "user_access_single_job_1" + uniqueId;
        String user2 = "user_access_single_job_2" + uniqueId;
        String pass = "somePassword";
        String workspaceName = "workspace_access_single_job" + uniqueId;

        // create workspaces and users using admin credentials
        createWorkspaceAndUser(workspaceName, user1, pass);
        createUserAndVerify(user2, pass);

        login(new UserLoginReq(user1, pass, workspaceName));

        // Handle create operation
        String jobName = "access_single_api" + uniqueId;
        String fsdSourceName = "fake_source_create" + uniqueId;
        String csSourceName = "console_create" + uniqueId;
        List<AccessType> accessTypes = new ArrayList<>();
        accessTypes.add(AccessType.CREATE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(
                        workspaceName, fsdSourceName, ResourceType.DATASOURCE, accessTypes));

        AccessControllerTestingImp.addResourcePermission(
                user1,
                new ResourcePermissionData(
                        workspaceName, csSourceName, ResourceType.DATASOURCE, accessTypes));
        JobCreateReq jobCreateReq =
                JobTestingUtils.populateJobCreateReqFromFile(jobName, fsdSourceName, csSourceName);
        Result<Long> jobCreation = jobControllerWrapper.createJob(jobCreateReq);
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), jobCreation.getCode());

        accessTypes.clear();
        accessTypes.add(AccessType.CREATE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(workspaceName, jobName, ResourceType.JOB, accessTypes));

        AccessControllerTestingImp.addResourcePermission(
                user1,
                new ResourcePermissionData(
                        workspaceName,
                        fsdSourceName,
                        ResourceType.DATASOURCE,
                        Collections.singletonList(AccessType.READ)));

        AccessControllerTestingImp.addResourcePermission(
                user1,
                new ResourcePermissionData(
                        workspaceName,
                        csSourceName,
                        ResourceType.DATASOURCE,
                        Collections.singletonList(AccessType.READ)));
        jobCreation = jobControllerWrapper.createJob(jobCreateReq);
        assertTrue(jobCreation.isSuccess());

        // Handle read operation
        Result<JobRes> getJobResponse = jobControllerWrapper.getJob(jobCreation.getData());
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), getJobResponse.getCode());

        accessTypes.clear();
        accessTypes.add(AccessType.READ);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(workspaceName, jobName, ResourceType.JOB, accessTypes));
        getJobResponse = jobControllerWrapper.getJob(jobCreation.getData());
        assertTrue(getJobResponse.isSuccess(), getJobResponse.getMsg());

        // Handle update operation
        JobCreateReq jobUpdateReq =
                jobControllerWrapper.convertJobResToJobCreateReq(getJobResponse.getData());
        Result<Void> updateResult =
                jobControllerWrapper.updateJob(jobCreation.getData(), jobUpdateReq);
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), updateResult.getCode());

        accessTypes.clear();
        accessTypes.add(AccessType.UPDATE);
        AccessControllerTestingImp.resetResourcePermission(
                user1,
                new ResourcePermissionData(workspaceName, jobName, ResourceType.JOB, accessTypes));
        AccessControllerTestingImp.addResourcePermission(
                user1,
                new ResourcePermissionData(
                        workspaceName,
                        fsdSourceName,
                        ResourceType.DATASOURCE,
                        Collections.singletonList(AccessType.READ)));

        AccessControllerTestingImp.addResourcePermission(
                user1,
                new ResourcePermissionData(
                        workspaceName,
                        csSourceName,
                        ResourceType.DATASOURCE,
                        Collections.singletonList(AccessType.READ)));
        updateResult = jobControllerWrapper.updateJob(jobCreation.getData(), jobUpdateReq);
        assertTrue(updateResult.isSuccess(), updateResult.getMsg());
    }

    private void createWorkspaceAndUser(String workspaceName, String username, String password) {
        AccessControllerTestingImp.resetResourcePermission(
                "admin",
                new ResourcePermissionData(
                        null,
                        workspaceName,
                        ResourceType.WORKSPACE,
                        Arrays.asList(AccessType.CREATE, AccessType.UPDATE)));
        workspaceControllerWrapper.createWorkspaceAndVerify(workspaceName);
        createUserAndVerify(username, password);
    }

    private void createUserAndVerify(String username, String password) {
        AccessControllerTestingImp.addResourcePermission(
                "admin",
                new ResourcePermissionData(
                        null,
                        username,
                        ResourceType.USER,
                        Collections.singletonList(AccessType.CREATE)));
        Result<AddUserRes> result =
                userControllerWrapper.addUser(getAddUserReq(username, password));
        assertTrue(result.isSuccess());
    }

    private static void login(UserLoginReq userLoginReq) {
        Result<UserSimpleInfoRes> login = userControllerWrapper.login(userLoginReq, null, true);
        assertTrue(login.isSuccess());
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
        AccessControllerTestingImp.clearPermission();
    }

    @AfterAll
    public static void tearDown() {
        AccessControllerTestingImp.disableAccessController();
        seaTunnelWebCluster.stop();
    }
}
