package com.whaleops.datasource.datasource.plugin.hive;

import com.google.common.collect.Sets;

import java.util.Set;

public class HiveConstants {

    public static final Set<String> HIVE_SYSTEM_DATABASES =
            Sets.newHashSet(
                    "information_schema", "mysql", "performance_schema", "sys", "test", "hivedb");
}
