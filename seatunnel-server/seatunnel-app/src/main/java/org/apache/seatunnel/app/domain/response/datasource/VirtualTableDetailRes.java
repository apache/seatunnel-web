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

package org.apache.seatunnel.app.domain.response.datasource;

import org.apache.seatunnel.app.domain.response.BaseInfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@ApiModel(value = "VirtualTable Detail Response", description = "virtual table detail info")
public class VirtualTableDetailRes extends BaseInfo {

    @ApiModelProperty(value = "table id", required = true, dataType = "String")
    private String tableId;

    @ApiModelProperty(value = "datasource id", required = true, dataType = "String")
    private String datasourceId;

    @ApiModelProperty(value = "datasource name", required = true, dataType = "String")
    private String datasourceName;

    @ApiModelProperty(value = "database name", required = true, dataType = "String")
    private String databaseName;

    @ApiModelProperty(value = "plugin name", required = true, dataType = "String")
    private String pluginName;

    @ApiModelProperty(value = "table name", required = true, dataType = "String")
    private String tableName;

    @ApiModelProperty(value = "table description", dataType = "String")
    private String description;

    @ApiModelProperty(value = "table properties", dataType = "List")
    private List<VirtualTableFieldRes> fields;

    private Map<String, String> datasourceProperties;
}
