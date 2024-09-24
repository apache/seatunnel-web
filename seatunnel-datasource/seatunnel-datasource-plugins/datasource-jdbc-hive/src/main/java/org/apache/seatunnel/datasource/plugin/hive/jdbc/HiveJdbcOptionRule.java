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

package org.apache.seatunnel.datasource.plugin.hive.jdbc;

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.api.configuration.util.OptionRule;

public class HiveJdbcOptionRule {

    public static final Option<String> URL =
            Options.key("url")
                    .stringType()
                    .defaultValue("jdbc:hive2://localhost:10000/default")
                    .withDescription(
                            "The URL of the JDBC connection. Refer to a case: jdbc:hive2://localhost:10000/default");

    public static final Option<String> DRIVER =
            Options.key("driver")
                    .stringType()
                    .defaultValue("org.apache.hive.jdbc.HiveDriver")
                    .withDescription(
                            "The jdbc class name used to connect to the remote data source");

    public static final Option<String> USER =
            Options.key("user").stringType().noDefaultValue().withDescription("user");

    public static final Option<String> PASSWORD =
            Options.key("password").stringType().noDefaultValue().withDescription("password");

    public static final Option<Boolean> USE_KERBEROS =
            Options.key("use_kerberos")
                    .booleanType()
                    .defaultValue(false)
                    .withDescription("Whether to enable Kerberos, default is false.");

    public static final Option<String> KERBEROS_PRINCIPAL =
            Options.key("kerberos_principal")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "When use kerberos, we should set kerberos principal such as 'test_user@xxx'. ");

    public static final Option<String> KERBEROS_KEYTAB_PATH =
            Options.key("kerberos_keytab_path")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "When use kerberos, we should set kerberos principal file path such as '/home/test/test_user.keytab'. ");

    public static final Option<String> KRB5_PATH =
            Options.key("krb5_path")
                    .stringType()
                    .defaultValue("/etc/krb5.conf")
                    .withDescription(
                            "When use kerberos, we should set krb5 path file path such as '/seatunnel/krb5.conf' or use the default path '/etc/krb5.conf");

    public static OptionRule optionRule() {
        return OptionRule.builder()
                .required(URL)
                .required(DRIVER)
                .optional(USER)
                .optional(PASSWORD)
                .optional(USE_KERBEROS)
                .optional(KERBEROS_PRINCIPAL)
                .optional(KERBEROS_KEYTAB_PATH)
                .optional(KRB5_PATH)
                .build();
    }

    public static OptionRule metadataRule() {
        return OptionRule.builder().build();
    }
}
