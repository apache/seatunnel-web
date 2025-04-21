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

import org.apache.seatunnel.app.dal.dao.IJobDefinitionDao;
import org.apache.seatunnel.app.dal.entity.JobDefinition;
import org.apache.seatunnel.app.dal.mapper.JobMapper;
import org.apache.seatunnel.app.domain.response.PageInfo;
import org.apache.seatunnel.app.domain.response.job.JobDefinitionRes;

import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.NonNull;

import javax.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.seatunnel.app.utils.ServletUtils.getCurrentWorkspaceId;

@Repository
public class JobDefinitionDaoImpl implements IJobDefinitionDao {

    @Resource private JobMapper jobMapper;

    @Override
    public void add(JobDefinition job) {
        job.setWorkspaceId(getCurrentWorkspaceId());
        jobMapper.insert(job);
    }

    @Override
    public JobDefinition getJob(long id) {
        return jobMapper.selectOne(
                Wrappers.<JobDefinition>lambdaQuery()
                        .eq(JobDefinition::getId, id)
                        .eq(JobDefinition::getWorkspaceId, getCurrentWorkspaceId()));
    }

    @Override
    public void updateJob(JobDefinition jobDefinition) {
        jobMapper.update(
                jobDefinition,
                Wrappers.<JobDefinition>lambdaUpdate()
                        .eq(JobDefinition::getId, jobDefinition.getId())
                        .eq(JobDefinition::getWorkspaceId, getCurrentWorkspaceId()));
    }

    @Override
    public PageInfo<JobDefinitionRes> getJob(
            String searchName, Integer pageNo, Integer pageSize, String jobMode) {
        IPage<JobDefinitionRes> jobDefinitionIPage;
        if (StringUtils.isEmpty(jobMode)) {
            jobDefinitionIPage =
                    jobMapper.queryJobListPaging(
                            new Page<>(pageNo, pageSize), searchName, getCurrentWorkspaceId());
        } else {
            jobDefinitionIPage =
                    jobMapper.queryJobListPagingWithJobMode(
                            new Page<>(pageNo, pageSize),
                            searchName,
                            jobMode,
                            getCurrentWorkspaceId());
        }
        PageInfo<JobDefinitionRes> jobs = new PageInfo<>();
        jobs.setData(jobDefinitionIPage.getRecords());
        jobs.setPageSize(pageSize);
        jobs.setPageNo(pageNo);
        jobs.setTotalCount((int) jobDefinitionIPage.getTotal());
        return jobs;
    }

    @Override
    public List<JobDefinition> getJobList(@NonNull String name) {
        return jobMapper.queryJobList(name, getCurrentWorkspaceId());
    }

    @Override
    public JobDefinition getJobByName(@NonNull String name) {
        return jobMapper.queryJob(name, getCurrentWorkspaceId());
    }

    @Override
    public void delete(long id) {
        jobMapper.delete(
                Wrappers.<JobDefinition>lambdaQuery()
                        .eq(JobDefinition::getId, id)
                        .eq(JobDefinition::getWorkspaceId, getCurrentWorkspaceId()));
    }

    @Override
    public List<String> getJobDefinitionNames(Long workspaceId, String searchName) {
        LambdaQueryWrapper<JobDefinition> query = Wrappers.<JobDefinition>lambdaQuery();
        query.eq(JobDefinition::getWorkspaceId, workspaceId);
        if (StringUtils.isNotEmpty(searchName)) {
            query.like(JobDefinition::getName, "%" + searchName + "%");
        }
        return jobMapper.selectList(query).stream()
                .map(JobDefinition::getName)
                .collect(Collectors.toList());
    }
}
