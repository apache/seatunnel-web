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

package org.apache.seatunnel.app.common;

import org.apache.seatunnel.common.access.AccessDeniedException;
import org.apache.seatunnel.common.access.AccessInfo;
import org.apache.seatunnel.common.access.AccessType;
import org.apache.seatunnel.common.access.ResourceType;
import org.apache.seatunnel.common.access.SeatunnelAccessController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is intended to provide basic access control functionality for testing purposes, rather
 * than simulating a full-fledged Ranger access controller.
 */
public class AccessControllerTestingImp implements SeatunnelAccessController {
    private static boolean isAccessControllerEnabled = false;
    private static final Map<String, List<ResourcePermissionData>> permissionList = new HashMap<>();

    public static void resetResourcePermission(String username, ResourcePermissionData permission) {
        clearPermission();
        addResourcePermission(username, permission);
    }

    public static void addResourcePermission(String username, ResourcePermissionData permission) {
        List<ResourcePermissionData> resourcePermissionDataList =
                permissionList.computeIfAbsent(username, k -> new ArrayList<>());
        resourcePermissionDataList.add(permission);
    }

    public static void enableAccessController() {
        isAccessControllerEnabled = true;
    }

    public static void disableAccessController() {
        isAccessControllerEnabled = false;
    }

    public static void clearPermission() {
        permissionList.clear();
    }

    @Override
    public void authorizeAccess(
            String resourceName,
            ResourceType resourceType,
            AccessType accessType,
            AccessInfo accessInfo) {
        if (!hasPermission(resourceName, resourceType, accessType, accessInfo)) {
            AccessDeniedException.accessDenied(
                    accessInfo.getUsername(), resourceName, resourceType, accessType);
        }
    }

    @Override
    public boolean hasPermission(
            String resourceName,
            ResourceType resourceType,
            AccessType accessType,
            AccessInfo accessInfo) {
        if (!isAccessControllerEnabled) {
            return true;
        }
        List<ResourcePermissionData> permissions = permissionList.get(accessInfo.getUsername());
        if (permissions != null) {
            for (ResourcePermissionData permission : permissions) {
                if (resourceType == ResourceType.USER || resourceType == ResourceType.WORKSPACE) {
                    // Do not consider workspace name
                    if (permission.getResourceName().equals(resourceName)
                            && permission.getResourceType() == resourceType
                            && permission.getAccessTypes().contains(accessType)) {
                        return true;
                    }
                } else {
                    if (permission.getWorkspaceName().equals(accessInfo.getWorkspaceName())
                            && permission.getResourceName().equals(resourceName)
                            && permission.getResourceType() == resourceType
                            && permission.getAccessTypes().contains(accessType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
