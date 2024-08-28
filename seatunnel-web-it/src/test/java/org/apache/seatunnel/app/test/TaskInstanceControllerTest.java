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
import org.apache.seatunnel.app.common.SeatunnelWebTestingBase;
import org.apache.seatunnel.app.controller.JobExecutorControllerWrapper;
import org.apache.seatunnel.app.controller.TaskInstanceControllerWrapper;
import org.apache.seatunnel.app.domain.dto.job.SeaTunnelJobInstanceDto;
import org.apache.seatunnel.app.domain.response.metrics.JobPipelineDetailMetricsRes;
import org.apache.seatunnel.app.utils.JobUtils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskInstanceControllerTest extends SeatunnelWebTestingBase {

    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static JobExecutorControllerWrapper jobExecutorControllerWrapper;
    private static TaskInstanceControllerWrapper taskInstanceControllerWrapper;
    private static final String uniqueId = "_" + System.currentTimeMillis();

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        jobExecutorControllerWrapper = new JobExecutorControllerWrapper();
        taskInstanceControllerWrapper = new TaskInstanceControllerWrapper();
    }

    @Test
    public void getTaskInstanceList_shouldReturnData_whenValidRequest() {
        String jobName = "getTaskInstance" + uniqueId;
        long jobVersionId = JobUtils.createJob(jobName);
        Result<Long> execuitonResult = jobExecutorControllerWrapper.jobExecutor(jobVersionId);
        assertTrue(execuitonResult.isSuccess());
        Result<List<JobPipelineDetailMetricsRes>> listResult =
                JobUtils.waitForJobCompletion(execuitonResult.getData());
        assertEquals(1, listResult.getData().size());
        assertEquals("FINISHED", listResult.getData().get(0).getStatus());

        SeaTunnelJobInstanceDto taskInstanceList =
                taskInstanceControllerWrapper.getTaskInstanceList(jobName);
        assertNotNull(taskInstanceList);
    }

    @AfterAll
    public static void tearDown() {
        seaTunnelWebCluster.stop();
    }
}
