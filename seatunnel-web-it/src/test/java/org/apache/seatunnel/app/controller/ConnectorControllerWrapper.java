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

import org.apache.seatunnel.shade.com.fasterxml.jackson.databind.JsonNode;

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.common.SeatunnelWebTestingBase;
import org.apache.seatunnel.app.domain.response.connector.ConnectorInfo;
import org.apache.seatunnel.app.utils.JSONTestUtils;
import org.apache.seatunnel.common.utils.JsonUtils;

import java.util.List;

public class ConnectorControllerWrapper extends SeatunnelWebTestingBase {

    public List<ConnectorInfo> listAllTransform() {
        String response = sendRequest(url("connector/transforms"));
        JsonNode data = JsonUtils.parseObject(response).findValue("data");
        return JSONTestUtils.toList(data.toString(), ConnectorInfo.class);
    }

    public List<ConnectorInfo> listSource(String status) {
        String response = sendRequest(urlWithParam("connector/sources?status=" + status));
        JsonNode data = JsonUtils.parseObject(response).findValue("data");
        return JSONTestUtils.toList(data.toString(), ConnectorInfo.class);
    }

    public List<ConnectorInfo> listSink(String status) {
        String response = sendRequest(urlWithParam("connector/sinks?status=" + status));
        JsonNode data = JsonUtils.parseObject(response).findValue("data");
        return JSONTestUtils.toList(data.toString(), ConnectorInfo.class);
    }

    public Result<Void> sync() {
        String response = sendRequest(url("connector/sync"));
        return JSONTestUtils.parseObject(response, Result.class);
    }

    public Result<Void> getConnectorFormStructure(String connectorType, String connectorName) {
        String response =
                sendRequest(
                        urlWithParam(
                                "connector/form?connectorType="
                                        + connectorType
                                        + "&connectorName="
                                        + connectorName));
        return JSONTestUtils.parseObject(response, Result.class);
    }
}
