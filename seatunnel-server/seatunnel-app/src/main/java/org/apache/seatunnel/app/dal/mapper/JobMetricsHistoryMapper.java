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

import org.apache.seatunnel.app.dal.entity.JobMetricsHistory;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

@Mapper
public interface JobMetricsHistoryMapper extends BaseMapper<JobMetricsHistory> {

    /** Batch insertion of monitoring history records */
    void insertBatchMetrics(
            @Param("jobMetricsHistories") List<JobMetricsHistory> jobMetricsHistories);

    /** Query monitoring history based on homework instance ID */
    List<JobMetricsHistory> queryJobMetricsHistoryByInstanceId(
            @Param("jobInstanceId") Long jobInstanceId);

    /** Query monitoring history based on homework instance ID and pipeline ID */
    List<JobMetricsHistory> queryJobMetricsHistoryByInstanceIdAndPipelineId(
            @Param("jobInstanceId") Long jobInstanceId, @Param("pipelineId") Integer pipelineId);

    /** Query monitoring history based on homework instance ID and time range */
    List<JobMetricsHistory> queryJobMetricsHistoryByInstanceIdAndTimeRange(
            @Param("jobInstanceId") Long jobInstanceId,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);
}
