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
package org.apache.seatunnel.app.utils;

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.controller.JobConfigControllerWrapper;
import org.apache.seatunnel.app.controller.JobDefinitionControllerWrapper;
import org.apache.seatunnel.app.controller.JobMetricsControllerWrapper;
import org.apache.seatunnel.app.controller.JobTaskControllerWrapper;
import org.apache.seatunnel.app.controller.SeatunnelDatasourceControllerWrapper;
import org.apache.seatunnel.app.domain.request.job.Edge;
import org.apache.seatunnel.app.domain.request.job.JobConfig;
import org.apache.seatunnel.app.domain.request.job.JobCreateReq;
import org.apache.seatunnel.app.domain.request.job.JobDAG;
import org.apache.seatunnel.app.domain.response.job.JobTaskCheckRes;
import org.apache.seatunnel.app.domain.response.metrics.JobPipelineDetailMetricsRes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobUtils {
    private static JobMetricsControllerWrapper jobMetricsControllerWrapper =
            new JobMetricsControllerWrapper();
    private static JobConfigControllerWrapper jobConfigControllerWrapper =
            new JobConfigControllerWrapper();
    private static JobDefinitionControllerWrapper jobDefinitionControllerWrapper =
            new JobDefinitionControllerWrapper();
    private static JobTaskControllerWrapper jobTaskControllerWrapper =
            new JobTaskControllerWrapper();
    private static SeatunnelDatasourceControllerWrapper seatunnelDatasourceControllerWrapper =
            new SeatunnelDatasourceControllerWrapper();
    private static final long TIMEOUT = 60; // 1 minute
    private static final long INTERVAL = 2; // 1 second

    public static Result<List<JobPipelineDetailMetricsRes>> waitForJobCompletion(
            long jobInstanceId) {
        return waitForJobCompletion(jobInstanceId, TIMEOUT, INTERVAL);
    }

    public static Result<List<JobPipelineDetailMetricsRes>> waitForJobCompletion(
            long jobInstanceId, long timeout, long interval) {
        long startTime = System.currentTimeMillis();
        while (true) {
            Result<List<JobPipelineDetailMetricsRes>> detail =
                    jobMetricsControllerWrapper.detail(jobInstanceId);
            if (!detail.isSuccess()) {
                throw new RuntimeException("Failed to get job detail metrics");
            }
            if (isAllFinished(detail.getData())) {
                return detail;
            }
            if (System.currentTimeMillis() - startTime > TimeUnit.SECONDS.toMillis(timeout)) {
                throw new RuntimeException(
                        "Timeout waiting for job to complete. Job not completed in "
                                + timeout
                                + " seconds.");
            }
            try {
                TimeUnit.SECONDS.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(
                        "Thread was interrupted while waiting for job completion", e);
            }
        }
    }

    private static boolean isAllFinished(List<JobPipelineDetailMetricsRes> details) {
        for (JobPipelineDetailMetricsRes metrics : details) {
            if (!isFinished(metrics)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFinished(JobPipelineDetailMetricsRes metrics) {
        if (metrics == null || metrics.getStatus() == null) {
            return false;
        }
        switch (metrics.getStatus()) {
            case "FINISHED":
            case "CANCELED":
            case "FAILED":
                return true;
            default:
                return false;
        }
    }

    public static Long createJob(String jobName) {
        Long jobId = jobDefinitionControllerWrapper.createJobDefinition(jobName);
        JobConfig jobConfig = jobConfigControllerWrapper.populateJobConfigObject(jobName);
        // jobVersionId is same as jobId
        long jobVersionId = jobId;

        Result<Void> jobConfigResult =
                jobConfigControllerWrapper.updateJobConfig(jobVersionId, jobConfig);
        assertTrue(jobConfigResult.isSuccess());

        String fakeSourceDatasourceId =
                seatunnelDatasourceControllerWrapper.createFakeSourceDatasource(
                        "source_" + jobName);
        String consoleDatasourceId =
                seatunnelDatasourceControllerWrapper.createConsoleDatasource("console_" + jobName);

        String sourcePluginId =
                jobTaskControllerWrapper.createFakeSourcePlugin(
                        fakeSourceDatasourceId, jobVersionId);
        String transPluginId = jobTaskControllerWrapper.createReplaceTransformPlugin(jobVersionId);
        String sinkPluginId =
                jobTaskControllerWrapper.createConsoleSinkPlugin(consoleDatasourceId, jobVersionId);

        JobDAG jobDAG = new JobDAG();
        List<Edge> edges = new ArrayList<>();
        edges.add(new Edge(sourcePluginId, transPluginId));
        edges.add(new Edge(transPluginId, sinkPluginId));
        jobDAG.setEdges(edges);

        Result<JobTaskCheckRes> jobTaskCheckResResult =
                jobTaskControllerWrapper.saveJobDAG(jobVersionId, jobDAG);
        assertTrue(jobTaskCheckResResult.isSuccess());
        return jobVersionId;
    }

    public static JobCreateReq populateMySQLJobCreateReqFromFile() {
        String filePath = "src/test/resources/jobs/mysql_source_mysql_sink.json";
        String jsonContent;
        try {
            jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return JSONTestUtils.parseObject(jsonContent, JobCreateReq.class);
    }
}
