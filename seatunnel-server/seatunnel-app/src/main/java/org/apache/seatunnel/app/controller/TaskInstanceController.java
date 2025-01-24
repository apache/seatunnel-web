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

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.domain.dto.job.SeaTunnelJobInstanceDto;
import org.apache.seatunnel.app.service.ITaskInstanceService;
import org.apache.seatunnel.app.utils.PageInfo;
import org.apache.seatunnel.common.constants.JobMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RequestMapping("/seatunnel/api/v1/task")
@RestController
public class TaskInstanceController {

    @Autowired ITaskInstanceService<SeaTunnelJobInstanceDto> taskInstanceService;

    @GetMapping("/jobMetrics")
    @ApiOperation(value = "get the jobMetrics list ", httpMethod = "GET")
    public Result<PageInfo<SeaTunnelJobInstanceDto>> getTaskInstanceList(
            @RequestParam(name = "taskName", required = false) String jobDefineName,
            @RequestParam(name = "executorName", required = false) String executorName,
            @RequestParam(name = "stateType", required = false) String stateType,
            @RequestParam(name = "startDate", required = false) String startTime,
            @RequestParam(name = "endDate", required = false) String endTime,
            @RequestParam("syncTaskType") JobMode jobMode,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("pageSize") Integer pageSize) {
        return taskInstanceService.getSyncTaskInstancePaging(
                jobDefineName,
                executorName,
                stateType,
                startTime,
                endTime,
                jobMode,
                pageNo,
                pageSize);
    }
}
