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
import org.apache.seatunnel.app.domain.request.user.AddUserReq;
import org.apache.seatunnel.app.domain.request.user.UpdateUserReq;
import org.apache.seatunnel.app.domain.response.user.AddUserRes;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserControllerTest {
    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static UserControllerWrapper userControllerWrapper;
    private static String uniqueId = "_" + System.currentTimeMillis();

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        userControllerWrapper = new UserControllerWrapper();
    }

    @Test
    public void addUser_shouldReturnSuccess_whenValidRequest() {
        String user = "addUser" + uniqueId;
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
        String user = "updateUser" + uniqueId;
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
        String user = "deleteUser" + uniqueId;
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
        assertNotNull(result.getData());
    }

    @AfterAll
    public static void tearDown() {
        Result<Void> logout = userControllerWrapper.logout();
        assertTrue(logout.isSuccess());
        seaTunnelWebCluster.stop();
    }
}
