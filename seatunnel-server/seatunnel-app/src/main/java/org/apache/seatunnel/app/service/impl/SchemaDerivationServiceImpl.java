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

package org.apache.seatunnel.app.service.impl;

import org.apache.seatunnel.api.configuration.ReadonlyConfig;
import org.apache.seatunnel.api.table.catalog.CatalogTable;
import org.apache.seatunnel.api.table.catalog.Column;
import org.apache.seatunnel.api.table.catalog.PhysicalColumn;
import org.apache.seatunnel.api.table.catalog.PrimaryKey;
import org.apache.seatunnel.api.table.catalog.TableIdentifier;
import org.apache.seatunnel.api.table.catalog.TableSchema;
import org.apache.seatunnel.api.table.connector.TableTransform;
import org.apache.seatunnel.api.table.factory.FactoryUtil;
import org.apache.seatunnel.api.table.factory.TableFactoryContext;
import org.apache.seatunnel.api.table.factory.TableTransformFactory;
import org.apache.seatunnel.api.table.type.ArrayType;
import org.apache.seatunnel.api.table.type.BasicType;
import org.apache.seatunnel.api.table.type.DecimalType;
import org.apache.seatunnel.api.table.type.LocalTimeType;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.app.domain.request.job.DatabaseTableSchemaReq;
import org.apache.seatunnel.app.domain.request.job.PluginConfig;
import org.apache.seatunnel.app.domain.request.job.TableSchemaReq;
import org.apache.seatunnel.app.domain.request.job.transform.SQL;
import org.apache.seatunnel.app.service.IJobTaskService;
import org.apache.seatunnel.app.service.ISchemaDerivationService;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;
import org.apache.seatunnel.transform.sql.SQLTransform;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SchemaDerivationServiceImpl implements ISchemaDerivationService {

    @Resource private IJobTaskService jobTaskService;

    private static final Pattern decimalPattern = Pattern.compile("DECIMAL\\((\\d+), (\\d+)\\)");

    @Override
    public TableSchemaReq derivationSQL(long jobVersionId, String inputPluginId, SQL sql) {

        PluginConfig pluginConfig = jobTaskService.getSingleTask(jobVersionId, inputPluginId);
        TableTransformFactory factory =
                FactoryUtil.discoverFactory(
                        Thread.currentThread().getContextClassLoader(),
                        TableTransformFactory.class,
                        "Sql");
        List<DatabaseTableSchemaReq> tableSchemaReqs = pluginConfig.getOutputSchema();
        if (tableSchemaReqs.isEmpty()) {
            throw new IllegalArgumentException("outputSchema is empty, please add input plugin");
        }
        DatabaseTableSchemaReq tableSchema = tableSchemaReqs.get(0);
        TableSchema.Builder builder = TableSchema.builder();
        List<String> primaryKeys = new ArrayList<>();
        for (TableField f : tableSchema.getFields()) {
            if (f.getPrimaryKey()) {
                primaryKeys.add(f.getName());
            }
            builder.column(
                    PhysicalColumn.of(
                            f.getName(),
                            stringToDataType(f.getOutputDataType()),
                            0,
                            f.getNullable(),
                            f.getDefaultValue(),
                            f.getComment()));
        }
        builder.primaryKey(PrimaryKey.of("PrimaryKeys", primaryKeys));

        CatalogTable table =
                CatalogTable.of(
                        TableIdentifier.of(
                                "default", tableSchema.getDatabase(), tableSchema.getTableName()),
                        builder.build(),
                        Collections.emptyMap(),
                        Collections.emptyList(),
                        tableSchema.getTableName());
        Map<String, Object> config = new HashMap<>();
        config.put(SQLTransform.KEY_QUERY.key(), sql.getQuery());
        TableFactoryContext context =
                new TableFactoryContext(
                        Collections.singletonList(table),
                        ReadonlyConfig.fromMap(config),
                        Thread.currentThread().getContextClassLoader());
        TableTransform<SeaTunnelRow> transform = factory.createTransform(context);
        SQLTransform sqlTransform = (SQLTransform) transform.createTransform();
        CatalogTable result = sqlTransform.getProducedCatalogTable();
        List<String> primaryKeysList = new ArrayList<>();
        if (result.getTableSchema().getPrimaryKey() != null) {
            primaryKeysList.addAll(result.getTableSchema().getPrimaryKey().getColumnNames());
        }
        List<TableField> fields = new ArrayList<>();
        for (Column column : result.getTableSchema().getColumns()) {
            TableField field = new TableField();
            field.setName(column.getName());
            field.setComment(column.getComment());
            field.setDefaultValue(
                    column.getDefaultValue() != null ? column.getDefaultValue().toString() : null);
            field.setNullable(column.isNullable());
            field.setOutputDataType(column.getDataType().toString());
            field.setPrimaryKey(primaryKeysList.contains(column.getName()));
            field.setType(column.getDataType().toString());
            fields.add(field);
        }

        TableSchemaReq tableSchemaRes = new TableSchemaReq();
        tableSchemaRes.setFields(fields);
        tableSchemaRes.setTableName(tableSchema.getTableName());
        return tableSchemaRes;
    }

    private SeaTunnelDataType<?> stringToDataType(String dataTypeStr) {
        dataTypeStr = dataTypeStr.toUpperCase();
        switch (dataTypeStr) {
            case "STRING":
                return BasicType.STRING_TYPE;
            case "BOOLEAN":
                return BasicType.BOOLEAN_TYPE;
            case "TINYINT":
                return BasicType.BYTE_TYPE;
            case "SMALLINT":
                return BasicType.SHORT_TYPE;
            case "INT":
                return BasicType.INT_TYPE;
            case "BIGINT":
                return BasicType.LONG_TYPE;
            case "FLOAT":
                return BasicType.FLOAT_TYPE;
            case "DOUBLE":
                return BasicType.DOUBLE_TYPE;
            case "NULL":
                return BasicType.VOID_TYPE;
            case "BYTES":
                return ArrayType.BYTE_ARRAY_TYPE;
            case "DATE":
                return LocalTimeType.LOCAL_DATE_TYPE;
            case "TIME":
                return LocalTimeType.LOCAL_TIME_TYPE;
            case "TIMESTAMP":
                return LocalTimeType.LOCAL_DATE_TIME_TYPE;
            case "DECIMAL":
                return new DecimalType(38, 18);
            case "ARRAY":
            case "MAP":
            case "ROW":
            case "MULTIPLE_ROW":
                return BasicType.STRING_TYPE;
            default:
                break;
        }

        Matcher matcher = decimalPattern.matcher(dataTypeStr);
        if (matcher.matches()) {
            int precision = Integer.parseInt(matcher.group(1));
            int scale = Integer.parseInt(matcher.group(2));
            return new DecimalType(precision, scale);
        }
        return BasicType.STRING_TYPE;
    }
}
