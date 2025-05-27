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

import org.apache.catalina.connector.Connector;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsPortConfig {

    @Value("${seatunnel-web.telemetry.port:8802}")
    private String telemetryPort;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> metricsConnectorCustomizer() {
        return factory -> {
            Connector metricsConnector =
                    new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            metricsConnector.setPort(
                    Integer.parseInt(telemetryPort)); // Set the desired port for metrics
            factory.addAdditionalTomcatConnectors(metricsConnector);
        };
    }
}
