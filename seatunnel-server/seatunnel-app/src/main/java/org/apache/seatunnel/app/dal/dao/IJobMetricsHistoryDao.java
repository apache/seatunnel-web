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

package org.apache.seatunnel.app.dal.dao;

import org.apache.seatunnel.app.dal.entity.JobMetricsHistory;

import java.util.List;

public interface IJobMetricsHistoryDao {
    /** Insert a monitoring record */
    void insert(JobMetricsHistory jobMetricsHistory);

    /** Batch insertion of monitoring records */
    void insertBatch(List<JobMetricsHistory> jobMetricsHistories);

    /** Batch insertion of monitoring records */
    List<JobMetricsHistory> getByJobInstanceId(Long jobInstanceId);

    /** Get monitoring history records according to job instance ID and pipeline ID. */
    List<JobMetricsHistory> getByJobInstanceIdAndPipelineId(Long jobInstanceId, Integer pipelineId);

    /** Query monitoring history based on homework instance ID and time range */
    List<JobMetricsHistory> getByJobInstanceIdAndTimeRange(
            Long jobInstanceId, String startTime, String endTime);
}
