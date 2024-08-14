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
package org.apache.seatunnel.app.test;

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.common.SeaTunnelWebCluster;
import org.apache.seatunnel.app.controller.JobExecutorControllerWrapper;
import org.apache.seatunnel.app.controller.JobMetricsControllerWrapper;
import org.apache.seatunnel.app.domain.response.metrics.JobDAG;
import org.apache.seatunnel.app.domain.response.metrics.JobPipelineDetailMetricsRes;
import org.apache.seatunnel.app.utils.JobUtils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobMetricsControllerTest {
    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static JobMetricsControllerWrapper jobMetricsControllerWrapper;
    private static JobExecutorControllerWrapper jobExecutorControllerWrapper;
    private static final String uniqueId = "_" + System.currentTimeMillis();

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        jobMetricsControllerWrapper = new JobMetricsControllerWrapper();
        jobExecutorControllerWrapper = new JobExecutorControllerWrapper();
    }

    @Test
    public void detailMetrics_shouldReturnData_whenValidRequest() {
        String jobName = "jobDetail" + uniqueId;
        long jobInstanceId = executeJob(jobName);
        Result<List<JobPipelineDetailMetricsRes>> result =
                jobMetricsControllerWrapper.detail(jobInstanceId);
        assertTrue(result.isSuccess());
        assertFalse(result.getData().isEmpty());
    }

    private static Long executeJob(String jobName) {
        Long jobVersionId = JobUtils.createJob(jobName);
        Result<Long> jobExecutionResult = jobExecutorControllerWrapper.jobExecutor(jobVersionId);
        assertTrue(jobExecutionResult.isSuccess());
        return jobExecutionResult.getData();
    }

    @Test
    public void getJobDAG_shouldReturnData_whenValidRequest() {
        String jobName = "jobDAG" + uniqueId;
        long jobInstanceId = executeJob(jobName);
        Result<JobDAG> result = jobMetricsControllerWrapper.getJobDAG(jobInstanceId);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    public void summaryMetrics_shouldReturnData_whenValidRequest() {
        String jobName = "jobSummary" + uniqueId;
        long jobInstanceId = executeJob(jobName);
        Result<Void> result = jobMetricsControllerWrapper.summary(jobInstanceId);
        assertTrue(result.isSuccess());
    }

    @AfterAll
    public static void tearDown() {
        seaTunnelWebCluster.stop();
    }
}
