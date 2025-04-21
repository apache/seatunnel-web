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
import org.apache.seatunnel.app.domain.request.connector.BusinessMode;
import org.apache.seatunnel.app.domain.request.job.JobReq;
import org.apache.seatunnel.app.domain.response.PageInfo;
import org.apache.seatunnel.app.domain.response.job.JobDefinitionRes;
import org.apache.seatunnel.app.utils.JSONTestUtils;
import org.apache.seatunnel.common.constants.JobMode;
import org.apache.seatunnel.common.utils.JsonUtils;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;

import com.fasterxml.jackson.core.type.TypeReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobDefinitionControllerWrapper extends SeatunnelWebTestingBase {

    public Result<Long> createJobDefinition(JobReq jobReq) {
        String requestBody = JsonUtils.toJsonString(jobReq);
        String response = sendRequest(url("job/definition"), requestBody, "POST");
        return JSONTestUtils.parseObject(response, new TypeReference<Result<Long>>() {});
    }

    public Long createJobDefinition(String jobName) {
        JobReq jobReq = new JobReq();
        jobReq.setName(jobName);
        jobReq.setDescription(jobName + " description");
        jobReq.setJobType(BusinessMode.DATA_INTEGRATION);
        Result<Long> result = createJobDefinition(jobReq);
        assertTrue(result.isSuccess());
        return result.getData();
    }

    public void createJobExpectingFailure(String jobName) {
        JobReq jobReq = new JobReq();
        jobReq.setName(jobName);
        jobReq.setDescription(jobName + " description");
        jobReq.setJobType(BusinessMode.DATA_INTEGRATION);
        Result<Long> result = createJobDefinition(jobReq);
        assertEquals(SeatunnelErrorEnum.ACCESS_DENIED.getCode(), result.getCode());
    }

    public Result<PageInfo<JobDefinitionRes>> getJobDefinition(
            String searchName, Integer pageNo, Integer pageSize, JobMode jobMode) {
        String response =
                sendRequest(
                        urlWithParam("job/definition?")
                                + "searchName="
                                + searchName
                                + "&pageNo="
                                + pageNo
                                + "&pageSize="
                                + pageSize
                                + "&jobMode="
                                + jobMode);
        return JSONTestUtils.parseObject(
                response, new TypeReference<Result<PageInfo<JobDefinitionRes>>>() {});
    }

    public Result<JobDefinitionRes> getJobDefinitionById(long jobId) {
        String response = sendRequest(url("job/definition/" + jobId));
        return JSONTestUtils.parseObject(
                response, new TypeReference<Result<JobDefinitionRes>>() {});
    }

    public Result<Void> deleteJobDefinition(long id) {
        String response = sendRequest(urlWithParam("job/definition?id=" + id), null, "DELETE");
        return JSONTestUtils.parseObject(response, Result.class);
    }
}
