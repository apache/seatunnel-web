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
import org.apache.seatunnel.app.dal.entity.Workspace;
import org.apache.seatunnel.app.domain.request.workspace.WorkspaceReq;
import org.apache.seatunnel.app.service.WorkspaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seatunnel/api/v1/workspace")
public class WorkspaceController {

    @Autowired private WorkspaceService workspaceService;

    @PostMapping("/create")
    public Result<Long> createWorkspace(@RequestBody WorkspaceReq workspaceReq) {
        return Result.success(workspaceService.createWorkspace(workspaceReq));
    }

    @PutMapping("/update/{id}")
    public Result<Boolean> updateWorkspace(
            @PathVariable Long id, @RequestBody WorkspaceReq workspaceReq) {
        return Result.success(workspaceService.updateWorkspace(id, workspaceReq));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteWorkspace(@PathVariable Long id) {
        return Result.success(workspaceService.deleteWorkspace(id));
    }

    @GetMapping("/list")
    public Result<List<Workspace>> getAllWorkspaces() {
        return Result.success(workspaceService.getAllWorkspaces());
    }

    @GetMapping("/list/{id}")
    public Result<Workspace> getWorkspace(@PathVariable Long id) {
        Workspace workspaceById = workspaceService.getWorkspace(id);
        return Result.success(workspaceById);
    }
}
