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

package org.apache.seatunnel.datasource.plugin.pulsar;

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.api.configuration.util.OptionRule;

public class PulsarOptionRule {

    public static final Option<String> CLIENT_SERVICE_URL =
            Options.key("client.service-url")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Pulsar cluster HTTP URL for client to connect to a broker.");

    public static final Option<String> ADMIN_SERVICE_URL =
            Options.key("admin.service-url")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Pulsar cluster HTTP URL for admin to connect to cluster .");

    public static final Option<String> TOPIC =
            Options.key("topic")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Pulsar topic name.");

    public static final Option<Boolean> TOPIC_PATTERN =
            Options.key("topic-pattern")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription(
                            "If pattern is set to true,the regular expression for a pattern of topic names to read from."
                                    + " All topics in clients with names that match the specified regular expression will be subscribed by the consumer.");

    // Absolutely required options: ''subscription.name', 'client.service-url', 'admin.service-url''
    // Exclusive required set options: 'topic', 'topic-pattern'
    /*51 known properties: "operationTimeoutMs", "connectionsPerBroker", "connectionMaxIdleSeconds", "useTcpNoDelay", "tlsTrustStorePath", "lookupTimeoutMs", "authParams", "memoryLimitBytes",
    "keepAliveIntervalSeconds", "tlsTrustCertsFilePath", "initialBackoffIntervalNanos", "authParamMap", "requestTimeoutMs", "statsIntervalSeconds", "sslProvider", "tlsTrustStoreType",
    "listenerName", "tlsProtocols", "tlsKeyStorePath", "socks5ProxyPassword", "proxyProtocol", "authPluginClassName", "connectionTimeoutMs", "tlsHostnameVerificationEnable",
    "numListenerThreads", "maxLookupRequest", "useTls", "maxBackoffIntervalNanos", "autoCertRefreshSeconds", "maxLookupRedirects", "serviceUrl", "description", "numIoThreads",
    "concurrentLookupRequest", "tlsTrustStorePassword", "dnsLookupBindPort", "tlsKeyFilePath", "tlsAllowInsecureConnection", "tlsCiphers", "maxNumberOfRejectedRequestPerConnection",
    "useKeyStoreTls", "tlsKeyStorePassword", "enableTransaction", "socks5ProxyUsername", "socks5ProxyAddress", "readTimeoutMs" [truncated]*/
    public static final Option<Integer> OPERATION_TIMEOUT_MS =
            Options.key("operationTimeoutMs")
                    .intType()
                    .defaultValue(30000)
                    .withDescription("Operation timeout. ");
    public static final Option<Integer> CONNECTION_MAX_IDLE_SECONDS =
            Options.key("connectionMaxIdleSeconds")
                    .intType()
                    .defaultValue(30000)
                    .withDescription("Maximum idle seconds of connection. ");
    public static final Option<Integer> LOOKUP_TIMEOUT_MS =
            Options.key("lookupTimeoutMs")
                    .intType()
                    .defaultValue(30000)
                    .withDescription("Search timeout Ms. ");
    public static final Option<Boolean> TLS_HOSTNAME_VERIFICATION_ENABLE =
            Options.key("tlsHostnameVerificationEnable")
                    .booleanType()
                    .defaultValue(Boolean.TRUE)
                    .withDescription("Whether to enable TLS hostname verification. ");
    public static final Option<Boolean> USE_TCP_NO_DELAY =
            Options.key("useTcpNoDelay")
                    .booleanType()
                    .defaultValue(Boolean.TRUE)
                    .withDescription("Whether Using TCP without latency. ");
    public static final Option<Boolean> USE_TLS =
            Options.key("useTls")
                    .booleanType()
                    .defaultValue(Boolean.FALSE)
                    .withDescription("Whether Using Tls. ");

    public static OptionRule optionRule() {
        return OptionRule.builder()
                .required(CLIENT_SERVICE_URL)
                .required(ADMIN_SERVICE_URL)
                .required(TOPIC)
                .optional(TOPIC_PATTERN)
                .optional(OPERATION_TIMEOUT_MS)
                .optional(CONNECTION_MAX_IDLE_SECONDS)
                .optional(LOOKUP_TIMEOUT_MS)
                .optional(TLS_HOSTNAME_VERIFICATION_ENABLE)
                .optional(USE_TCP_NO_DELAY)
                .optional(USE_TLS)
                .build();
    }

    public static OptionRule metadataRule() {
        return OptionRule.builder().required(TOPIC).optional(TOPIC_PATTERN).build();
    }
}
