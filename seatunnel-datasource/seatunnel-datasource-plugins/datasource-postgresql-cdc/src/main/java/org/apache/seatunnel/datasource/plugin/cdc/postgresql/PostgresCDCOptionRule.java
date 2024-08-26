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

package org.apache.seatunnel.datasource.plugin.cdc.postgresql;

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.api.configuration.util.OptionRule;

public class PostgresCDCOptionRule {
    public static final Option<String> URL =
            Options.key("url")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "jdbc url, eg:"
                                    + "jdbc:postgresql://localhost:5432//test?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8");

    public static final Option<String> BASE_URL =
            Options.key("base-url")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "URL has to be without database, like \"jdbc:postgresql://localhost:5432/\"");

    public static final Option<String> USERNAME =
            Options.key("username")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Username to use when connecting to the PostgreSQL server.");

    public static final Option<String> PASSWORD =
            Options.key("password")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Password to use when connecting to the PostgreSQL server.");

    public static final Option<String> DATABASE_NAME =
            Options.key("database-name")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Name of the database to monitor.");

    public static final Option<String> TABLE_NAME =
            Options.key("table-name")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Name of the table to monitor.");

    public static final Option<String> SERVER_TIME_ZONE =
            Options.key("server-time-zone")
                    .stringType()
                    .defaultValue("UTC")
                    .withDescription("The session time zone in the PostgreSQL server.");
    public static final Option<DriverType> DRIVER =
            Options.key("driver")
                    .enumType(DriverType.class)
                    .defaultValue(DriverType.POSTGRESQL)
                    .withDescription("driver");
    public static final Option<String> USER =
            Options.key("user").stringType().noDefaultValue().withDescription("jdbc user");

    public static OptionRule optionRule() {
        return OptionRule.builder()
                .required(USERNAME, PASSWORD, BASE_URL)
                .optional(SERVER_TIME_ZONE)
                .build();
    }

    public enum DriverType {
        POSTGRESQL("org.postgresql.Driver"),
        ;
        private final String driverClassName;

        DriverType(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        @Override
        public String toString() {
            return driverClassName;
        }
    }

    public static OptionRule metadataRule() {
        return OptionRule.builder().required(DATABASE_NAME, TABLE_NAME).build();
    }
}
