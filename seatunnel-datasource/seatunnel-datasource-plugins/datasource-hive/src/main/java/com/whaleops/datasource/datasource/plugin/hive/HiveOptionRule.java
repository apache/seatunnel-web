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
