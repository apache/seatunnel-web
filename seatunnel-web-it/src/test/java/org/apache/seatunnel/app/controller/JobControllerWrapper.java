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
package org.apache.seatunnel.app.controller;

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.common.SeatunnelWebTestingBase;
import org.apache.seatunnel.app.domain.request.job.JobConfig;
import org.apache.seatunnel.app.domain.request.job.JobCreateReq;
import org.apache.seatunnel.app.domain.request.job.JobDAG;
import org.apache.seatunnel.app.domain.request.job.PluginConfig;
import org.apache.seatunnel.app.domain.response.job.JobConfigRes;
import org.apache.seatunnel.app.domain.response.job.JobRes;
import org.apache.seatunnel.app.utils.JSONTestUtils;
import org.apache.seatunnel.common.utils.JsonUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class JobControllerWrapper extends SeatunnelWebTestingBase {

    public Result<Long> createJob(JobCreateReq jobCreateRequest) {
        String requestBody = JsonUtils.toJsonString(jobCreateRequest);
        String response = sendRequest(url("job/create"), requestBody, "POST");
        return JSONTestUtils.parseObject(response, new TypeReference<Result<Long>>() {});
    }

    public Result<Void> updateJob(long jobVersionId, JobCreateReq jobCreateReq) {
        String requestBody = JsonUtils.toJsonString(jobCreateReq);
        String response =
                sendRequest(urlWithParam("job/update/" + jobVersionId + "?"), requestBody, "PUT");
        return JSONTestUtils.parseObject(response, new TypeReference<Result<Void>>() {});
    }

    public Result<JobRes> getJob(long jobVersionId) {
        String response = sendRequest(urlWithParam("job/get/" + jobVersionId + "?"), null, "GET");
        return JSONTestUtils.parseObject(response, new TypeReference<Result<JobRes>>() {});
    }

    public JobCreateReq convertJobResToJobCreateReq(JobRes jobRes) {
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
}
