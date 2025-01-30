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
package org.apache.seatunnel.app.service.impl;

import org.apache.seatunnel.app.domain.request.connector.BusinessMode;
import org.apache.seatunnel.app.domain.request.job.Edge;
import org.apache.seatunnel.app.domain.request.job.JobConfig;
import org.apache.seatunnel.app.domain.request.job.JobCreateReq;
import org.apache.seatunnel.app.domain.request.job.JobDAG;
import org.apache.seatunnel.app.domain.request.job.JobReq;
import org.apache.seatunnel.app.domain.request.job.JobTaskInfo;
import org.apache.seatunnel.app.domain.request.job.PluginConfig;
import org.apache.seatunnel.app.domain.response.job.JobConfigRes;
import org.apache.seatunnel.app.domain.response.job.JobRes;
import org.apache.seatunnel.app.service.IJobConfigService;
import org.apache.seatunnel.app.service.IJobDefinitionService;
import org.apache.seatunnel.app.service.IJobService;
import org.apache.seatunnel.app.service.IJobTaskService;
import org.apache.seatunnel.common.constants.JobMode;
import org.apache.seatunnel.server.common.CodeGenerateUtils;
import org.apache.seatunnel.server.common.ParamValidationException;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;

import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.annotation.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class JobServiceImpl implements IJobService {

    @Resource private IJobDefinitionService jobService;
    @Resource private IJobTaskService jobTaskService;
    @Resource private IJobConfigService jobConfigService;

    @Override
    @Transactional
    public long createJob(JobCreateReq jobCreateRequest) throws JsonProcessingException {
        JobReq jobDefinition = getJobDefinition(jobCreateRequest.getJobConfig());
        long jobId = jobService.createJob(jobDefinition);
        createTasks(jobCreateRequest, jobId);
        return jobId;
    }

    private void createTasks(JobCreateReq jobCreateRequest, long jobId)
            throws JsonProcessingException {
        List<PluginConfig> pluginConfig = jobCreateRequest.getPluginConfigs();
        Set<String> edgeIds =
                jobCreateRequest.getJobDAG().getEdges().stream()
                        .flatMap(
                                edge ->
                                        Stream.of(
                                                edge.getInputPluginId(), edge.getTargetPluginId()))
                        .collect(Collectors.toSet());
        Map<String, String> pluginNameVsPluginId = new HashMap<>();
        if (pluginConfig != null) {
            for (PluginConfig config : pluginConfig) {
                String pluginIdKey =
                        edgeIds.contains(config.getName())
                                ? config.getName()
                                : config.getPluginId();
                String newPluginId = String.valueOf(CodeGenerateUtils.getInstance().genCode());
                config.setPluginId(newPluginId);
                jobTaskService.saveSingleTask(jobId, config);
                pluginNameVsPluginId.put(pluginIdKey, newPluginId);
            }
        }
        jobConfigService.updateJobConfig(jobId, jobCreateRequest.getJobConfig());
        JobDAG jobDAG = jobCreateRequest.getJobDAG();
        // Replace the plugin name with plugin id
        List<Edge> edges = jobDAG.getEdges();
        for (Edge edge : edges) {
            edge.setInputPluginId(pluginNameVsPluginId.get(edge.getInputPluginId()));
            edge.setTargetPluginId(pluginNameVsPluginId.get(edge.getTargetPluginId()));
        }
        jobTaskService.saveJobDAG(jobId, jobDAG);
    }

    private JobReq getJobDefinition(JobConfig jobConfig) {
        JobReq jobReq = new JobReq();
        if (StringUtils.isEmpty(jobConfig.getName())) {
            throw new ParamValidationException(SeatunnelErrorEnum.PARAM_CAN_NOT_BE_NULL, "name");
        }
        jobReq.setName(jobConfig.getName());
        if (StringUtils.isEmpty(jobConfig.getDescription())) {
            throw new ParamValidationException(
                    SeatunnelErrorEnum.PARAM_CAN_NOT_BE_NULL, "description");
        }
        jobReq.setDescription(jobConfig.getDescription());
        try {
            JobMode jobMode = JobMode.valueOf((String) jobConfig.getEnv().get("job.mode"));
            if (JobMode.BATCH == jobMode) {
                jobReq.setJobType(BusinessMode.DATA_INTEGRATION);
            } else if (JobMode.STREAMING == jobMode) {
                jobReq.setJobType(BusinessMode.DATA_REPLICA);
            } else {
                throw new ParamValidationException(
                        SeatunnelErrorEnum.INVALID_PARAM,
                        "job.mode",
                        "job.mode should be either BATCH or STREAMING");
            }
        } catch (Exception e) {
            throw new ParamValidationException(
                    SeatunnelErrorEnum.INVALID_PARAM,
                    "job.mode",
                    "job.mode should be either BATCH or STREAMING");
        }
        return jobReq;
    }

    @Override
    public void updateJob(long jobVersionId, JobCreateReq jobCreateReq)
            throws JsonProcessingException {
        jobTaskService.deleteTaskByVersionId(jobVersionId);
        createTasks(jobCreateReq, jobVersionId);
    }

    @Override
    public JobRes getJob(long jobVersionId) throws JsonProcessingException {
        JobConfigRes jobConfig = jobConfigService.getJobConfig(jobVersionId);
        JobTaskInfo taskConfig = jobTaskService.getTaskConfig(jobVersionId);
        return new JobRes(jobConfig, taskConfig.getPlugins(), new JobDAG(taskConfig.getEdges()));
    }
}
