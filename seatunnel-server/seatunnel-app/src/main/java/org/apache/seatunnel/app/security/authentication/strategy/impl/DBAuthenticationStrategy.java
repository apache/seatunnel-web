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
package org.apache.seatunnel.app.security.authentication.strategy.impl;

import org.apache.seatunnel.app.common.Constants;
import org.apache.seatunnel.app.dal.dao.IUserDao;
import org.apache.seatunnel.app.dal.entity.User;
import org.apache.seatunnel.app.domain.request.user.UserLoginReq;
import org.apache.seatunnel.app.security.authentication.strategy.IAuthenticationStrategy;
import org.apache.seatunnel.app.utils.PasswordUtils;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.apache.seatunnel.server.common.SeatunnelErrorEnum.USERNAME_PASSWORD_NO_MATCHED;

@Component
public class DBAuthenticationStrategy implements IAuthenticationStrategy {

    @Autowired private IUserDao userDaoImpl;

    @Value("${user.default.passwordSalt:seatunnel}")
    private String defaultSalt;

    @Override
    public User authenticate(UserLoginReq req) {
        final String password = PasswordUtils.encryptWithSalt(defaultSalt, req.getPassword());
        final User user =
                userDaoImpl.checkPassword(
                        req.getUsername(), password, Constants.AUTHENTICATION_PROVIDER_DB);
        if (Objects.isNull(user)) {
            throw new SeatunnelException(USERNAME_PASSWORD_NO_MATCHED);
        }
        return user;
    }
}
