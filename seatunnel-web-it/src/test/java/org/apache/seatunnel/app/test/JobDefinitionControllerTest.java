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
import org.apache.seatunnel.app.controller.JobDefinitionControllerWrapper;
import org.apache.seatunnel.app.domain.response.PageInfo;
import org.apache.seatunnel.app.domain.response.job.JobDefinitionRes;
import org.apache.seatunnel.common.constants.JobMode;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobDefinitionControllerTest {
    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static JobDefinitionControllerWrapper jobDefinitionControllerWrapper;
    private static final String uniqueId = "_" + System.currentTimeMillis();

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        jobDefinitionControllerWrapper = new JobDefinitionControllerWrapper();
    }

    @Test
    public void createJobDefinition_shouldReturnSuccess_whenValidRequest() {
        long jobId = jobDefinitionControllerWrapper.createJobDefinition("job1" + uniqueId);
        assertTrue(jobId > 0);
    }

    @Test
    public void getJobDefinitionById_shouldReturnData_whenValidRequest() {
        String job2 = "job2" + uniqueId;
        long jobId = jobDefinitionControllerWrapper.createJobDefinition(job2);
        Result<JobDefinitionRes> result =
                jobDefinitionControllerWrapper.getJobDefinitionById(jobId);
        assertTrue(result.isSuccess());
        assertEquals(job2, result.getData().getName());
    }

    @Test
    public void getJobDefinition_shouldReturnData_whenValidRequest() {
        String job3 = "job3" + uniqueId;
        long jobId = jobDefinitionControllerWrapper.createJobDefinition(job3);
        Result<PageInfo<JobDefinitionRes>> result =
                jobDefinitionControllerWrapper.getJobDefinition(job3, 1, 10, JobMode.BATCH.name());
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().getData().size());
        assertEquals(jobId, result.getData().getData().get(0).getId());

        result =
                jobDefinitionControllerWrapper.getJobDefinition(
                        job3, 1, 10, JobMode.STREAMING.name());
        assertTrue(result.isSuccess());
        assertEquals(0, result.getData().getData().size());
    }

    @Test
    public void deleteJobDefinition_shouldReturnSuccess_whenValidId() {
        String job7 = "job7" + uniqueId;
        long jobId = jobDefinitionControllerWrapper.createJobDefinition(job7);
        Result<Void> result = jobDefinitionControllerWrapper.deleteJobDefinition(jobId);
        assertTrue(result.isSuccess());
    }

    @AfterAll
    public static void tearDown() {
        seaTunnelWebCluster.stop();
    }
}
