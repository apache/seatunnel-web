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
import org.apache.seatunnel.app.service.IJobExecutorService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import java.io.IOException;

@Slf4j
@RequestMapping("/seatunnel/api/v1/job/executor")
@RestController
public class JobExecutorController {

    @Resource IJobExecutorService jobExecutorService;

    @PostMapping("/create")
    @ApiOperation(value = "Create synchronization tasks", httpMethod = "POST")
    public Result<Long> jobCreate(
            @ApiParam(value = "userId", required = true) @RequestAttribute("userId") Integer userId,
            @ApiParam(value = "jobDefineId", required = true) @RequestParam("jobDefineId")
            Long jobDefineId) {
        return jobExecutorService.jobCreate(userId, jobDefineId);
    }

    @PostMapping("/execute")
    @ApiOperation(value = "Execute synchronization tasks", httpMethod = "Post")
    public Result<Long> jobExecutor(
            @ApiParam(value = "userId", required = true) @RequestAttribute("userId") Integer userId,
            @ApiParam(value = "jobInstanceId", required = true) @RequestParam Long jobInstanceId) {
        return jobExecutorService.jobExecute(userId, jobInstanceId);
    }

    @PostMapping("/pause")
    @ApiOperation(value = "Pause synchronization tasks", httpMethod = "POST")
    public Result<Void> jobPause(
            @ApiParam(value = "userId", required = true) @RequestAttribute("userId") Integer userId,
            @ApiParam(value = "jobInstanceId", required = true) @RequestParam Long jobInstanceId) {
        return jobExecutorService.jobPause(userId, jobInstanceId);
    }

    @GetMapping("/restore")
    public Result<Void> jobRestore(
            @ApiParam(value = "userId", required = true) @RequestAttribute("userId") Integer userId,
            @ApiParam(value = "jobInstanceId", required = true) @RequestParam Long jobInstanceId) {
        return jobExecutorService.jobStore(userId, jobInstanceId);
    }

    @DeleteMapping("/delete")
    @ApiOperation(value = "Delete synchronization tasks", httpMethod = "DELETE")
    public Result<Void> jobCancel(
            @ApiParam(value = "userId", required = true) @RequestAttribute("userId") Integer userId,
            @ApiParam(value = "jobInstanceId", required = true) @RequestParam Long jobInstanceId) {
        return jobExecutorService.jobDelete(userId, jobInstanceId);
    }
}
