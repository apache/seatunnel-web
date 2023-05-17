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

package org.apache.seatunnel.app.thirdpart.datasource;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.seatunnel.app.domain.request.job.SelectTableFields;
import org.apache.seatunnel.app.domain.response.datasource.VirtualTableDetailRes;
import org.apache.seatunnel.app.domain.response.datasource.VirtualTableFieldRes;
import org.apache.seatunnel.shade.com.typesafe.config.Config;
import org.apache.seatunnel.shade.com.typesafe.config.ConfigFactory;
import org.apache.seatunnel.shade.com.typesafe.config.ConfigValueFactory;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class SchemaGenerator {

    private SchemaGenerator() {}

    /**
     * Generate the schema of the table.
     *
     * <pre>
     * fields {
     *        name = "string"
     *        age = "int"
     *       }
     * </pre>
     *
     * @param virtualTableDetailRes virtual table detail.
     * @param selectTableFields select table fields which need to be placed in the schema.
     * @return schema.
     */
    public static Config generateSchemaBySelectTableFields(
            VirtualTableDetailRes virtualTableDetailRes, SelectTableFields selectTableFields) {
        checkNotNull(selectTableFields, "selectTableFields cannot be null");
        checkArgument(
                CollectionUtils.isNotEmpty(selectTableFields.getTableFields()),
                "selectTableFields.tableFields cannot be empty");

        checkNotNull(virtualTableDetailRes, "virtualTableDetailRes cannot be null");
        checkArgument(
                CollectionUtils.isNotEmpty(virtualTableDetailRes.getFields()),
                "virtualTableDetailRes.fields cannot be empty");

        Map<String, VirtualTableFieldRes> fieldTypeMap =
                virtualTableDetailRes.getFields().stream()
                        .collect(
                                Collectors.toMap(
                                        VirtualTableFieldRes::getFieldName, Function.identity()));

        Config schema = ConfigFactory.empty();
        for (String fieldName : selectTableFields.getTableFields()) {
            VirtualTableFieldRes virtualTableFieldRes =
                    checkNotNull(
                            fieldTypeMap.get(fieldName),
                            String.format(
                                    "Cannot find the field: %s from virtual table", fieldName));
            schema =
                    schema.withValue(
                            fieldName,
                            ConfigValueFactory.fromAnyRef(virtualTableFieldRes.getFieldType()));
        }
        return schema.atKey("fields");
    }
}
