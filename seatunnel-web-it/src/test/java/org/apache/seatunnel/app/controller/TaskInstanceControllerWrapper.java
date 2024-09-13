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

package org.apache.seatunnel.app.controller;

import org.apache.seatunnel.shade.com.fasterxml.jackson.core.type.TypeReference;

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.common.SeatunnelWebTestingBase;
import org.apache.seatunnel.app.domain.dto.job.SeaTunnelJobInstanceDto;
import org.apache.seatunnel.app.utils.PageInfo;
import org.apache.seatunnel.common.constants.JobMode;
import org.apache.seatunnel.common.utils.JsonUtils;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskInstanceControllerWrapper extends SeatunnelWebTestingBase {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Result<PageInfo<SeaTunnelJobInstanceDto>> getTaskInstanceList(
            String taskName,
            String executorName,
            String stateType,
            String startTime,
            String endTime,
            JobMode jobMode,
            Integer pageNo,
            Integer pageSize) {
        String response =
                sendRequest(
                        urlWithParam(
                                "task/jobMetrics?taskName="
                                        + taskName
                                        + "&executorName="
                                        + executorName
                                        + "&stateType="
                                        + stateType
                                        + "&startDate="
                                        + startTime
                                        + "&endDate="
                                        + endTime
                                        + "&syncTaskType="
                                        + jobMode
                                        + "&pageNo="
                                        + pageNo
                                        + "&pageSize="
                                        + pageSize));
        return JsonUtils.parseObject(
                response, new TypeReference<Result<PageInfo<SeaTunnelJobInstanceDto>>>() {});
    }

    public List<SeaTunnelJobInstanceDto> getTaskInstanceList(String jobDefineName) {
        String startTime =
                URLEncoder.encode(
                        dateFormat.format(
                                new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)));
        String endTime =
                URLEncoder.encode(
                        dateFormat.format(
                                new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)));
        JobMode jobMode = JobMode.BATCH;
        Integer pageNo = 1;
        Integer pageSize = 10;
        Result<PageInfo<SeaTunnelJobInstanceDto>> result =
                getTaskInstanceList(
                        jobDefineName, null, null, startTime, endTime, jobMode, pageNo, pageSize);
        assertTrue(result.isSuccess());
        if (result.getData().getTotalList().isEmpty()) {
            return null;
        }
        return result.getData().getTotalList();
    }
}
