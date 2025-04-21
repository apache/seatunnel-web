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

import org.apache.seatunnel.app.dal.dao.IJobVersionDao;
import org.apache.seatunnel.app.dal.entity.JobVersion;
import org.apache.seatunnel.app.dal.mapper.JobVersionMapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import javax.annotation.Resource;

import java.util.List;

import static org.apache.seatunnel.app.utils.ServletUtils.getCurrentWorkspaceId;

@Repository
public class JobVersionDaoImpl implements IJobVersionDao {

    @Resource private JobVersionMapper jobVersionMapper;

    @Override
    public void createVersion(JobVersion jobVersion) {
        jobVersion.setWorkspaceId(getCurrentWorkspaceId());
        jobVersionMapper.insert(jobVersion);
    }

    @Override
    public void updateVersion(JobVersion version) {
        version.setWorkspaceId(getCurrentWorkspaceId());
        jobVersionMapper.updateById(version);
    }

    @Override
    public JobVersion getLatestVersion(long jobId) {
        return jobVersionMapper.selectOne(
                new QueryWrapper<JobVersion>()
                        .eq("job_id", jobId)
                        .eq("workspace_id", getCurrentWorkspaceId())
                        .orderByDesc("create_time")
                        .last("LIMIT 1"));
    }

    @Override
    public List<JobVersion> getLatestVersionByJobIds(List<Long> jobIds) {
        return jobVersionMapper.selectList(
                new QueryWrapper<JobVersion>()
                        .in("job_id", jobIds)
                        .eq("workspace_id", getCurrentWorkspaceId())
                        .orderByDesc("create_time"));
    }

    @Override
    public JobVersion getVersionById(long jobVersionId) {
        return jobVersionMapper.selectOne(
                new QueryWrapper<JobVersion>()
                        .eq("id", jobVersionId)
                        .eq("workspace_id", getCurrentWorkspaceId()));
    }

    @Override
    public List<JobVersion> getVersionsByIds(List<Long> jobVersionIds) {
        return jobVersionMapper.selectList(
                new QueryWrapper<JobVersion>()
                        .in("id", jobVersionIds)
                        .eq("workspace_id", getCurrentWorkspaceId()));
    }
}
