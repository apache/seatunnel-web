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
import org.apache.seatunnel.app.domain.request.job.JobCreateReq;
import org.apache.seatunnel.app.utils.JSONTestUtils;
import org.apache.seatunnel.app.utils.JSONUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JobControllerWrapper extends SeatunnelWebTestingBase {

    public Result<Long> createJob(JobCreateReq jobCreateRequest) {
        String requestBody = JSONUtils.toPrettyJsonString(jobCreateRequest);
        String response = sendRequest(url("job/create"), requestBody, "POST");
        return JSONTestUtils.parseObject(response, new TypeReference<Result<Long>>() {});
    }

    public JobCreateReq populateJobCreateReqFromFile() {
        String filePath = "src/test/resources/jobs/fake_source_console_job.json";
        String jsonContent;
        try {
            jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return JSONTestUtils.parseObject(jsonContent, JobCreateReq.class);
    }
}
