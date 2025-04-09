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

import org.apache.seatunnel.app.dal.dao.IJobTaskDao;
import org.apache.seatunnel.app.dal.entity.JobTask;
import org.apache.seatunnel.app.dal.mapper.JobTaskMapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import javax.annotation.Resource;

import java.util.List;

import static org.apache.seatunnel.app.utils.ServletUtils.getCurrentWorkspaceId;

@Repository
public class JobTaskDaoImpl implements IJobTaskDao {

    @Resource private JobTaskMapper jobTaskMapper;

    @Override
    public List<JobTask> getTasksByVersionId(long jobVersionId) {
        return jobTaskMapper.selectList(
                Wrappers.lambdaQuery(new JobTask())
                        .eq(JobTask::getVersionId, jobVersionId)
                        .eq(JobTask::getWorkspaceId, getCurrentWorkspaceId()));
    }

    @Override
    public void insertTask(JobTask jobTask) {
        if (jobTask != null) {
            jobTask.setWorkspaceId(getCurrentWorkspaceId());
            jobTaskMapper.insert(jobTask);
        }
    }

    @Override
    public void updateTask(JobTask jobTask) {
        if (jobTask != null) {
            jobTask.setWorkspaceId(getCurrentWorkspaceId());
            jobTaskMapper.updateById(jobTask);
        }
    }

    @Override
    public JobTask getTask(long jobVersionId, String pluginId) {
        return jobTaskMapper.selectOne(
                Wrappers.lambdaQuery(new JobTask())
                        .eq(JobTask::getVersionId, jobVersionId)
                        .eq(JobTask::getPluginId, pluginId)
                        .eq(JobTask::getWorkspaceId, getCurrentWorkspaceId()));
    }

    @Override
    public List<JobTask> getJobTaskByDataSourceId(long datasourceId) {
        return jobTaskMapper.selectList(
                Wrappers.lambdaQuery(new JobTask())
                        .eq(JobTask::getDataSourceId, datasourceId)
                        .eq(JobTask::getWorkspaceId, getCurrentWorkspaceId()));
    }

    @Override
    public void updateTasks(List<JobTask> jobTasks) {
        Long workspaceId = getCurrentWorkspaceId();
        jobTasks.forEach(
                jobTask -> {
                    jobTask.setWorkspaceId(workspaceId);
                    jobTaskMapper.updateById(jobTask);
                });
    }

    @Override
    public void deleteTasks(List<Long> jobTaskIds) {
        if (!jobTaskIds.isEmpty()) {
            jobTaskMapper.delete(
                    Wrappers.lambdaQuery(new JobTask())
                            .in(JobTask::getId, jobTaskIds)
                            .eq(JobTask::getWorkspaceId, getCurrentWorkspaceId()));
        }
    }

    @Override
    public void deleteTask(long jobVersionId, String pluginId) {
        jobTaskMapper.delete(
                Wrappers.lambdaQuery(new JobTask())
                        .eq(JobTask::getVersionId, jobVersionId)
                        .eq(JobTask::getPluginId, pluginId)
                        .eq(JobTask::getWorkspaceId, getCurrentWorkspaceId()));
    }

    @Override
    public void deleteTaskByVersionId(long id) {
        jobTaskMapper.delete(
                Wrappers.lambdaQuery(new JobTask())
                        .eq(JobTask::getVersionId, id)
                        .eq(JobTask::getWorkspaceId, getCurrentWorkspaceId()));
    }
}
