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
import org.apache.seatunnel.app.common.TokenProvider;
import org.apache.seatunnel.app.domain.request.user.AddUserReq;
import org.apache.seatunnel.app.domain.request.user.UpdateUserReq;
import org.apache.seatunnel.app.domain.request.user.UserLoginReq;
import org.apache.seatunnel.app.domain.response.PageInfo;
import org.apache.seatunnel.app.domain.response.user.AddUserRes;
import org.apache.seatunnel.app.domain.response.user.UserSimpleInfoRes;
import org.apache.seatunnel.app.utils.JSONTestUtils;
import org.apache.seatunnel.common.utils.JsonUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserControllerWrapper extends SeatunnelWebTestingBase {

    public Result<AddUserRes> addUser(AddUserReq addUserReq) {
        String requestBody = JsonUtils.toJsonString(addUserReq);
        String response = sendRequest(url("user"), requestBody, "POST");
        return JSONTestUtils.parseObject(response, new TypeReference<Result<AddUserRes>>() {});
    }

    public Result<Void> updateUser(String userId, UpdateUserReq updateUserReq) {
        String requestBody = JsonUtils.toJsonString(updateUserReq);
        String response = sendRequest(url("user/" + userId), requestBody, "PUT");
        return JSONTestUtils.parseObject(response, Result.class);
    }

    public Result<Void> deleteUser(String userId) {
        String response = sendRequest(url("user/" + userId), null, "DELETE");
        return JSONTestUtils.parseObject(response, Result.class);
    }

    public Result<Void> listUsers(Integer pageNo, Integer pageSize) {
        String response =
                sendRequest(urlWithParam("user?pageNo=" + pageNo + "&pageSize=" + pageSize));
        return JSONTestUtils.parseObject(response, Result.class);
    }

    public Result<PageInfo<UserSimpleInfoRes>> listUsers(String name) {
        String response = sendRequest(urlWithParam("user?name=" + name + "&pageNo=1&pageSize=10"));
        return JSONTestUtils.parseObject(
                response, new TypeReference<Result<PageInfo<UserSimpleInfoRes>>>() {});
    }

    public Result<PageInfo<UserSimpleInfoRes>> listUsers() {
        String response = sendRequest(urlWithParam("user?pageNo=1&pageSize=10000"));
        return JSONTestUtils.parseObject(
                response, new TypeReference<Result<PageInfo<UserSimpleInfoRes>>>() {});
    }

    public Result<Void> disableUser(String userId) {
        String response = sendRequest(url("user/" + userId + "/disable"), null, "PUT");
        return JSONTestUtils.parseObject(response, Result.class);
    }

    public Result<Void> enableUser(String userId) {
        String response = sendRequest(url("user/" + userId + "/enable"), null, "PATCH");
        return JSONTestUtils.parseObject(response, Result.class);
    }

    public Result<AddUserRes> addUser(String userName, String password) {
        String requestBody = JsonUtils.toJsonString(getAddUserReq(userName, password));
        String response = sendRequest(url("user"), requestBody, "POST");
        return JSONTestUtils.parseObject(response, new TypeReference<Result<AddUserRes>>() {});
    }

    public Result<Void> logout() {
        String response = sendRequest(url("user/logout"), null, "PATCH");
        Result<Void> logoutResult =
                JSONTestUtils.parseObject(response, new TypeReference<Result<Void>>() {});
        assert logoutResult != null;
        if (logoutResult.isSuccess()) {
            TokenProvider.setToken(null);
        }
        return logoutResult;
    }

    public void loginAndSetCurrentUser(UserLoginReq userLoginReq) {
        Result<UserSimpleInfoRes> login = login(userLoginReq, null, true);
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
}
