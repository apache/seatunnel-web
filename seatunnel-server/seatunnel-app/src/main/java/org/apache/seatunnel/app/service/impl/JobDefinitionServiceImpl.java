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

import org.apache.seatunnel.app.common.EngineType;
import org.apache.seatunnel.app.dal.dao.IJobDefinitionDao;
import org.apache.seatunnel.app.dal.dao.IJobTaskDao;
import org.apache.seatunnel.app.dal.dao.IJobVersionDao;
import org.apache.seatunnel.app.dal.entity.JobDefinition;
import org.apache.seatunnel.app.dal.entity.JobTask;
import org.apache.seatunnel.app.dal.entity.JobVersion;
import org.apache.seatunnel.app.domain.request.connector.BusinessMode;
import org.apache.seatunnel.app.domain.request.job.DataSourceOption;
import org.apache.seatunnel.app.domain.request.job.JobReq;
import org.apache.seatunnel.app.domain.response.PageInfo;
import org.apache.seatunnel.app.domain.response.job.JobDefinitionRes;
import org.apache.seatunnel.app.security.UserContextHolder;
import org.apache.seatunnel.app.service.IJobDefinitionService;
import org.apache.seatunnel.app.service.WorkspaceService;
import org.apache.seatunnel.app.utils.ServletUtils;
import org.apache.seatunnel.common.access.AccessType;
import org.apache.seatunnel.common.access.ResourceType;
import org.apache.seatunnel.common.constants.JobMode;
import org.apache.seatunnel.common.utils.JsonUtils;
import org.apache.seatunnel.server.common.CodeGenerateUtils;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.NonNull;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class JobDefinitionServiceImpl extends SeatunnelBaseServiceImpl
        implements IJobDefinitionService {

    private static final String DEFAULT_VERSION = "1.0";

    @Resource(name = "jobDefinitionDaoImpl")
    private IJobDefinitionDao jobDefinitionDao;

    @Resource(name = "jobTaskDaoImpl")
    private IJobTaskDao jobTaskDao;

    @Resource(name = "jobVersionDaoImpl")
    private IJobVersionDao jobVersionDao;

    @Resource private WorkspaceService workspaceService;

    @Override
    @Transactional
    public long createJob(JobReq jobReq) throws CodeGenerateUtils.CodeGenerateException {
        Integer userId = ServletUtils.getCurrentUserId();
        permCheck(jobReq.getName(), AccessType.CREATE);
        long uuid = CodeGenerateUtils.getInstance().genCode();
        jobDefinitionDao.add(
                JobDefinition.builder()
                        .id(uuid)
                        .name(jobReq.getName())
                        .description(jobReq.getDescription())
                        .createUserId(userId)
                        .updateUserId(userId)
                        .jobType(jobReq.getJobType().name())
                        .build());
        JobVersion.JobVersionBuilder builder = JobVersion.builder();
        builder.jobId(uuid)
                .createUserId(userId)
                .updateUserId(userId)
                .name(DEFAULT_VERSION)
                .id(uuid)
                .engineName(EngineType.SeaTunnel)
                .engineVersion("2.3.8");
        if (BusinessMode.DATA_INTEGRATION.equals(jobReq.getJobType())) {
            builder.jobMode(JobMode.BATCH);
        } else if (BusinessMode.DATA_REPLICA.equals(jobReq.getJobType())) {
            builder.jobMode(JobMode.STREAMING);
        }
        jobVersionDao.createVersion(builder.build());

        return uuid;
    }

    @Override
    public PageInfo<JobDefinitionRes> getJob(String name, Integer pageNo, Integer pageSize) {
        return getJob(name, pageNo, pageSize, null);
    }

    @Override
    public PageInfo<JobDefinitionRes> getJob(
            String searchName, Integer pageNo, Integer pageSize, String jobMode) {
        if (StringUtils.isNotEmpty(jobMode)) {
            try {
                JobMode.valueOf(jobMode);
            } catch (Exception e) {
                throw new SeatunnelException(
                        SeatunnelErrorEnum.ILLEGAL_STATE, "Unsupported JobMode");
            }
        }
        PageInfo<JobDefinitionRes> job =
                jobDefinitionDao.getJob(searchName, pageNo, pageSize, jobMode);
        if (CollectionUtils.isEmpty(job.getData())) {
            return job;
        }

        List<JobDefinitionRes> filteredJobs =
                job.getData().stream()
                        .filter(jobDefinitionRes -> hasReadPerm(jobDefinitionRes.getName()))
                        .collect(Collectors.toList());

        PageInfo<JobDefinitionRes> jobs = new PageInfo<>();
        jobs.setData(filteredJobs);
        jobs.setPageSize(pageSize);
        jobs.setPageNo(pageNo);
        jobs.setTotalCount(filteredJobs.size());
        return jobs;
    }

    @Override
    public Map<Long, String> getJob(@NonNull String name) {
        List<JobDefinition> job = jobDefinitionDao.getJobList(name);
        if (CollectionUtils.isEmpty(job)) {
            return new HashMap<>();
        }

        Map<Long, String> jobDefineMap = new HashMap<>();
        job.forEach(
                jobDefine -> {
                    if (hasReadPerm(jobDefine.getName())) {
                        jobDefineMap.put(jobDefine.getId(), jobDefine.getName());
                    }
                });

        return jobDefineMap;
    }

    @Override
    public JobDefinition getJobDefinitionByJobId(long jobId) {
        JobDefinition job = jobDefinitionDao.getJob(jobId);
        if (null != job) {
            permCheck(job.getName(), AccessType.READ);
        }
        return job;
    }

    @Override
    public List<JobVersion> getJobVersionByDataSourceId(long datasourceId) {
        List<Long> versionIds =
                jobTaskDao.getJobTaskByDataSourceId(datasourceId).stream()
                        .map(JobTask::getVersionId)
                        .distinct()
                        .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(versionIds)) {
            return new ArrayList<>();
        }
        return jobVersionDao.getVersionsByIds(versionIds);
    }

    @Override
    public boolean getUsedByDataSourceIdAndVirtualTable(long datasourceId, String tableName) {
        List<DataSourceOption> options =
                jobTaskDao.getJobTaskByDataSourceId(datasourceId).stream()
                        .map(JobTask::getDataSourceOption)
                        .distinct()
                        .map(
                                option ->
                                        StringUtils.isEmpty(option)
                                                ? null
                                                : JsonUtils.parseObject(
                                                        option, DataSourceOption.class))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        return options.stream().anyMatch(option -> option.getTables().contains(tableName));
    }

    @Override
    public void deleteJob(long id) {
        JobDefinition job = jobDefinitionDao.getJob(id);
        if (job != null) {
            permCheck(job.getName(), AccessType.DELETE);
        }
        jobDefinitionDao.delete(id);
    }

    @Override
    public List<String> getJobDefinitionNames(String workspaceName, String searchName) {
        return jobDefinitionDao.getJobDefinitionNames(
                workspaceService.getWorkspaceIdOrCurrent(workspaceName), searchName);
    }

    private void permCheck(String resourceName, AccessType accessType) {
        permissionCheck(
                resourceName, ResourceType.JOB, accessType, UserContextHolder.getAccessInfo());
    }

    private boolean hasReadPerm(String resourceName) {
        return hasPermission(
                resourceName, ResourceType.JOB, AccessType.READ, UserContextHolder.getAccessInfo());
    }
}
