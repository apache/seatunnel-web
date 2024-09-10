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
import org.apache.seatunnel.app.domain.request.job.JobCreateReq;
import org.apache.seatunnel.app.domain.response.job.JobRes;
import org.apache.seatunnel.app.service.IJobService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.annotation.Resource;

@RestController
@RequestMapping("/seatunnel/api/v1/job")
public class JobController {

    @Resource private IJobService jobCRUDService;

    @PostMapping("/create")
    @ApiOperation(
            value =
                    "Create a job, In jobDAG for inputPluginId and targetPluginId use the plugin names instead of ids.",
            httpMethod = "POST")
    public Result<Long> createJob(
            @ApiParam(value = "userId", required = true) @RequestAttribute("userId") Integer userId,
            @RequestBody JobCreateReq jobCreateRequest)
            throws JsonProcessingException {
        return Result.success(jobCRUDService.createJob(userId, jobCreateRequest));
    }

    @PutMapping("/update/{jobVersionId}")
    @ApiOperation(
            value = "Update a job, all the existing ids should be passed in the request.",
            httpMethod = "PUT")
    public Result<Void> updateJob(
            @ApiParam(value = "userId", required = true) @RequestAttribute("userId") Integer userId,
            @ApiParam(value = "jobVersionId", required = true) @PathVariable long jobVersionId,
            @RequestBody JobCreateReq jobCreateReq)
            throws JsonProcessingException {
        jobCRUDService.updateJob(userId, jobVersionId, jobCreateReq);
        return Result.success();
    }

    @GetMapping("/get/{jobVersionId}")
    @ApiOperation(value = "Get a job detail.", httpMethod = "GET")
    public Result<JobRes> getJob(
            @ApiParam(value = "userId", required = true) @RequestAttribute("userId") Integer userId,
            @ApiParam(value = "jobVersionId", required = true) @PathVariable long jobVersionId)
            throws JsonProcessingException {
        JobRes jobRes = jobCRUDService.getJob(userId, jobVersionId);
        return Result.success(jobRes);
    }
}
