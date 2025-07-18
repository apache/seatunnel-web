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

package org.apache.seatunnel.app.scheduler;

import org.apache.seatunnel.shade.com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.apache.seatunnel.app.dal.dao.IJobInstanceDao;
import org.apache.seatunnel.app.dal.dao.IJobMetricsHistoryDao;
import org.apache.seatunnel.app.dal.entity.JobInstance;
import org.apache.seatunnel.app.dal.entity.JobMetricsHistory;
import org.apache.seatunnel.app.domain.response.metrics.JobPipelineDetailMetricsRes;
import org.apache.seatunnel.app.service.IJobMetricsService;
import org.apache.seatunnel.engine.core.job.JobStatus;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MonitorTaskScheduler {

    private final ExecutorService executorService;

    @Resource private IJobInstanceDao jobInstanceDao;

    @Resource private IJobMetricsService jobMetricsService;

    @Resource private IJobMetricsHistoryDao jobMetricsHistoryDao;

    private final ConcurrentHashMap<Long, JobInstance> jobInstanceMap = new ConcurrentHashMap<>();

    private final Object mapLock = new Object();

    public MonitorTaskScheduler() {
        // Create thread pool
        this.executorService =
                new ThreadPoolExecutor(
                        5,
                        10,
                        60L,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(100),
                        new ThreadFactoryBuilder()
                                .setNameFormat("task-processor-%d")
                                .setUncaughtExceptionHandler(
                                        (t, e) ->
                                                log.error(
                                                        "Thread {} encountered uncaught exception",
                                                        t.getName(),
                                                        e))
                                .build(),
                        new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Scheduled(initialDelay = 0, fixedRate = 60000)
    public void updateJobInstance() {
        try {
            log.info("Start updating job instance information...");
            List<JobInstance> allJobInstance = jobInstanceDao.getAllRunningJobInstance();

            Map<Long, JobInstance> newInstanceMap =
                    allJobInstance.stream()
                            .collect(
                                    Collectors.toMap(
                                            JobInstance::getId,
                                            instance -> instance,
                                            (existing, replacement) -> replacement));

            synchronized (mapLock) {
                jobInstanceMap.clear();
                jobInstanceMap.putAll(newInstanceMap);
            }

            log.debug(
                    "Job instance information updated, current total instances: {}",
                    jobInstanceMap.size());
        } catch (Exception e) {
            log.error("Error updating job instance information", e);
        }
    }

    public JobInstance getJobInstance(Long jobInstanceId) {
        synchronized (mapLock) {
            return jobInstanceMap.get(jobInstanceId);
        }
    }

    public List<JobInstance> getAllJobInstances() {
        synchronized (mapLock) {
            return new ArrayList<>(jobInstanceMap.values());
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void scheduleTasks() {
        List<JobInstance> instances;
        synchronized (mapLock) {
            instances = new ArrayList<>(jobInstanceMap.values());
        }

        instances.forEach(
                jobInstance -> {
                    if (jobInstance.getJobStatus() != JobStatus.RUNNING) {
                        return;
                    }
                    try {
                        executorService.submit(
                                () -> {
                                    try {
                                        Long jobInstanceId = jobInstance.getId();
                                        List<JobPipelineDetailMetricsRes> metricsResList =
                                                jobMetricsService.getJobPipelineDetailMetricsRes(
                                                        jobInstance);

                                        if (metricsResList != null && !metricsResList.isEmpty()) {
                                            List<JobMetricsHistory> historyList =
                                                    metricsResList.stream()
                                                            .map(
                                                                    metrics ->
                                                                            convertToJobMetricsHistory(
                                                                                    metrics,
                                                                                    jobInstanceId))
                                                            .collect(Collectors.toList());

                                            jobMetricsHistoryDao.insertBatch(historyList);
                                            log.debug(
                                                    "Successfully saved metrics for job {}, total {} records",
                                                    jobInstanceId,
                                                    historyList.size());
                                        }
                                    } catch (Exception e) {
                                        log.error("Error saving job metrics", e);
                                    }
                                });
                    } catch (Exception e) {
                        log.error("Task scheduling error", e);
                    }
                });
    }

    private JobMetricsHistory convertToJobMetricsHistory(
            JobPipelineDetailMetricsRes metrics, Long jobInstanceId) {
        return JobMetricsHistory.builder()
                .id(generateId())
                .jobInstanceId(jobInstanceId)
                .pipelineId(metrics.getPipelineId())
                .readRowCount(metrics.getReadRowCount())
                .writeRowCount(metrics.getWriteRowCount())
                .sourceTableNames(metrics.getSourceTableNames())
                .sinkTableNames(metrics.getSinkTableNames())
                .readQps(metrics.getReadQps())
                .writeQps(metrics.getWriteQps())
                .recordDelay(metrics.getRecordDelay())
                .status(metrics.getStatus())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .createUserId(-1)
                .updateUserId(-1)
                .build();
    }

    private Long generateId() {
        // Here you can use a distributed ID generator, such as Snowflake algorithm
        return System.currentTimeMillis();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down task scheduler...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
