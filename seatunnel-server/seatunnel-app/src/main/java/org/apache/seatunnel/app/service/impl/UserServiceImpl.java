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

package org.apache.seatunnel.app.service.impl;

import org.apache.seatunnel.app.common.Constants;
import org.apache.seatunnel.app.common.UserTokenStatusEnum;
import org.apache.seatunnel.app.config.SeatunnelAuthenticationProvidersConfig;
import org.apache.seatunnel.app.dal.dao.IUserDao;
import org.apache.seatunnel.app.dal.entity.User;
import org.apache.seatunnel.app.domain.dto.user.ListUserDto;
import org.apache.seatunnel.app.domain.dto.user.UpdateUserDto;
import org.apache.seatunnel.app.domain.dto.user.UserLoginLogDto;
import org.apache.seatunnel.app.domain.request.user.AddUserReq;
import org.apache.seatunnel.app.domain.request.user.UpdateUserReq;
import org.apache.seatunnel.app.domain.request.user.UserListReq;
import org.apache.seatunnel.app.domain.request.user.UserLoginReq;
import org.apache.seatunnel.app.domain.response.PageInfo;
import org.apache.seatunnel.app.domain.response.user.AddUserRes;
import org.apache.seatunnel.app.domain.response.user.UserSimpleInfoRes;
import org.apache.seatunnel.app.security.JwtUtils;
import org.apache.seatunnel.app.security.authentication.strategy.IAuthenticationStrategy;
import org.apache.seatunnel.app.security.authentication.strategy.impl.DBAuthenticationStrategy;
import org.apache.seatunnel.app.security.authentication.strategy.impl.LDAPAuthenticationStrategy;
import org.apache.seatunnel.app.service.IRoleService;
import org.apache.seatunnel.app.service.IUserService;
import org.apache.seatunnel.app.utils.PasswordUtils;
import org.apache.seatunnel.server.common.PageData;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserServiceImpl implements IUserService {
    @Resource private IUserDao userDaoImpl;

    @Resource private IRoleService roleServiceImpl;

    @Resource private JwtUtils jwtUtils;

    @Value("${user.default.passwordSalt:seatunnel}")
    private String defaultSalt;

    private final Map<String, IAuthenticationStrategy> strategies = new HashMap<>();

    @Autowired private DBAuthenticationStrategy dbAuthenticationStrategy;

    @Autowired private LDAPAuthenticationStrategy ldapAuthenticationStrategy;

    @Autowired
    private SeatunnelAuthenticationProvidersConfig seatunnelAuthenticationProvidersConfig;

    @PostConstruct
    public void init() {
        List<String> providers = seatunnelAuthenticationProvidersConfig.getProviders();
        if (providers.isEmpty() || providers.contains(Constants.AUTHENTICATION_PROVIDER_DB)) {
            strategies.put(Constants.AUTHENTICATION_PROVIDER_DB, dbAuthenticationStrategy);
        }
        if (providers.contains(Constants.AUTHENTICATION_PROVIDER_LDAP)) {
            strategies.put(Constants.AUTHENTICATION_PROVIDER_LDAP, ldapAuthenticationStrategy);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddUserRes add(AddUserReq addReq) {
        // 1. check duplicate user first
        userDaoImpl.checkUserExists(addReq.getUsername());

        // 2. add a new user.
        final UpdateUserDto dto =
                UpdateUserDto.builder()
                        .id(null)
                        .username(addReq.getUsername())
                        // encryption user's password
                        .password(PasswordUtils.encryptWithSalt(defaultSalt, addReq.getPassword()))
                        .status(addReq.getStatus())
                        .type(addReq.getType())
                        .authProvider(Constants.AUTHENTICATION_PROVIDER_DB)
                        .build();

        final int userId = userDaoImpl.add(dto);
        final AddUserRes res = new AddUserRes();
        res.setId(userId);

        // 3. add to role
        roleServiceImpl.addUserToRole(userId, addReq.getType().intValue());
        return res;
    }

    @Override
    public void update(UpdateUserReq updateReq) {
        final UpdateUserDto dto =
                UpdateUserDto.builder()
                        .id(updateReq.getUserId())
                        .username(updateReq.getUsername())
                        // encryption user's password
                        .password(
                                PasswordUtils.encryptWithSalt(defaultSalt, updateReq.getPassword()))
                        .status(updateReq.getStatus())
                        .type(updateReq.getType())
                        .build();

        userDaoImpl.update(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(int id) {
        userDaoImpl.delete(id);
        roleServiceImpl.deleteByUserId(id);
    }

    @Override
    public PageInfo<UserSimpleInfoRes> list(UserListReq userListReq) {

        final ListUserDto dto = ListUserDto.builder().name(userListReq.getName()).build();

        final PageData<User> userPageData =
                userDaoImpl.list(dto, userListReq.getRealPageNo(), userListReq.getPageSize());

        final List<UserSimpleInfoRes> data =
                userPageData.getData().stream().map(this::translate).collect(Collectors.toList());
        final PageInfo<UserSimpleInfoRes> pageInfo = new PageInfo<>();
        pageInfo.setPageNo(userListReq.getPageNo());
        pageInfo.setPageSize(userListReq.getPageSize());
        pageInfo.setData(data);
        pageInfo.setTotalCount(userPageData.getTotalCount());

        return pageInfo;
    }

    @Override
    public void enable(int id) {
        userDaoImpl.enable(id);
    }

    @Override
    public void disable(int id) {
        userDaoImpl.disable(id);
    }

    @Override
    public UserSimpleInfoRes login(UserLoginReq req, String authType) {
        authType = StringUtils.isEmpty(authType) ? Constants.AUTHENTICATION_PROVIDER_DB : authType;
        if (!strategies.containsKey(authType)) {
            throw new SeatunnelException(
                    SeatunnelErrorEnum.INVALID_AUTHENTICATION_PROVIDER, authType);
        }
        IAuthenticationStrategy strategy = strategies.get(authType);
        User user = strategy.authenticate(req);
        UserSimpleInfoRes translate = translate(user);
        final String token = jwtUtils.genToken(translate.toMap());
        translate.setToken(token);

        final UserLoginLogDto logDto =
                UserLoginLogDto.builder()
                        .token(token)
                        .tokenStatus(UserTokenStatusEnum.ENABLE.enable())
                        .userId(user.getId())
                        .build();
        userDaoImpl.insertLoginLog(logDto);
        return translate;
    }

    private UserSimpleInfoRes translate(User user) {
        final UserSimpleInfoRes info = new UserSimpleInfoRes();
        info.setId(user.getId());
        info.setStatus(user.getStatus());
        info.setType(user.getType());
        info.setCreateTime(user.getCreateTime());
        info.setUpdateTime(user.getUpdateTime());
        info.setName(user.getUsername());
        return info;
    }
}
