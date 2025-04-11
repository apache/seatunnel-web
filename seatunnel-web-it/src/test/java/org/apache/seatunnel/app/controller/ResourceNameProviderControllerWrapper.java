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
import org.apache.seatunnel.app.utils.JSONTestUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class ResourceNameProviderControllerWrapper extends SeatunnelWebTestingBase {

    private String buildUrl(String baseUrl, String workspaceName, String searchName) {
        StringBuilder url = new StringBuilder(baseUrl);
        if (workspaceName != null || searchName != null) {
            url.append("?");
            if (workspaceName != null) {
                url.append("workspaceName=").append(workspaceName);
            }
            if (searchName != null) {
                if (workspaceName != null) {
                    url.append("&");
                }
                url.append("searchName=").append(searchName);
            }
        }
        return url.toString();
    }

    public Result<List<String>> getWorkspaces(String searchName) {
        String url = buildUrl("resources/workspace", null, searchName);
        String response = sendRequest(urlWithParam(url));
        return JSONTestUtils.parseObject(response, new TypeReference<Result<List<String>>>() {});
    }

    public Result<List<String>> getDatasources(String workspaceName, String searchName) {
        String url = buildUrl("resources/datasource", workspaceName, searchName);
        String response = sendRequest(urlWithParam(url));
        return JSONTestUtils.parseObject(response, new TypeReference<Result<List<String>>>() {});
    }

    public Result<List<String>> getJobDefinitions(String workspaceName, String searchName) {
        String url = buildUrl("resources/job_definition", workspaceName, searchName);
        String response = sendRequest(urlWithParam(url));
        return JSONTestUtils.parseObject(response, new TypeReference<Result<List<String>>>() {});
    }

    public Result<List<String>> getUserNames(String searchName) {
        String url = buildUrl("resources/user", null, searchName);
        String response = sendRequest(urlWithParam(url));
        return JSONTestUtils.parseObject(response, new TypeReference<Result<List<String>>>() {});
    }
}
