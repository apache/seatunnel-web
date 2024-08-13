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
import org.apache.seatunnel.app.domain.response.metrics.JobPipelineDetailMetricsRes;
import org.apache.seatunnel.app.utils.JobUtils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobExecutorControllerTest {
    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static JobExecutorControllerWrapper jobExecutorControllerWrapper;
    private static final String uniqueId = "_" + System.currentTimeMillis();

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        jobExecutorControllerWrapper = new JobExecutorControllerWrapper();
    }

    @Test
    public void executeJob_shouldReturnSuccess_whenValidRequest() {
        String jobName = "execJob" + uniqueId;
        long jobVersionId = JobUtils.createJob(jobName);
        Result<Long> result = jobExecutorControllerWrapper.jobExecutor(jobVersionId);
        assertTrue(result.isSuccess());
        assertTrue(result.getData() > 0);
        Result<List<JobPipelineDetailMetricsRes>> listResult =
                JobUtils.waitForJobCompletion(result.getData());
        assertEquals(1, listResult.getData().size());
        assertEquals("FINISHED", listResult.getData().get(0).getStatus());
        assertEquals(5, listResult.getData().get(0).getReadRowCount());
        assertEquals(5, listResult.getData().get(0).getWriteRowCount());
    }

    @Test
    public void restoreJob_shouldReturnSuccess_whenValidRequest() {
        String jobName = "jobRestore" + uniqueId;
        long jobVersionId = JobUtils.createJob(jobName);
        Result<Long> executorResult = jobExecutorControllerWrapper.jobExecutor(jobVersionId);
        assertTrue(executorResult.isSuccess());
        Result<Void> result = jobExecutorControllerWrapper.jobRestore(executorResult.getData());
        assertTrue(result.isSuccess());
    }

    @AfterAll
    public static void tearDown() {
        seaTunnelWebCluster.stop();
    }
}
