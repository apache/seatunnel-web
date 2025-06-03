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

package org.apache.seatunnel.app.metrics.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

@Component
public class MetricsFilter implements Filter {

    @Value("${seatunnel-web.telemetry.port:8802}")
    private String telemetryPort;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (httpRequest.getLocalPort() == Integer.parseInt(telemetryPort)
                && "/metrics".equals(httpRequest.getRequestURI())) {
            chain.doFilter(request, response); // Allow metrics requests
        } else if (httpRequest.getLocalPort() == Integer.parseInt(telemetryPort)) {
            response.getWriter().write("Access Denied");
            response.getWriter().flush();
        } else {
            chain.doFilter(request, response); // Allow other requests on the default port
        }
    }
}
