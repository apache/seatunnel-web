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

package com.apache.seatunnel.datasource.plugin.mongodbcdc;

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.common.utils.SeaTunnelException;

import org.apache.commons.lang3.StringUtils;

import com.mongodb.ConnectionString;

import javax.annotation.Nonnull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MongodbCDCOptionRule {

    public static final Option<String> HOSTS =
            Options.key("hosts")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "The comma-separated list of hostname and port pairs of the MongoDB servers. "
                                    + "eg. localhost:27017,localhost:27018");

    public static final Option<String> CONNECTION_OPTIONS =
            Options.key("connection.options")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "The ampersand-separated MongoDB connection options. "
                                    + "eg. replicaSet=test&connectTimeoutMS=300000");

    public static final Option<String> USERNAME =
            Options.key("username")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "Name of the database user to be used when connecting to MongoDB. "
                                    + "This is required only when MongoDB is configured to use authentication.");

    public static final Option<String> PASSWORD =
            Options.key("password")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "Password to be used when connecting to MongoDB. "
                                    + "This is required only when MongoDB is configured to use authentication.");

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
        return OptionRule.builder()
                .required(HOSTS)
                .optional(CONNECTION_OPTIONS, USERNAME, PASSWORD)
                .build();
    }

    public static OptionRule metadataRule() {
        return OptionRule.builder().required(DATABASE, COLLECTION).build();
    }

    public static @Nonnull ConnectionString buildConnectionString(
            String username, String password, String hosts, String connectionOptions) {
        StringBuilder sb = new StringBuilder("mongodb://");

        if (hasCredentials(username, password)) {
            appendCredentials(sb, username, password);
        }

        sb.append(hosts);

        if (StringUtils.isNotEmpty(connectionOptions)) {
            sb.append("/?").append(connectionOptions);
        }

        return new ConnectionString(sb.toString());
    }

    private static boolean hasCredentials(String username, String password) {
        return StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password);
    }

    private static void appendCredentials(
            @Nonnull StringBuilder sb, String username, String password) {
        sb.append(encodeValue(username)).append(":").append(encodeValue(password)).append("@");
    }

    public static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new SeaTunnelException(e.getMessage());
        }
    }
}
