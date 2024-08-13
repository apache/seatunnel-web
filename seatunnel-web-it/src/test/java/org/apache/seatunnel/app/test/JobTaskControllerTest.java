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
import org.apache.seatunnel.app.controller.JobTaskControllerWrapper;
import org.apache.seatunnel.app.controller.SeatunnelDatasourceControllerWrapper;
import org.apache.seatunnel.app.domain.request.job.Edge;
import org.apache.seatunnel.app.domain.request.job.JobConfig;
import org.apache.seatunnel.app.domain.request.job.JobDAG;
import org.apache.seatunnel.app.domain.request.job.JobTaskInfo;
import org.apache.seatunnel.app.domain.request.job.PluginConfig;
import org.apache.seatunnel.app.domain.response.job.JobTaskCheckRes;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobTaskControllerTest {
    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static JobTaskControllerWrapper jobTaskControllerWrapper;
    private static JobDefinitionControllerWrapper jobDefinitionControllerWrapper;
    private static SeatunnelDatasourceControllerWrapper seatunnelDatasourceControllerWrapper;
    private static JobConfigControllerWrapper jobConfigControllerWrapper;
    private static String uniqueId = "_" + System.currentTimeMillis();

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        jobTaskControllerWrapper = new JobTaskControllerWrapper();
        jobDefinitionControllerWrapper = new JobDefinitionControllerWrapper();
        seatunnelDatasourceControllerWrapper = new SeatunnelDatasourceControllerWrapper();
        jobConfigControllerWrapper = new JobConfigControllerWrapper();
    }

    @Test
    public void getJob_shouldReturnData_whenValidRequest() {
        long jobId = jobDefinitionControllerWrapper.createJobDefinition("task_job1" + uniqueId);
        Result<JobTaskInfo> result = jobTaskControllerWrapper.getJob(jobId);
        assertTrue(result.isSuccess());
    }

    @Test
    public void saveSingleTask_shouldReturnSuccess_whenValidRequest() {
        String jobName = "task_job2" + uniqueId;
        long jobId = jobDefinitionControllerWrapper.createJobDefinition(jobName);
        String sourceDatasourceId =
                seatunnelDatasourceControllerWrapper.createFakeSourceDatasource(
                        "task_db2_source" + uniqueId);
        String sinkDataSourceId =
                seatunnelDatasourceControllerWrapper.createConsoleDatasource(
                        "task_db2_sink" + uniqueId);
        String sourcePluginId =
                jobTaskControllerWrapper.createFakeSourcePlugin(sourceDatasourceId, jobId);
        String sinkPluginId =
                jobTaskControllerWrapper.createConsoleSinkPlugin(sinkDataSourceId, jobId);
        String transPluginId = jobTaskControllerWrapper.createReplaceTransformPlugin(jobId);

        JobConfig jobConfig = jobConfigControllerWrapper.populateJobConfigObject(jobName);
        jobConfigControllerWrapper.updateJobConfig(jobId, jobConfig);

        JobDAG jobDAG = new JobDAG();
        List<Edge> edges = new ArrayList<>();
        edges.add(new Edge(sourcePluginId, transPluginId));
        edges.add(new Edge(transPluginId, sinkPluginId));
        jobDAG.setEdges(edges);

        Result<JobTaskCheckRes> dagResult = jobTaskControllerWrapper.saveJobDAG(jobId, jobDAG);
        assertTrue(dagResult.isSuccess());
    }

    @Test
    public void getSingleTask_shouldReturnData_whenValidRequest() {
        String jobName = "task_job3" + uniqueId;
        long jobId = jobDefinitionControllerWrapper.createJobDefinition(jobName);
        String datasourceName = "task_job1_db3" + uniqueId;
        String datasourceId =
                seatunnelDatasourceControllerWrapper.createFakeSourceDatasource(datasourceName);
        String sourcePluginId =
                jobTaskControllerWrapper.createFakeSourcePlugin(datasourceId, jobId);
        Result<PluginConfig> result = jobTaskControllerWrapper.getSingleTask(jobId, sourcePluginId);
        assertTrue(result.isSuccess());
    }

    @Test
    public void deleteSingleTask_shouldReturnSuccess_whenValidRequest() {
        String jobName = "task_job7" + uniqueId;
        long jobId = jobDefinitionControllerWrapper.createJobDefinition(jobName);
        String datasourceName = "task_job1_db4" + uniqueId;
        String datasourceId =
                seatunnelDatasourceControllerWrapper.createFakeSourceDatasource(datasourceName);
        String sourcePluginId =
                jobTaskControllerWrapper.createFakeSourcePlugin(datasourceId, jobId);
        Result<Void> result = jobTaskControllerWrapper.deleteSingleTask(jobId, sourcePluginId);
        assertTrue(result.isSuccess());
    }

    @AfterAll
    public static void tearDown() {
        seaTunnelWebCluster.stop();
    }
}
