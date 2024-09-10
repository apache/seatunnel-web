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

import org.apache.seatunnel.app.dal.dao.IJobDefinitionDao;
import org.apache.seatunnel.app.dal.dao.IJobVersionDao;
import org.apache.seatunnel.app.dal.entity.JobDefinition;
import org.apache.seatunnel.app.dal.entity.JobVersion;
import org.apache.seatunnel.app.domain.request.job.JobConfig;
import org.apache.seatunnel.app.domain.response.job.JobConfigRes;
import org.apache.seatunnel.app.permission.constants.SeatunnelFuncPermissionKeyConstant;
import org.apache.seatunnel.app.service.IJobConfigService;
import org.apache.seatunnel.common.constants.JobMode;
import org.apache.seatunnel.common.utils.JsonUtils;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;

@Service
public class JobConfigServiceImpl extends SeatunnelBaseServiceImpl implements IJobConfigService {
    private static final String JOB_MODE = "job.mode";

    @Resource private IJobVersionDao jobVersionDao;

    @Resource private IJobDefinitionDao jobDefinitionDao;

    @Override
    public JobConfigRes getJobConfig(long jobVersionId) throws JsonProcessingException {
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_CONFIG_DETAIL, 0);
        JobVersion jobVersion = jobVersionDao.getVersionById(jobVersionId);
        if (jobVersion == null) {
            throw new SeatunnelException(
                    SeatunnelErrorEnum.RESOURCE_NOT_FOUND, "job version not found.");
        }
        JobDefinition jobDefinition = jobDefinitionDao.getJob(jobVersion.getJobId());
        JobConfigRes jobConfigRes = new JobConfigRes();
        jobConfigRes.setName(jobDefinition.getName());
        jobConfigRes.setId(jobVersion.getId());
        jobConfigRes.setDescription(jobDefinition.getDescription());
        jobConfigRes.setEnv(
                StringUtils.isEmpty(jobVersion.getEnv())
                        ? null
                        : JsonUtils.toMap(jobVersion.getEnv(), String.class, Object.class));
        jobConfigRes.setEngine(jobVersion.getEngineName());
        return jobConfigRes;
    }

    @Override
    @Transactional
    public void updateJobConfig(int userId, long jobVersionId, JobConfig jobConfig)
            throws JsonProcessingException {
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_CONFIG_UPDATE, 0);
        JobVersion version = jobVersionDao.getVersionById(jobVersionId);
        if (version == null) {
            throw new SeatunnelException(
                    SeatunnelErrorEnum.RESOURCE_NOT_FOUND, "job version not found.");
        }
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setId(version.getJobId());
        jobDefinition.setUpdateUserId(userId);
        jobDefinition.setName(jobConfig.getName());
        jobDefinition.setDescription(jobConfig.getDescription());
        jobDefinitionDao.updateJob(jobDefinition);
        if (jobConfig.getEnv().containsKey(JOB_MODE)) {
            JobMode jobMode = JobMode.valueOf(jobConfig.getEnv().get(JOB_MODE).toString());
            jobVersionDao.updateVersion(
                    JobVersion.builder()
                            .jobId(version.getJobId())
                            .id(version.getId())
                            .jobMode(jobMode)
                            .engineName(jobConfig.getEngine())
                            .updateUserId(userId)
                            .env(JsonUtils.toJsonString(jobConfig.getEnv()))
                            .build());
        } else {
            throw new SeatunnelException(SeatunnelErrorEnum.ILLEGAL_STATE, "job mode is not set");
        }
    }
}
