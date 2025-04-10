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
package org.apache.seatunnel.app.common;

import org.apache.seatunnel.app.domain.request.user.UserLoginReq;
import org.apache.seatunnel.app.domain.response.user.UserSimpleInfoRes;
import org.apache.seatunnel.app.utils.JSONTestUtils;
import org.apache.seatunnel.common.utils.JsonUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class SeatunnelWebTestingBase {
    protected final String baseUrl = "http://localhost:8802/seatunnel/api/v1";

    public Result<UserSimpleInfoRes> login(UserLoginReq userLoginReq) {
        return login(userLoginReq, null);
    }

    public Result<UserSimpleInfoRes> login(UserLoginReq userLoginReq, String authType) {
        return login(userLoginReq, authType, false);
    }

    public Result<UserSimpleInfoRes> login(
            UserLoginReq userLoginReq, String authType, Boolean setAsCurrentUser) {
        String requestBody = JsonUtils.toJsonString(userLoginReq);
        Map<String, String> headers =
                authType != null
                        ? Collections.singletonMap("X-Seatunnel-Auth-Type", authType)
                        : null;
        String response = sendRequest(url("user/login"), requestBody, "POST", headers);
        Result<UserSimpleInfoRes> userSimpleInfoResResult =
                JSONTestUtils.parseObject(
                        response, new TypeReference<Result<UserSimpleInfoRes>>() {});
        if (setAsCurrentUser) {
            assert userSimpleInfoResResult != null;
            TokenProvider.setToken(userSimpleInfoResResult.getData().getToken());
        }
        return userSimpleInfoResResult;
    }

    protected String url(String path) {
        return String.format("%s/%s?", baseUrl, path);
    }

    protected String urlWithParam(String pathAndParam) {
        return String.format("%s/%s", baseUrl, pathAndParam);
    }

    protected String sendRequest(String url) {
        return sendRequest(url, null, "GET", null);
    }

    protected String sendRequest(String url, String requestBody, String httpMethod) {
        return sendRequest(url, requestBody, httpMethod, null);
    }

    protected String sendRequest(
            String url, String requestBody, String httpMethod, Map<String, String> headers) {
        HttpURLConnection connection = null;
        try {
            URL urlObject = new URL(url);
            connection = (HttpURLConnection) urlObject.openConnection();
            if ("PATCH".equalsIgnoreCase(httpMethod)) {
                setRequestMethodUsingReflection(connection, "PATCH");
            } else {
                connection.setRequestMethod(httpMethod);
            }

            connection.setRequestProperty("Content-Type", "application/json");
            if (!url.endsWith("user/login?")) {
                connection.setRequestProperty("token", TokenProvider.getToken());
            }
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    connection.setRequestProperty(header.getKey(), header.getValue());
                }
            }
            connection.setDoOutput(true);
            if (requestBody != null) {
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readResponse(connection);
            } else {
                String message = "API Request failed with status code: " + responseCode;
                throw new RuntimeException(message);
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void setRequestMethodUsingReflection(
            HttpURLConnection httpURLConnection, String method) throws Exception {
        try {
            Field methodField = HttpURLConnection.class.getDeclaredField("method");
            methodField.setAccessible(true);
            methodField.set(httpURLConnection, method);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new Exception("Failed to set HTTP method to PATCH", e);
        }
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }
}
