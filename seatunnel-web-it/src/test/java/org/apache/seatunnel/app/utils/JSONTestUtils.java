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

package org.apache.seatunnel.app.utils;

import org.apache.seatunnel.api.common.PluginIdentifier;
import org.apache.seatunnel.app.common.Constants;
import org.apache.seatunnel.app.domain.ConnectorInfoDeserializer;
import org.apache.seatunnel.app.domain.JobExecutorResDeserializer;
import org.apache.seatunnel.app.domain.PluginIdentifierDeserializer;
import org.apache.seatunnel.app.domain.response.connector.ConnectorInfo;
import org.apache.seatunnel.app.domain.response.executor.JobExecutorRes;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;
import static com.fasterxml.jackson.databind.MapperFeature.REQUIRE_SETTERS_FOR_GETTERS;

public class JSONTestUtils {

    private static final Logger logger = LoggerFactory.getLogger(JSONTestUtils.class);

    static {
        logger.info("init timezone: {}", TimeZone.getDefault());
    }

    private static final ObjectMapper objectMapper =
            JsonMapper.builder()
                    .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                    .configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                    .configure(REQUIRE_SETTERS_FOR_GETTERS, true)
                    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                    .addModule(new JavaTimeModule())
                    .defaultTimeZone(TimeZone.getDefault())
                    .defaultDateFormat(new SimpleDateFormat(Constants.YYYY_MM_DD_HH_MM_SS))
                    .defaultPrettyPrinter(new DefaultPrettyPrinter())
                    .build();
    /* can use static singleton, inject: just make sure to reuse! */
    static {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PluginIdentifier.class, new PluginIdentifierDeserializer());
        module.addDeserializer(ConnectorInfo.class, new ConnectorInfoDeserializer());
        module.addDeserializer(JobExecutorRes.class, new JobExecutorResDeserializer());
        objectMapper.registerModule(module);
    }

    /**
     * This method deserializes the specified Json into an object of the specified class. It is not
     * suitable to use if the specified class is a generic type since it will not have the generic
     * type information because of the Type Erasure feature of Java. Therefore, this method should
     * not be used if the desired type is a generic type. Note that this method works fine if any of
     * the fields of the specified object are generics, just the object itself should not be a
     * generic type.
     *
     * @param json the string from which the object is to be deserialized
     * @param clazz the class of T
     * @param <T> T
     * @return an object of type T from the string classOfT
     */
    public static @Nullable <T> T parseObject(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            logger.error("parse object exception! json: {}", json, e);
        }
        return null;
    }

    /**
     * json to list
     *
     * @param json json string
     * @param clazz class
     * @param <T> T
     * @return list
     */
    public static <T> List<T> toList(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyList();
        }
        try {
            CollectionType listType =
                    objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
            return objectMapper.readValue(json, listType);
        } catch (Exception e) {
            logger.error("parse list exception! json: {}", json, e);
        }

        return Collections.emptyList();
    }

    /**
     * json to object
     *
     * @param json json string
     * @param type type reference
     * @param <T>
     * @return return parse object
     */
    public static @Nullable <T> T parseObject(String json, TypeReference<T> type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            logger.error("json to map exception!, json: {}", json, e);
        }

        return null;
    }
}
