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

import org.apache.seatunnel.shade.com.fasterxml.jackson.core.type.TypeReference;

import org.apache.seatunnel.app.common.EngineType;
import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.common.SeatunnelWebTestingBase;
import org.apache.seatunnel.app.domain.request.job.JobConfig;
import org.apache.seatunnel.app.domain.response.job.JobConfigRes;
import org.apache.seatunnel.common.constants.JobMode;
import org.apache.seatunnel.common.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

public class JobConfigControllerWrapper extends SeatunnelWebTestingBase {

    public Result<Void> updateJobConfig(long jobVersionId, JobConfig jobConfig) {
        String requestBody = JsonUtils.toJsonString(jobConfig);
        String response = sendRequest(url("job/config/" + jobVersionId), requestBody, "PUT");
        return JsonUtils.parseObject(response, Result.class);
    }

    public Result<JobConfigRes> getJobConfig(long jobVersionId) {
        String response = sendRequest(url("job/config/" + jobVersionId));
        return JsonUtils.parseObject(response, new TypeReference<Result<JobConfigRes>>() {});
    }

    public JobConfig populateJobConfigObject(String jobName) {
        JobConfig jobConfig = new JobConfig();
        jobConfig.setName(jobName);
        jobConfig.setDescription(jobName + " description from config");
        jobConfig.setEngine(EngineType.SeaTunnel);
        Map<String, Object> env = new HashMap<>();
        env.put("job.mode", JobMode.BATCH);
        env.put("job.name", "SeaTunnel_Job");
        env.put("jars", "");
        env.put("checkpoint.interval", "30");
        env.put("checkpoint.timeout", "");
        env.put("read_limit.rows_per_second", "");
        env.put("read_limit.bytes_per_second", "");
        env.put("custom_parameters", "");
        jobConfig.setEnv(env);
        return jobConfig;
    }
}
