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

package org.apache.seatunnel.app.dal.dao.impl;

import org.apache.seatunnel.app.dal.dao.IJobInstanceDao;
import org.apache.seatunnel.app.dal.entity.JobInstance;
import org.apache.seatunnel.app.dal.mapper.JobInstanceMapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.NonNull;

import javax.annotation.Resource;

@Repository
public class JobInstanceDaoImpl implements IJobInstanceDao {
    @Resource private JobInstanceMapper jobInstanceMapper;

    @Override
    public JobInstance getJobInstance(@NonNull Long jobInstanceId) {
        return jobInstanceMapper.selectById(jobInstanceId);
    }

    @Override
    public JobInstance getJobInstanceByEngineId(@NonNull Long jobEngineId) {
        return jobInstanceMapper.selectOne(
                new LambdaQueryWrapper<>(new JobInstance())
                        .eq(JobInstance::getJobEngineId, jobEngineId));
    }

    @Override
    public void update(@NonNull JobInstance jobInstance) {
        jobInstanceMapper.updateById(jobInstance);
    }

    @Override
    public void insert(@NonNull JobInstance jobInstance) {
        jobInstanceMapper.insert(jobInstance);
    }

    @Override
    public JobInstanceMapper getJobInstanceMapper() {
        return jobInstanceMapper;
    }
}
