package com.apache.seatunnel.datasource.plugin.mongodb;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class MongoRequestParamsUtils {

    public static String parseStringFromRequestParams(Map<String, String> requestParams) {
        checkArgument(
                requestParams.containsKey(MongoOptionRule.URI.key()),
                String.format("Missing %s in requestParams", MongoOptionRule.URI.key()));

        return requestParams.get(MongoOptionRule.URI.key());
    }
}
