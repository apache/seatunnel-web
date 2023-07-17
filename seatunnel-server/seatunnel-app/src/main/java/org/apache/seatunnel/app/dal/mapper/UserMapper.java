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

package org.apache.seatunnel.app.dal.mapper;

import org.apache.seatunnel.app.dal.entity.User;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User selectByPrimaryKey(@Param("id") Integer id);

    void insert(User user);

    int updateByPrimaryKey(User user);

    void deleteByPrimaryKey(@Param("id") int id);

    List<User> selectBySelectiveAndPage(
            @Param("user") User user, @Param("start") int start, @Param("offset") int offset);

    void updateStatus(@Param("id") int id, @Param("status") byte status);

    User selectByName(@Param("username") String username);

    int countBySelective(@Param("user") User user);

    User selectByNameAndPasswd(
            @Param("username") String username, @Param("password") String password);

    List<User> queryEnabledUsers();
}
