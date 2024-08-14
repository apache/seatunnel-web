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
import org.apache.seatunnel.app.controller.JobConfigControllerWrapper;
import org.apache.seatunnel.app.controller.JobDefinitionControllerWrapper;
import org.apache.seatunnel.app.domain.request.job.JobConfig;
import org.apache.seatunnel.app.domain.response.job.JobConfigRes;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobConfigControllerTest {
    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static JobConfigControllerWrapper jobConfigControllerWrapper;
    private static JobDefinitionControllerWrapper jobDefinitionControllerWrapper;
    private static String uniqueId = "_" + System.currentTimeMillis();

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        jobConfigControllerWrapper = new JobConfigControllerWrapper();
        jobDefinitionControllerWrapper = new JobDefinitionControllerWrapper();
    }

    @Test
    public void updateJobConfig_shouldReturnSuccess_whenValidRequest() {
        String jobName = "config_job1" + uniqueId;
        updateConfig(jobName);
    }

    private static void updateConfig(String jobName) {
        long jobId = jobDefinitionControllerWrapper.createJobDefinition(jobName);
        JobConfig jobConfig = jobConfigControllerWrapper.populateJobConfigObject(jobName);
        Result<Void> result = jobConfigControllerWrapper.updateJobConfig(jobId, jobConfig);
        assertTrue(result.isSuccess());
    }

    @Test
    public void getJobConfig_shouldReturnData_whenValidRequest() {
        String jobName = "config_job2" + uniqueId;
        long jobId = jobDefinitionControllerWrapper.createJobDefinition(jobName);
        Result<JobConfigRes> result = jobConfigControllerWrapper.getJobConfig(jobId);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(jobName, result.getData().getName());
    }

    @AfterAll
    public static void tearDown() {
        seaTunnelWebCluster.stop();
    }
}
