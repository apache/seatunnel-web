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

package org.apache.seatunnel.datasource.plugin.fakesource;

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginInfo;
import org.apache.seatunnel.datasource.plugin.api.DatasourcePluginTypeEnum;

public class FakeSourceDataSourceConfig {

    public static final String PLUGIN_NAME = "FakeSource";

    public static final DataSourcePluginInfo FAKESOURCE_DATASOURCE_PLUGIN_INFO =
            DataSourcePluginInfo.builder()
                    .name(PLUGIN_NAME)
                    .icon(PLUGIN_NAME)
                    .version("1.0.0")
                    .type(DatasourcePluginTypeEnum.FAKE_CONNECTION.getCode())
                    .build();
    public static final Option<String> FAKESOURCE_FIELDS =
            Options.key("fields")
                    .stringType()
                    .defaultValue(
                            "{\n"
                                    + "      \"name\": \"string\",\n"
                                    + "      \"age\": \"int\"\n"
                                    + "    }")
                    .withDescription(
                            "This value will be used to populate field details in UI, however, it won't be utilized during execution.");

    public static final OptionRule OPTION_RULE =
            OptionRule.builder().required(FAKESOURCE_FIELDS).build();

    public static final OptionRule METADATA_RULE = OptionRule.builder().required().build();
}
