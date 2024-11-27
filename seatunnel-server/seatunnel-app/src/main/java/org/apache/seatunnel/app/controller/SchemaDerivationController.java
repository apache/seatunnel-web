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
import org.apache.seatunnel.app.domain.request.job.TableSchemaReq;
import org.apache.seatunnel.app.domain.request.job.transform.SQL;
import org.apache.seatunnel.app.service.ISchemaDerivationService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiParam;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/seatunnel/api/v1/schema/derivation")
public class SchemaDerivationController {

    @Resource private ISchemaDerivationService schemaDerivationService;

    @PostMapping("/sql")
    Result<TableSchemaReq> SQLSchemaDerivation(
            @ApiParam(value = "job version id", required = true) @RequestParam long jobVersionId,
            @ApiParam(value = "inputPluginId", required = true) @RequestParam String inputPluginId,
            @RequestBody SQL sql) {
        return Result.success(
                schemaDerivationService.derivationSQL(jobVersionId, inputPluginId, sql));
    }
}
