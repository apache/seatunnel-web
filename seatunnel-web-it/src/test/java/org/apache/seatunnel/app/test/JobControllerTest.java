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
import org.apache.seatunnel.app.domain.request.connector.SceneMode;
import org.apache.seatunnel.app.domain.request.job.Edge;
import org.apache.seatunnel.app.domain.request.job.JobConfig;
import org.apache.seatunnel.app.domain.request.job.JobCreateReq;
import org.apache.seatunnel.app.domain.request.job.JobDAG;
import org.apache.seatunnel.app.domain.request.job.PluginConfig;
import org.apache.seatunnel.app.domain.response.job.JobConfigRes;
import org.apache.seatunnel.app.domain.response.job.JobRes;
import org.apache.seatunnel.app.domain.response.metrics.JobPipelineDetailMetricsRes;
import org.apache.seatunnel.app.utils.JobTestingUtils;
import org.apache.seatunnel.common.constants.JobMode;
import org.apache.seatunnel.common.constants.PluginType;
import org.apache.seatunnel.engine.core.job.JobStatus;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobControllerTest {
    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static JobControllerWrapper jobControllerWrapper;
    private static JobExecutorControllerWrapper jobExecutorControllerWrapper;
    private static final String uniqueId = "_" + System.currentTimeMillis();

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        jobControllerWrapper = new JobControllerWrapper();
        jobExecutorControllerWrapper = new JobExecutorControllerWrapper();
    }

    @Test
    public void createJobWithSingleAPI_shouldExecuteSuccessfully() {
        String jobName = "jobWithSingleAPI" + uniqueId;
        JobCreateReq jobCreateReq =
                JobTestingUtils.populateJobCreateReqFromFile(
                        jobName, "fake_source_create" + uniqueId, "console_create" + uniqueId);
        Result<Long> job = jobControllerWrapper.createJob(jobCreateReq);
        assertTrue(job.isSuccess());
        Result<Long> result = jobExecutorControllerWrapper.jobExecutor(job.getData());
        assertTrue(result.isSuccess());
        assertTrue(result.getData() > 0);
        Result<List<JobPipelineDetailMetricsRes>> listResult =
                JobTestingUtils.waitForJobCompletion(result.getData());
        assertEquals(1, listResult.getData().size());
        assertEquals(JobStatus.FINISHED, listResult.getData().get(0).getStatus());
        assertEquals(5, listResult.getData().get(0).getReadRowCount());
        assertEquals(5, listResult.getData().get(0).getWriteRowCount());
    }

    @Test
    public void createJobWithSingleAPI_ValidateInput() {
        String jobName = "jobWithSingleAPI2" + uniqueId;
        JobCreateReq jobCreateReq =
                JobTestingUtils.populateJobCreateReqFromFile(
                        jobName, "fake_source_create_2" + uniqueId, "console_create_2" + uniqueId);
        JobConfig jobConfig = jobCreateReq.getJobConfig();
        jobConfig.setName("");
        Result<Long> result = jobControllerWrapper.createJob(jobCreateReq);
        assertTrue(result.isFailed());
        assertEquals(SeatunnelErrorEnum.PARAM_CAN_NOT_BE_NULL.getCode(), result.getCode());
        assertEquals("param [name] can not be null or empty", result.getMsg());

        jobName = "jobValidation" + uniqueId;
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
        assertEquals(SeatunnelErrorEnum.INVALID_PARAM.getCode(), result.getCode());
        assertEquals(
                "param [job.mode] is invalid. job.mode should be either BATCH or STREAMING",
                result.getMsg());

        jobConfig.getEnv().put("job.mode", "InvalidJobMode");
        result = jobControllerWrapper.createJob(jobCreateReq);
        assertTrue(result.isFailed());
        assertEquals(SeatunnelErrorEnum.INVALID_PARAM.getCode(), result.getCode());
        assertEquals(
                "param [job.mode] is invalid. job.mode should be either BATCH or STREAMING",
                result.getMsg());

        jobConfig.getEnv().put("job.mode", JobMode.BATCH);
        // setSourceIds(jobCreateReq, "fake_source_create2" + uniqueId, "console_create2" +
        // uniqueId);
        result = jobControllerWrapper.createJob(jobCreateReq);
        assertTrue(result.isSuccess());
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    public void testUpdateJob_ForValidAndInvalidScenarios() {
        String jobName = "updateJob_single_api" + uniqueId;
        JobCreateReq jobCreateReq =
                JobTestingUtils.populateJobCreateReqFromFile(
                        jobName, "fake_source_create_3" + uniqueId, "console_create_3" + uniqueId);

        Result<Long> job = jobControllerWrapper.createJob(jobCreateReq);
        assertTrue(job.isSuccess());

        Result<JobRes> getJobResponse = jobControllerWrapper.getJob(job.getData());
        assertTrue(getJobResponse.isSuccess());
        JobRes jobRes = getJobResponse.getData();
        assertNotNull(jobRes.getJobConfig());
        assertNotNull(jobRes.getJobConfig());
        assertNotNull(jobRes.getJobDAG());

        assertEquals(jobName, jobRes.getJobConfig().getName());
        assertEquals(
                jobCreateReq.getPluginConfigs().get(0).getName(),
                jobRes.getPluginConfigs().get(0).getName());
        assertEquals(
                jobCreateReq.getPluginConfigs().get(1).getName(),
                jobRes.getPluginConfigs().get(1).getName());

        JobCreateReq jobUpdateReq = convertJobResToJobCreateReq(jobRes);
        String jobName2 = "updateJob_single_api2" + uniqueId;
        jobUpdateReq.getJobConfig().setName(jobName2);
        jobUpdateReq.getJobConfig().setDescription(jobName2 + " description");

        Result<Void> jobUpdateResult = jobControllerWrapper.updateJob(job.getData(), jobUpdateReq);
        assertTrue(jobUpdateResult.isSuccess());

        Result<JobRes> getJobResponse2 = jobControllerWrapper.getJob(job.getData());
        assertTrue(getJobResponse2.isSuccess());
        JobRes jobRes2 = getJobResponse2.getData();
        assertEquals(jobName2, jobRes2.getJobConfig().getName());
        assertEquals(
                jobUpdateReq.getPluginConfigs().get(0).getName(),
                jobRes2.getPluginConfigs().get(0).getName());
        assertEquals(
                jobUpdateReq.getPluginConfigs().get(1).getName(),
                jobRes2.getPluginConfigs().get(1).getName());

        // Handle error scenarios

        // Invalid job instance id
        Result<JobRes> invalidJobInstanceIdResponse = jobControllerWrapper.getJob(123L);
        assertFalse(invalidJobInstanceIdResponse.isSuccess());
        assertEquals(
                SeatunnelErrorEnum.RESOURCE_NOT_FOUND.getCode(),
                invalidJobInstanceIdResponse.getCode());

        Result<Void> result = jobControllerWrapper.updateJob(123L, jobUpdateReq);
        assertFalse(result.isSuccess());
        assertEquals(SeatunnelErrorEnum.RESOURCE_NOT_FOUND.getCode(), result.getCode());

        // While doing job update some configuration is wrong.
        jobUpdateReq.getJobDAG().getEdges().get(0).setInputPluginId("InvalidInputPluginId");
        jobUpdateReq.getJobDAG().getEdges().get(0).setTargetPluginId("InvalidTargetPluginId");
        jobUpdateResult = jobControllerWrapper.updateJob(job.getData(), jobUpdateReq);
        assertFalse(jobUpdateResult.isSuccess());
        assertEquals(SeatunnelErrorEnum.ERROR_CONFIG.getCode(), jobUpdateResult.getCode());
    }

    @Test
    public void testUpdateJob_AddNewTask() {
        String jobName = "updateJob_single_api_add_task" + uniqueId;
        JobCreateReq jobCreateReq =
                JobTestingUtils.populateJobCreateReqFromFile(
                        jobName, "fake_source_create_4" + uniqueId, "console_create_4" + uniqueId);

        Result<Long> job = jobControllerWrapper.createJob(jobCreateReq);
        assertTrue(job.isSuccess());
        JobTestingUtils.executeJobAndVerifySuccess(job.getData());

        Result<JobRes> getJobResponse = jobControllerWrapper.getJob(job.getData());
        assertTrue(getJobResponse.isSuccess());
        JobRes jobRes = getJobResponse.getData();

        JobCreateReq jobUpdateReq = convertJobResToJobCreateReq(jobRes);
        jobUpdateReq.getPluginConfigs().add(getCopyTransformPlugin());

        List<Edge> edges = new ArrayList<>();
        edges.add(new Edge("source-fake-source", "transform-replace"));
        edges.add(new Edge("transform-replace", "transform-copy"));
        edges.add(new Edge("transform-copy", "sink-console"));
        JobDAG jobDAG = new JobDAG(edges);
        jobUpdateReq.setJobDAG(jobDAG);
        String jobName2 = "updateJob_single_api_add_task_up" + uniqueId;
        jobUpdateReq.getJobConfig().setName(jobName2);
        jobUpdateReq.getJobConfig().setDescription(jobName2 + " description");

        Result<Void> jobUpdateResult = jobControllerWrapper.updateJob(job.getData(), jobUpdateReq);
        assertTrue(jobUpdateResult.isSuccess());
        JobTestingUtils.executeJobAndVerifySuccess(job.getData());
    }

    @Test
    public void testUpdateJob_RemoveTask() {
        String jobName = "updateJob_single_api_remove_task" + uniqueId;
        JobCreateReq jobCreateReq =
                JobTestingUtils.populateJobCreateReqFromFile(
                        jobName, "fake_source_create_5" + uniqueId, "console_create_5" + uniqueId);

        Result<Long> job = jobControllerWrapper.createJob(jobCreateReq);
        assertTrue(job.isSuccess());
        JobTestingUtils.executeJobAndVerifySuccess(job.getData());

        Result<JobRes> getJobResponse = jobControllerWrapper.getJob(job.getData());
        assertTrue(getJobResponse.isSuccess());
        JobRes jobRes = getJobResponse.getData();

        JobCreateReq jobUpdateReq = convertJobResToJobCreateReq(jobRes);
        jobUpdateReq
                .getPluginConfigs()
                .removeIf(pluginConfig -> "transform-replace".equals(pluginConfig.getName()));

        List<Edge> edges = new ArrayList<>();
        edges.add(new Edge("source-fake-source", "sink-console"));
        JobDAG jobDAG = new JobDAG(edges);
        jobUpdateReq.setJobDAG(jobDAG);

        Result<Void> jobUpdateResult = jobControllerWrapper.updateJob(job.getData(), jobUpdateReq);
        assertTrue(jobUpdateResult.isSuccess());
        JobTestingUtils.executeJobAndVerifySuccess(job.getData());
    }

    private PluginConfig getCopyTransformPlugin() {
        String transPluginId = "copy" + System.currentTimeMillis();
        Map<String, Object> transOptions = new HashMap<>();
        transOptions.put(
                "copyList",
                Arrays.asList(
                        new HashMap<String, String>() {
                            {
                                put("sourceFieldName", "name");
                                put("targetFieldName", "name_copy");
                            }
                        }));
        return PluginConfig.builder()
                .pluginId(transPluginId)
                .name("transform-copy")
                .type(PluginType.TRANSFORM)
                .connectorType("Copy")
                .transformOptions(transOptions)
                .outputSchema(null)
                .sceneMode(SceneMode.SINGLE_TABLE)
                .config("{\"query\":\"\"}")
                .build();
    }

    private JobCreateReq convertJobResToJobCreateReq(JobRes jobRes) {
        JobCreateReq jobCreateReq = new JobCreateReq();

        // Assuming JobRes contains JobConfigRes and List<PluginConfig> and JobDAG
        JobConfigRes jobConfigRes = jobRes.getJobConfig();
        List<PluginConfig> pluginConfigs = jobRes.getPluginConfigs();
        JobDAG jobDAG = jobRes.getJobDAG();

        // Populate JobCreateReq with data from JobRes
        JobConfig jobConfig = new JobConfig();
        jobConfig.setName(jobConfigRes.getName());
        jobConfig.setDescription(jobConfigRes.getDescription());
        jobConfig.setEnv(jobConfigRes.getEnv());
        jobConfig.setEngine(jobConfigRes.getEngine());

        jobCreateReq.setJobConfig(jobConfig);
        jobCreateReq.setPluginConfigs(pluginConfigs);
        jobCreateReq.setJobDAG(jobDAG);

        return jobCreateReq;
    }

    @AfterAll
    public static void tearDown() {
        seaTunnelWebCluster.stop();
    }
}
