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
import org.apache.seatunnel.app.domain.request.job.JobExecParam;
import org.apache.seatunnel.app.utils.JSONTestUtils;
import org.apache.seatunnel.app.utils.JSONUtils;

import com.fasterxml.jackson.core.type.TypeReference;

public class JobExecutorControllerWrapper extends SeatunnelWebTestingBase {

    public Result<Long> jobExecutor(Long jobDefineId) {
        String response =
                sendRequest(
                        urlWithParam("job/executor/execute?jobDefineId=" + jobDefineId),
                        "{}",
                        "POST");
        return JSONTestUtils.parseObject(response, new TypeReference<Result<Long>>() {});
    }

    public Result<Long> jobExecutor(Long jobDefineId, JobExecParam jobExecParam) {
        String requestBody = JSONUtils.toPrettyJsonString(jobExecParam);
        String response =
                sendRequest(
                        urlWithParam("job/executor/execute?jobDefineId=" + jobDefineId),
                        requestBody,
                        "POST");
        return JSONTestUtils.parseObject(response, new TypeReference<Result<Long>>() {});
    }

    public Result<Void> resource(Long jobDefineId) {
        String response =
                sendRequest(urlWithParam("job/executor/resource?jobDefineId=" + jobDefineId));
        return JSONTestUtils.parseObject(response, Result.class);
    }

    public Result<Void> jobPause(Long jobInstanceId) {
        String response =
                sendRequest(urlWithParam("job/executor/pause?jobInstanceId=" + jobInstanceId));
        return JSONTestUtils.parseObject(response, Result.class);
    }

    public Result<Void> jobRestore(Long jobInstanceId) {
        String response =
                sendRequest(urlWithParam("job/executor/restore?jobInstanceId=" + jobInstanceId));
        return JSONTestUtils.parseObject(response, Result.class);
    }
}
