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
import org.apache.seatunnel.app.service.IDatasourceService;
import org.apache.seatunnel.app.service.IJobDefinitionService;
import org.apache.seatunnel.app.service.IUserService;
import org.apache.seatunnel.app.service.IVirtualTableService;
import org.apache.seatunnel.app.service.WorkspaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seatunnel/api/v1/resources")
public class ResourceNameProviderController {

    @Autowired private WorkspaceService workspaceService;

    @Autowired private IDatasourceService datasourceService;

    @Autowired private IJobDefinitionService jobDefinitionService;

    @Autowired private IVirtualTableService virtualTableService;

    @Autowired private IUserService userService;

    @GetMapping("/workspace")
    public Result<List<String>> getWorkspaces(
            @RequestParam(value = "searchName", required = false) String searchName) {
        return Result.success(workspaceService.getWorkspaceNames(searchName));
    }

    @GetMapping("/datasource")
    public Result<List<String>> getDatasources(
            @RequestParam(value = "workspaceName", required = false) String workspaceName,
            @RequestParam(value = "searchName", required = false) String searchName) {
        return Result.success(datasourceService.getDatasourceNames(workspaceName, searchName));
    }

    @GetMapping("/job_definition")
    public Result<List<String>> getJobDefinitions(
            @RequestParam(value = "workspaceName", required = false) String workspaceName,
            @RequestParam(value = "searchName", required = false) String searchName) {
        return Result.success(
                jobDefinitionService.getJobDefinitionNames(workspaceName, searchName));
    }

    @GetMapping("/virtual_table")
    public Result<List<String>> getVirtualTables(
            @RequestParam(value = "workspaceName", required = false) String workspaceName,
            @RequestParam(value = "searchName", required = false) String searchName) {
        return Result.success(
                virtualTableService.getVirtualTableNamesWithinWorkspace(workspaceName, searchName));
    }

    @GetMapping("/user")
    public Result<List<String>> getUserNames(
            @RequestParam(value = "searchName", required = false) String searchName) {
        return Result.success(userService.getUserNames(searchName));
    }
}
