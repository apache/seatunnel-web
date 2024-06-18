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

package org.apache.seatunnel.app.service.impl;

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.dal.dao.IJobInstanceDao;
import org.apache.seatunnel.app.dal.entity.JobInstance;
import org.apache.seatunnel.app.domain.response.engine.Engine;
import org.apache.seatunnel.app.service.IJobExecutorService;
import org.apache.seatunnel.app.service.IJobInstanceService;
import org.apache.seatunnel.app.thirdparty.engine.SeaTunnelEngineProxy;
import org.apache.seatunnel.app.thirdparty.metrics.EngineMetricsExtractorFactory;
import org.apache.seatunnel.app.thirdparty.metrics.IEngineMetricsExtractor;
import org.apache.seatunnel.engine.client.job.ClientJobProxy;
import org.apache.seatunnel.engine.core.job.JobResult;
import org.apache.seatunnel.engine.core.job.JobStatus;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 这是一个使用 SeaTunnel 引擎的 JobExecutor 实现类，其负责 JobInstance 的生命周期，
 * 以及 JobInstance 的任务监控和管理。
 */
@Slf4j
@Service
public class JobExecutorServiceImpl implements IJobExecutorService {
    @Resource
    private IJobInstanceService jobInstanceService;

    @Override
    public Result<Long> jobCreate(Integer userId, Long jobDefineId) {
        return Result.success(jobInstanceService.create(userId, jobDefineId));
    }

    @Override
    public Result<Long> jobExecute(Integer userId, Long jobInstanceId) {
        if (JobStatus.CREATED.equals(jobInstanceService.status(userId, jobInstanceId))) {
            CompletableFuture.runAsync(() -> jobInstanceService.start(userId, jobInstanceId));
        }
        return Result.success(jobInstanceId);
    }

    @Override
    public Result<Void> jobPause(Integer userId, Long jobInstanceId) {
        if (!jobInstanceService.status(userId, jobInstanceId).isEndState()) {
            jobInstanceService.pause(userId, jobInstanceId);
        }
        return Result.success();
    }

    @Override
    public Result<Void> jobStore(Integer userId, Long jobInstanceId) {
        if (jobInstanceService.status(userId, jobInstanceId).isEndState()) {
            jobInstanceService.restore(userId, jobInstanceId);
        }
        return Result.success();
    }

    @Override
    public Result<Void> jobDelete(Integer userId, Long jobInstanceId) {
        if (jobInstanceService.status(userId, jobInstanceId).isEndState()) {
            jobInstanceService.delete(userId, jobInstanceId);
        }
        return Result.success();
    }
}
