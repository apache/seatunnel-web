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
package org.apache.seatunnel.app.controller;

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.common.SeatunnelWebTestingBase;
import org.apache.seatunnel.app.domain.request.connector.SceneMode;
import org.apache.seatunnel.app.domain.request.job.DataSourceOption;
import org.apache.seatunnel.app.domain.request.job.DatabaseTableSchemaReq;
import org.apache.seatunnel.app.domain.request.job.JobDAG;
import org.apache.seatunnel.app.domain.request.job.JobTaskInfo;
import org.apache.seatunnel.app.domain.request.job.PluginConfig;
import org.apache.seatunnel.app.domain.request.job.SelectTableFields;
import org.apache.seatunnel.app.domain.response.job.JobTaskCheckRes;
import org.apache.seatunnel.app.utils.JSONTestUtils;
import org.apache.seatunnel.common.constants.PluginType;
import org.apache.seatunnel.common.utils.JsonUtils;
import org.apache.seatunnel.datasource.plugin.api.model.TableField;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobTaskControllerWrapper extends SeatunnelWebTestingBase {

    public Result<JobTaskCheckRes> saveJobDAG(long jobVersionId, JobDAG jobDAG) {
        String requestBody = JsonUtils.toJsonString(jobDAG);
        String response = sendRequest(url("job/dag/" + jobVersionId), requestBody, "POST");
        return JSONTestUtils.parseObject(response, new TypeReference<Result<JobTaskCheckRes>>() {});
    }

    public Result<JobTaskInfo> getJob(long jobVersionId) {
        String response = sendRequest(url("job/" + jobVersionId));
        return JSONTestUtils.parseObject(response, new TypeReference<Result<JobTaskInfo>>() {});
    }

    public Result<Void> saveSingleTask(long jobVersionId, PluginConfig pluginConfig) {
        String requestBody = JsonUtils.toJsonString(pluginConfig);
        String response = sendRequest(url("job/task/" + jobVersionId), requestBody, "POST");
        return JSONTestUtils.parseObject(response, Result.class);
    }

    public Result<PluginConfig> getSingleTask(long jobVersionId, String pluginId) {
        String response = sendRequest(url("job/task/" + jobVersionId) + "pluginId=" + pluginId);
        return JSONTestUtils.parseObject(response, new TypeReference<Result<PluginConfig>>() {});
    }

    public Result<Void> deleteSingleTask(long jobVersionId, String pluginId) {
        String response =
                sendRequest(
                        url("job/task/" + jobVersionId) + "pluginId=" + pluginId, null, "DELETE");
        return JSONTestUtils.parseObject(response, Result.class);
    }

    public String createFakeSourcePlugin(String datasourceId, long jobVersionId, String rows) {
        DataSourceOption tableOption = new DataSourceOption();
        tableOption.setDatabases(Collections.singletonList("fake_database"));
        tableOption.setTables(Collections.singletonList("fake_table"));
        String sourcePluginId = "src_" + System.currentTimeMillis();
        PluginConfig sourcePluginConfig =
                PluginConfig.builder()
                        .pluginId(sourcePluginId)
                        .name("source-fakesource")
                        .type(PluginType.SOURCE)
                        .tableOption(tableOption)
                        .selectTableFields(getSelectTableFields())
                        .transformOptions(null)
                        .outputSchema(getOutputSchema())
                        .dataSourceId(Long.parseLong(datasourceId))
                        .sceneMode(SceneMode.SINGLE_TABLE)
                        .config(
                                "{\"query\":\"\",\"tables_configs\":\"\",\"schema\":\"fields {\\n        name = \\\"string\\\"\\n        age = \\\"int\\\"\\n      }\",\"string.fake.mode\":\"RANGE\",\"string.template\":\"\",\"tinyint.fake.mode\":\"RANGE\",\"tinyint.template\":\"\",\"smallint.fake.mode\":\"RANGE\",\"smallint.template\":\"\",\"int.fake.mode\":\"RANGE\",\"int.template\":\"\",\"bigint.fake.mode\":\"RANGE\",\"bigint.template\":\"\",\"float.fake.mode\":\"RANGE\",\"float.template\":\"\",\"double.fake.mode\":\"RANGE\",\"double.template\":\"\",\"rows\":\""
                                        + rows
                                        + "\",\"row.num\":5,\"split.num\":1,\"split.read-interval\":1,\"map.size\":5,\"array.size\":5,\"bytes.length\":5,\"date.year.template\":\"\",\"date.month.template\":\"\",\"date.day.template\":\"\",\"time.hour.template\":\"\",\"time.minute.template\":\"\",\"time.second.template\":\"\",\"parallelism\":1}")
                        .build();

        Result<Void> srcResult = saveSingleTask(jobVersionId, sourcePluginConfig);
        assertTrue(srcResult.isSuccess());
        return sourcePluginId;
    }

    public String createFakeSourcePlugin(String datasourceId, long jobVersionId) {
        return createFakeSourcePlugin(datasourceId, jobVersionId, "");
    }

    public String createFakeSourcePluginThatFails(String datasourceId, long jobVersionId) {
        String rows =
                "[{kind=INSERT, fields=[\"org\", 100]}, {kind=INSERT, fields=[\"apache\", 50]}, {kind=INSERT, fields=[\"seatunnel\", 25]}, {kind=INSERT, fields=[\"seatunnel-web\", 12]}, {kind=INSERT, fields=[\"etl\", 6_age_invalid_number]}]";
        return createFakeSourcePlugin(datasourceId, jobVersionId, rows);
    }

    public String createConsoleSinkPlugin(String datasourceId, long jobVersionId) {
        DataSourceOption sinkTableOption = new DataSourceOption();
        sinkTableOption.setDatabases(Collections.singletonList("console_fake_database"));
        sinkTableOption.setTables(Collections.singletonList("console_fake_table"));

        String sinkPluginId = "sink_" + System.currentTimeMillis();
        PluginConfig sinkPluginConfig =
                PluginConfig.builder()
                        .pluginId(sinkPluginId)
                        .name("sink-console")
                        .type(PluginType.SINK)
                        .tableOption(sinkTableOption)
                        .selectTableFields(getSelectTableFields())
                        .transformOptions(null)
                        .outputSchema(null)
                        .dataSourceId(Long.parseLong(datasourceId))
                        .sceneMode(SceneMode.SINGLE_TABLE)
                        .config("{\"query\":\"\"}")
                        .build();

        Result<Void> sinkResult = saveSingleTask(jobVersionId, sinkPluginConfig);
        assertTrue(sinkResult.isSuccess());
        return sinkPluginId;
    }

    public String createReplaceTransformPlugin(long jobVersionId) {
        String transPluginId = "trans_" + System.currentTimeMillis();
        PluginConfig transformPluginConfig =
                PluginConfig.builder()
                        .pluginId(transPluginId)
                        .name("transform-replace")
                        .type(PluginType.TRANSFORM)
                        .connectorType("Replace")
                        .transformOptions(null)
                        .outputSchema(null)
                        .sceneMode(SceneMode.SINGLE_TABLE)
                        .config(
                                "{\"query\":\"\",\"replace_field\":\"name\",\"pattern\":\"OK\",\"replacement\":\"ITS OK.\",\"is_regex\":\"false\",\"replace_first\":null}")
                        .build();
        Result<Void> transResult = saveSingleTask(jobVersionId, transformPluginConfig);
        assertTrue(transResult.isSuccess());
        return transPluginId;
    }

    private List<DatabaseTableSchemaReq> getOutputSchema() {
        DatabaseTableSchemaReq databaseTableSchemaReq = new DatabaseTableSchemaReq();
        databaseTableSchemaReq.setDatabase("fake_database");
        databaseTableSchemaReq.setTableName("fake_table");
        databaseTableSchemaReq.setFields(createFields());
        return Collections.singletonList(databaseTableSchemaReq);
    }

    private List<TableField> createFields() {
        List<TableField> fields = new ArrayList<>();
        fields.add(
                createTableField("string", "name", "", true, null, false, null, false, "STRING"));
        fields.add(createTableField("int", "age", "", false, null, false, null, false, "INT"));
        return fields;
    }

    private TableField createTableField(
            String type,
            String name,
            String comment,
            Boolean primaryKey,
            String defaultValue,
            Boolean nullable,
            Map<String, String> properties,
            Boolean unSupport,
            String outputDataType) {
        TableField field = new TableField();
        field.setType(type);
        field.setName(name);
        field.setComment(comment);
        field.setPrimaryKey(primaryKey);
        field.setDefaultValue(defaultValue);
        field.setNullable(nullable);
        field.setProperties(properties);
        field.setUnSupport(unSupport);
        field.setOutputDataType(outputDataType);
        return field;
    }

    private SelectTableFields getSelectTableFields() {
        SelectTableFields selectTableFields = new SelectTableFields();
        selectTableFields.setAll(true);
        List<String> tableFields = Arrays.asList("name", "age");
        selectTableFields.setTableFields(tableFields);
        return selectTableFields;
    }
}
