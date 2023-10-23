package com.apache.seatunnel.datasource.plugin.mongodb;

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.api.configuration.util.OptionRule;

public class MongoOptionRule {

    public static final Option<String> URI =
            Options.key("uri")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The MongoDB connection uri.");

    public static final Option<String> DATABASE =
            Options.key("database")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The name of MongoDB database to read or write.");

    public static final Option<String> COLLECTION =
            Options.key("collection")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The name of MongoDB collection to read or write.");

    public static OptionRule optionRule() {
        return OptionRule.builder().required(URI).build();
    }

    public static OptionRule metadataRule() {
        return OptionRule.builder().required(DATABASE, COLLECTION).build();
    }
}
