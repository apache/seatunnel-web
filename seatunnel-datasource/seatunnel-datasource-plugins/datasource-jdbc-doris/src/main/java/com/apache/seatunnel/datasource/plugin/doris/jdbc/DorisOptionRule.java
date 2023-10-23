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
