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

package org.apache.seatunnel.app.utils;

import org.apache.seatunnel.shade.com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.apache.seatunnel.shade.com.typesafe.config.ConfigFactory;
import org.apache.seatunnel.shade.com.typesafe.config.ConfigRenderOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SeaTunnelConfGenerator {

    private static final String CONFIG_TEMPLATE =
            "env {\n"
                    + "env_placeholder"
                    + "}\n"
                    + "source {\n"
                    + "source_placeholder"
                    + "}\n"
                    + "transform {\n"
                    + "transform_placeholder"
                    + "}\n"
                    + "sink {\n"
                    + "sink_placeholder"
                    + "}\n";

    private static final ConfigRenderOptions configRenderOptions =
            ConfigRenderOptions.defaults()
                    .setJson(false)
                    .setComments(false)
                    .setOriginComments(false);

    public static String generate(Config env,
                                  Map<String, List<Config>> sources,
                                  Map<String, List<Config>> transforms,
                                  Map<String, List<Config>> sinks) {
        return CONFIG_TEMPLATE
                .replace("env_placeholder", configToString(env))
                .replace("source_placeholder", mapConfigToString(sources))
                .replace("transform_placeholder", mapConfigToString(transforms))
                .replace("sink_placeholder", mapConfigToString(sinks));
    }

    private static String configToString(Config config) {
        return config.root().render(configRenderOptions);
    }

    private static String mapConfigToString(Map<String, List<Config>> connectorMap) {
        if (connectorMap.isEmpty()) {
            return "";
        }

        List<String> configs = new ArrayList<>();
        connectorMap.forEach((key, value) -> {
            for (Config c : value) {
                configs.add(
                        ConfigFactory.empty()
                                .withValue(key, c.root())
                                .root()
                                .render(configRenderOptions));
            }
        });
        return StringUtils.join(configs, "\n");
    }


    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
