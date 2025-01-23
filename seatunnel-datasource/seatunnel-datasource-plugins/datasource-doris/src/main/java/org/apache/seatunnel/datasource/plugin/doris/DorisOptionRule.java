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

package org.apache.seatunnel.datasource.plugin.doris;

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.api.configuration.util.OptionRule;

import java.util.List;

public class DorisOptionRule {

    public static final Option<List<String>> NODE_URLS =
            Options.key("nodeUrls")
                    .listType()
                    .noDefaultValue()
                    .withDescription(
                            "Apache Doris FrontEnds address, the format is [\"fe_ip:fe_http_port\", ...]");

    public static final Option<String> USERNAME =
            Options.key("username")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Apache Doris user username");

    public static final Option<String> PASSWORD =
            Options.key("password")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Apache Doris user password");

    public static final Option<String> DATABASE =
            Options.key("database")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The name of Apache Doris database");

    public static final Option<String> TABLE =
            Options.key("table")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The name of Apache Doris table");

    public static final Option<String> BASE_URL =
            Options.key("base-url")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "URL has to be without database, like \"jdbc:mysql://localhost:9030/\" or"
                                    + "\"jdbc:mysql://localhost:9030\" rather than \"jdbc:mysql://localhost:9030/db\"");

    public static OptionRule optionRule() {
        return OptionRule.builder().required(NODE_URLS, USERNAME, PASSWORD, BASE_URL).build();
    }

    public static OptionRule metadataRule() {
        return OptionRule.builder().required(DATABASE, TABLE).build();
    }
}
