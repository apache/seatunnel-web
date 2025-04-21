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
package org.apache.seatunnel.app.common;

import org.apache.seatunnel.app.domain.request.user.UserLoginReq;
import org.apache.seatunnel.app.domain.response.user.UserSimpleInfoRes;

public class TokenProvider {
    private static String token;

    public static String getToken() {
        if (token == null) {
            initToken();
        }
        return token;
    }

    private static void initToken() {
        SeatunnelWebTestingBase seatunnelWebTestingBase = new SeatunnelWebTestingBase();
        UserLoginReq userLoginReq = new UserLoginReq();
        userLoginReq.setUsername("admin");
        userLoginReq.setPassword("admin");
        Result<UserSimpleInfoRes> loginResponse = seatunnelWebTestingBase.login(userLoginReq);
        setToken(loginResponse.getData().getToken());
    }

    public static void setToken(String token) {
        TokenProvider.token = token;
    }
}
