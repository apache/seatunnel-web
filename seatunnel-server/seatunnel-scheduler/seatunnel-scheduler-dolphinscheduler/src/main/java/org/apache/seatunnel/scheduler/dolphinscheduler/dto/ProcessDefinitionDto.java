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

package org.apache.seatunnel.scheduler.dolphinscheduler.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

import static org.apache.seatunnel.server.common.DateUtils.DEFAULT_DATETIME_FORMAT;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessDefinitionDto {
    private int id;
    private long code;
    private String name;
    private String releaseState;
    private long projectCode;
    private String description;

    @JsonFormat(pattern = DEFAULT_DATETIME_FORMAT)
    private Date createTime;

    @JsonFormat(pattern = DEFAULT_DATETIME_FORMAT)
    private Date updateTime;

    private String userName;
    private String projectName;
    private String locations;
    private String scheduleReleaseState;
    private int timeout;
    private int tenantId;
    private String tenantCode;
    private String modifyBy;
    private int warningGroupId;
}
