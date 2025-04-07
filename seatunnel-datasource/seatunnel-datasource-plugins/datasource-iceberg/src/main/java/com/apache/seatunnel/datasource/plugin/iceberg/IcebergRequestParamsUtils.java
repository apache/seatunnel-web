package com.apache.seatunnel.datasource.plugin.iceberg;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class IcebergRequestParamsUtils {

    public static String parseStringFromRequestParams(Map<String, String> requestParams) {
        checkArgument(
                requestParams.containsKey(IcebergOptionRule.WAREHOUSE_URI.key()),
                String.format(
                        "Missing %s in requestParams", IcebergOptionRule.WAREHOUSE_URI.key()));

        return requestParams.get(IcebergOptionRule.WAREHOUSE_URI.key());
    }
}
