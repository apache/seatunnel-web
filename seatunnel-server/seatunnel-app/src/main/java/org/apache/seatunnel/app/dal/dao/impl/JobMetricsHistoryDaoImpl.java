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

import org.apache.seatunnel.app.dal.dao.IJobMetricsHistoryDao;
import org.apache.seatunnel.app.dal.entity.JobMetricsHistory;
import org.apache.seatunnel.app.dal.mapper.JobMetricsHistoryMapper;

import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

import java.util.List;

@Repository
public class JobMetricsHistoryDaoImpl implements IJobMetricsHistoryDao {

    @Resource private JobMetricsHistoryMapper jobMetricsHistoryMapper;

    @Override
    public void insert(JobMetricsHistory jobMetricsHistory) {
        jobMetricsHistoryMapper.insert(jobMetricsHistory);
    }

    @Override
    public void insertBatch(List<JobMetricsHistory> jobMetricsHistories) {
        if (!jobMetricsHistories.isEmpty()) {
            jobMetricsHistoryMapper.insertBatchMetrics(jobMetricsHistories);
        }
    }

    @Override
    public List<JobMetricsHistory> getByJobInstanceId(Long jobInstanceId) {
        return jobMetricsHistoryMapper.queryJobMetricsHistoryByInstanceId(jobInstanceId);
    }

    @Override
    public List<JobMetricsHistory> getByJobInstanceIdAndPipelineId(
            Long jobInstanceId, Integer pipelineId) {
        return jobMetricsHistoryMapper.queryJobMetricsHistoryByInstanceIdAndPipelineId(
                jobInstanceId, pipelineId);
    }

    @Override
    public List<JobMetricsHistory> getByJobInstanceIdAndTimeRange(
            Long jobInstanceId, String startTime, String endTime) {
        return jobMetricsHistoryMapper.queryJobMetricsHistoryByInstanceIdAndTimeRange(
                jobInstanceId, startTime, endTime);
    }
}
