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

package com.whaleops.datasource.datasource.plugin.hive;

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.api.configuration.util.OptionRule;

public class HiveOptionRule {

    public static final Option<String> METASTORE_URI =
            Options.key("metastore_uri")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("thrift url, eg:" + "thrift://127.0.0.1:9083");

    public static final Option<String> KERBEROS_PRINCIPAL =
            Options.key("kerberos_principal")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("jdbc kerberos_principal");

    public static final Option<String> KERBEROS_KEYTAB_PATH =
            Options.key("kerberos_keytab_path")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("jdbc kerberos_keytab_path");

    public static final Option<String> KERBEROS_KRB5_CONF_PATH =
            Options.key("kerberos_krb5_conf_path")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("jdbc kerberos_keytab_path");

    public static final Option<String> HDFS_SITE_PATH =
            Options.key("hdfs_site_path")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("jdbc hdfs_site_path");

    public static final Option<String> HIVE_SITE_PATH =
            Options.key("hive_site_path")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("jdbc hive_site_path");

    public static OptionRule optionRule() {
        return OptionRule.builder()
                .required(METASTORE_URI)
                .optional(KERBEROS_PRINCIPAL)
                .optional(KERBEROS_KRB5_CONF_PATH)
                .optional(KERBEROS_KEYTAB_PATH)
                .optional(HDFS_SITE_PATH)
                .optional(HIVE_SITE_PATH)
                .build();
    }

    public static OptionRule metadataRule() {
        // todo
        return OptionRule.builder().build();
    }

    public static final Option<String> TABLE_NAME =
            Options.key("table").stringType().noDefaultValue().withDescription("hive table");
}
