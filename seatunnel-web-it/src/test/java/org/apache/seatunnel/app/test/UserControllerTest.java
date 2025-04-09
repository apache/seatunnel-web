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
import org.apache.seatunnel.app.controller.UserControllerWrapper;
import org.apache.seatunnel.app.controller.WorkspaceControllerWrapper;
import org.apache.seatunnel.app.domain.request.user.AddUserReq;
import org.apache.seatunnel.app.domain.request.user.UpdateUserReq;
import org.apache.seatunnel.app.domain.request.user.UserLoginReq;
import org.apache.seatunnel.app.domain.response.user.AddUserRes;
import org.apache.seatunnel.app.domain.response.user.UserSimpleInfoRes;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserControllerTest {
    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static UserControllerWrapper userControllerWrapper;
    private static WorkspaceControllerWrapper workspaceControllerWrapper;
    final Supplier<String> uniqueId = () -> "_" + System.nanoTime();

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        userControllerWrapper = new UserControllerWrapper();
        workspaceControllerWrapper = new WorkspaceControllerWrapper();
    }

    @Test
    public void addUser_shouldReturnSuccess_whenValidRequest() {
        String user = "addUser" + uniqueId.get();
        AddUserReq addUserReq = getAddUserReq(user, "pass1");
        Result<AddUserRes> result = userControllerWrapper.addUser(addUserReq);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().getId() > 0);
    }

    private static AddUserReq getAddUserReq(String user, String pass) {
        AddUserReq addUserReq = new AddUserReq();
        addUserReq.setUsername(user);
        addUserReq.setPassword(pass);
        addUserReq.setStatus((byte) 0);
        addUserReq.setType((byte) 0);
        return addUserReq;
    }

    @Test
    public void updateUser_shouldReturnSuccess_whenValidRequest() {
        String user = "updateUser" + uniqueId.get();
        AddUserReq addUserReq = getAddUserReq(user, "pass2");
        Result<AddUserRes> result = userControllerWrapper.addUser(addUserReq);
        assertTrue(result.isSuccess());
        UpdateUserReq updateUserReq = new UpdateUserReq();
        updateUserReq.setUsername(user);
        updateUserReq.setUserId(result.getData().getId());
        updateUserReq.setPassword("pass3");
        updateUserReq.setStatus((byte) 0);
        updateUserReq.setType((byte) 0);
        Result<Void> updateUserResult =
                userControllerWrapper.updateUser(
                        String.valueOf(updateUserReq.getUserId()), updateUserReq);
        assertTrue(updateUserResult.isSuccess());
    }

    @Test
    public void deleteUser_shouldReturnSuccess_whenValidUserId() {
        String user = "deleteUser" + uniqueId.get();
        AddUserReq addUserReq = getAddUserReq(user, "pass3");
        Result<AddUserRes> result = userControllerWrapper.addUser(addUserReq);
        assertTrue(result.isSuccess());
        Result<Void> deleteUserResult =
                userControllerWrapper.deleteUser(String.valueOf(result.getData().getId()));
        assertTrue(deleteUserResult.isSuccess());
    }

    @Test
    public void listUsers_shouldReturnUsers_whenUsersExist() {
        Result<Void> result = userControllerWrapper.listUsers(1, 10);
        assertTrue(result.isSuccess());
    }

    @Test
    public void login_shouldPass_whenValidAuthType() {
        String user = "loginUser" + uniqueId.get();
        AddUserReq addUserReq = getAddUserReq(user, "pass4");
        Result<AddUserRes> addUserResult = userControllerWrapper.addUser(addUserReq);
        assertTrue(addUserResult.isSuccess());

        UserLoginReq loginReq = new UserLoginReq();
        loginReq.setUsername(user);
        loginReq.setPassword("pass4");

        Result<UserSimpleInfoRes> loginResult = userControllerWrapper.login(loginReq, "DB");
        assertTrue(loginResult.isSuccess());
    }

    @Test
    public void login_shouldPass_whenNoAuthType() {
        String user = "loginUser" + uniqueId.get();
        AddUserReq addUserReq = getAddUserReq(user, "pass5");
        Result<AddUserRes> addUserResult = userControllerWrapper.addUser(addUserReq);
        assertTrue(addUserResult.isSuccess());

        UserLoginReq loginReq = new UserLoginReq();
        loginReq.setUsername(user);
        loginReq.setPassword("pass5");

        Result<UserSimpleInfoRes> loginResult = userControllerWrapper.login(loginReq);
        assertTrue(loginResult.isSuccess());
    }

    @Test
    public void login_shouldFail_whenInvalidAuthType() {
        String user = "loginUser" + uniqueId.get();
        AddUserReq addUserReq = getAddUserReq(user, "pass6");
        Result<AddUserRes> addUserResult = userControllerWrapper.addUser(addUserReq);
        assertTrue(addUserResult.isSuccess());

        UserLoginReq loginReq = new UserLoginReq();
        loginReq.setUsername(user);
        loginReq.setPassword("pass6");

        Result<UserSimpleInfoRes> loginResult =
                userControllerWrapper.login(loginReq, "INVALID_AUTH_TYPE");
        assertTrue(loginResult.isFailed());
        assertEquals("Invalid authentication provider [INVALID_AUTH_TYPE]", loginResult.getMsg());
    }

    @Test
    public void disabledUser_shouldNotBeAbleToLogin() {
        String user = "disabledUser" + uniqueId.get();
        String pass = "pass7";
        AddUserReq addUserReq = getAddUserReq(user, pass);
        Result<AddUserRes> result = userControllerWrapper.addUser(addUserReq);
        assertTrue(result.isSuccess());

        // Disable the user
        UpdateUserReq updateUserReq = new UpdateUserReq();
        updateUserReq.setUsername(user);
        updateUserReq.setUserId(result.getData().getId());
        updateUserReq.setPassword(pass);
        updateUserReq.setStatus((byte) 1);
        updateUserReq.setType((byte) 0);
        Result<Void> disableUserResult =
                userControllerWrapper.updateUser(
                        Long.toString(result.getData().getId()), updateUserReq);
        assertTrue(disableUserResult.isSuccess());

        // Attempt to login with the disabled user
        UserLoginReq loginReq = new UserLoginReq();
        loginReq.setUsername(user);
        loginReq.setPassword(pass);
        Result<UserSimpleInfoRes> loginResult = userControllerWrapper.login(loginReq);
        assertFalse(loginResult.isSuccess());
        assertEquals(
                SeatunnelErrorEnum.USERNAME_PASSWORD_NO_MATCHED.getCode(), loginResult.getCode());
    }

    @Test
    public void loginWithWorkspace() {
        String user = "userLoginWithWorkspace" + uniqueId.get();
        String pass = "pass9";
        String workspace = "workspaceForLogin" + uniqueId.get();

        workspaceControllerWrapper.createWorkspaceAndVerify(workspace);
        AddUserReq addUserReq = getAddUserReq(user, pass);
        Result<AddUserRes> result = userControllerWrapper.addUser(addUserReq);
        assertTrue(result.isSuccess());
        UserLoginReq userLoginReq = new UserLoginReq();
        userLoginReq.setUsername(user);
        userLoginReq.setPassword(pass);
        userLoginReq.setWorkspaceName(workspace);
        Result<UserSimpleInfoRes> login = userControllerWrapper.login(userLoginReq);
        assertTrue(login.isSuccess());

        // login with workspace when workspace does not exist
        userLoginReq.setWorkspaceName("nonExistentWorkspace");
        Result<UserSimpleInfoRes> loginResult = userControllerWrapper.login(userLoginReq);
        assertFalse(loginResult.isSuccess());
        assertEquals(SeatunnelErrorEnum.RESOURCE_NOT_FOUND.getCode(), loginResult.getCode());

        // login without any workspace, should login with the workspace of the user
        userLoginReq.setWorkspaceName(null);
        Result<UserSimpleInfoRes> loginWithoutWorkspace = userControllerWrapper.login(userLoginReq);
        assertTrue(loginWithoutWorkspace.isSuccess());
    }

    @AfterAll
    public static void tearDown() {
        Result<Void> logout = userControllerWrapper.logout();
        assertTrue(logout.isSuccess());
        seaTunnelWebCluster.stop();
    }
}
