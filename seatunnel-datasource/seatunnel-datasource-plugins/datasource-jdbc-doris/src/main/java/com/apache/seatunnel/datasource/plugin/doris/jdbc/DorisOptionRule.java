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

package com.apache.seatunnel.datasource.plugin.doris.jdbc;

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.api.configuration.util.OptionRule;

public class DorisOptionRule {

    private static final String DEFAULT_PASSWORD = "";

    public static final Option<String> FENODES =
            Options.key("fenodes")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "Doris cluster fenodes address, the format is \"fe_ip:fe_http_port, ...\"");

    public static final Option<String> USERNAME =
            Options.key("username")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Doris user username");

    public static final Option<String> PASSWORD =
            Options.key("password")
                    .stringType()
                    .defaultValue(DEFAULT_PASSWORD)
                    .withDescription("Doris user password");

    public static final Option<String> TABLE =
            Options.key("table.identifier")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The name of Doris table");

    public static final Option<String> BASE_URL =
            Options.key("base-url")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "URL has to be without database, like \"jdbc:mysql://localhost:5432/\" or"
                                    + "\"jdbc:mysql://localhost:5432\" rather than \"jdbc:mysql://localhost:5432/db\"");

    public static OptionRule optionRule() {
        return OptionRule.builder().required(FENODES, USERNAME, PASSWORD, BASE_URL).build();
    }

    public static OptionRule metadataRule() {
        return OptionRule.builder().required(TABLE).build();
    }
}
