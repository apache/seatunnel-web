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

package org.apache.seatunnel.app.service;

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.domain.dto.job.SeaTunnelJobInstanceDto;
import org.apache.seatunnel.app.domain.response.executor.JobExecutionStatus;
import org.apache.seatunnel.app.utils.PageInfo;
import org.apache.seatunnel.common.constants.JobMode;

public interface ITaskInstanceService<T> {

    Result<PageInfo<T>> getSyncTaskInstancePaging(
            String jobDefineName,
            String executorName,
            String stateType,
            String startTime,
            String endTime,
            JobMode jobMode,
            Integer pageNo,
            Integer pageSize);

    Result<JobExecutionStatus> getJobExecutionStatus(long jobInstanceId);

    Result<SeaTunnelJobInstanceDto> getJobExecutionDetail(long jobInstanceId);

    Result<Void> deleteJobInstanceById(long jobInstanceId);
}
