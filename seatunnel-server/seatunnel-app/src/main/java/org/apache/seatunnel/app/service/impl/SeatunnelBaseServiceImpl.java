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

import org.apache.seatunnel.app.service.ISeatunnelBaseService;
import org.apache.seatunnel.common.access.AccessInfo;
import org.apache.seatunnel.common.access.AccessType;
import org.apache.seatunnel.common.access.ResourceType;
import org.apache.seatunnel.common.access.SeatunnelAccessController;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SeatunnelBaseServiceImpl implements ISeatunnelBaseService {
    @Resource private SeatunnelAccessController seatunnelAccessController;

    @Override
    public void funcPermissionCheck(String permissionKey, int userId) {
        // Placeholder method: To be removed after thorough analysis of all references and usages.
    }

    @Override
    public void permissionCheck(
            String resourceName,
            ResourceType resourceType,
            AccessType accessType,
            AccessInfo accessInfo) {
        seatunnelAccessController.authorizeAccess(
                resourceName, resourceType, accessType, accessInfo);
    }

    @Override
    public boolean hasPermission(
            String resourceName,
            ResourceType resourceType,
            AccessType accessType,
            AccessInfo accessInfo) {
        return seatunnelAccessController.hasPermission(
                resourceName, resourceType, accessType, accessInfo);
    }
}
