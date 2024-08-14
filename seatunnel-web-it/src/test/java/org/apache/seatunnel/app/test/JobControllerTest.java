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
import org.apache.seatunnel.app.controller.JobControllerWrapper;
import org.apache.seatunnel.app.controller.JobExecutorControllerWrapper;
import org.apache.seatunnel.app.controller.SeatunnelDatasourceControllerWrapper;
import org.apache.seatunnel.app.domain.request.job.JobConfig;
import org.apache.seatunnel.app.domain.request.job.JobCreateReq;
import org.apache.seatunnel.app.domain.request.job.PluginConfig;
import org.apache.seatunnel.app.domain.response.metrics.JobPipelineDetailMetricsRes;
import org.apache.seatunnel.app.utils.JobUtils;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobControllerTest {
    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static SeatunnelDatasourceControllerWrapper seatunnelDatasourceControllerWrapper;
    private static JobControllerWrapper jobControllerWrapper;
    private static JobExecutorControllerWrapper jobExecutorControllerWrapper;
    private static final String uniqueId = "_" + System.currentTimeMillis();

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        seatunnelDatasourceControllerWrapper = new SeatunnelDatasourceControllerWrapper();
        jobControllerWrapper = new JobControllerWrapper();
        jobExecutorControllerWrapper = new JobExecutorControllerWrapper();
    }

    @Test
    public void createJobWithSingleAPI_shouldExecuteSuccessfully() {
        String jobName = "jobWithSingleAPI" + uniqueId;
        JobCreateReq jobCreateReq = jobControllerWrapper.populateJobCreateReqFromFile();
        jobCreateReq.getJobConfig().setName(jobName);
        jobCreateReq.getJobConfig().setDescription(jobName + " description");
        setSourceIds(jobCreateReq, "fake_source_ds1" + uniqueId, "console_ds1" + uniqueId);

        Result<Long> job = jobControllerWrapper.createJob(jobCreateReq);
        assertTrue(job.isSuccess());
        Result<Long> result = jobExecutorControllerWrapper.jobExecutor(job.getData());
        assertTrue(result.isSuccess());
        assertTrue(result.getData() > 0);
        Result<List<JobPipelineDetailMetricsRes>> listResult =
                JobUtils.waitForJobCompletion(result.getData());
        assertEquals(1, listResult.getData().size());
        assertEquals("FINISHED", listResult.getData().get(0).getStatus());
        assertEquals(5, listResult.getData().get(0).getReadRowCount());
        assertEquals(5, listResult.getData().get(0).getWriteRowCount());
    }

    private void setSourceIds(
            JobCreateReq jobCreateReq, String fsdSourceName, String csSourceName) {
        // Set the data source id for the plugin configs
        String fakeSourceDataSourceId =
                seatunnelDatasourceControllerWrapper.createFakeSourceDatasource(fsdSourceName);
        String consoleDataSourceId =
                seatunnelDatasourceControllerWrapper.createConsoleDatasource(csSourceName);
        for (PluginConfig pluginConfig : jobCreateReq.getPluginConfigs()) {
            if (pluginConfig.getName().equals("source-fake-source")) {
                pluginConfig.setDataSourceId(Long.parseLong(fakeSourceDataSourceId));
            } else if (pluginConfig.getName().equals("sink-console")) {
                pluginConfig.setDataSourceId(Long.parseLong(consoleDataSourceId));
            }
        }
    }

    @Test
    public void createJobWithSingleAPI_ValidateInput() {
        JobCreateReq jobCreateReq = jobControllerWrapper.populateJobCreateReqFromFile();
        JobConfig jobConfig = jobCreateReq.getJobConfig();
        jobConfig.setName("");
        Result<Long> result = jobControllerWrapper.createJob(jobCreateReq);
        assertTrue(result.isFailed());
        assertEquals(SeatunnelErrorEnum.PARAM_CAN_NOT_BE_NULL.getCode(), result.getCode());
        assertEquals("param [name] can not be null or empty", result.getMsg());

        String jobName = "jobValidation" + uniqueId;
        jobConfig.setName(jobName);
        jobConfig.setDescription(null);
        result = jobControllerWrapper.createJob(jobCreateReq);
        assertTrue(result.isFailed());
        assertEquals(SeatunnelErrorEnum.PARAM_CAN_NOT_BE_NULL.getCode(), result.getCode());
        assertEquals("param [description] can not be null or empty", result.getMsg());

        jobConfig.setDescription(jobName + " description");
        jobConfig.getEnv().put("job.mode", "");
        result = jobControllerWrapper.createJob(jobCreateReq);
        assertTrue(result.isFailed());
        assertEquals(SeatunnelErrorEnum.PARAM_CAN_NOT_BE_NULL.getCode(), result.getCode());
        assertEquals("param [job.mode] can not be null or empty", result.getMsg());

        jobConfig.getEnv().put("job.mode", "InvalidJobMode");
        result = jobControllerWrapper.createJob(jobCreateReq);
        assertTrue(result.isFailed());
        assertEquals(SeatunnelErrorEnum.INVALID_PARAM.getCode(), result.getCode());
        assertEquals(
                "param [job.mode] is invalid. job.mode should be either BATCH or STREAM",
                result.getMsg());

        jobConfig.getEnv().put("job.mode", "BATCH");
        setSourceIds(jobCreateReq, "fake_source_ds2" + uniqueId, "console_ds2" + uniqueId);
        result = jobControllerWrapper.createJob(jobCreateReq);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
    }

    @AfterAll
    public static void tearDown() {
        seaTunnelWebCluster.stop();
    }
}
