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
package org.apache.seatunnel.app.test;

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.common.SeaTunnelWebCluster;
import org.apache.seatunnel.app.controller.SeatunnelDatasourceControllerWrapper;
import org.apache.seatunnel.app.domain.request.datasource.DatasourceCheckReq;
import org.apache.seatunnel.app.domain.request.datasource.DatasourceReq;
import org.apache.seatunnel.app.domain.response.PageInfo;
import org.apache.seatunnel.app.domain.response.datasource.DatasourceDetailRes;
import org.apache.seatunnel.app.domain.response.datasource.DatasourceRes;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SeatunnelDatasourceControllerTest {
    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static SeatunnelDatasourceControllerWrapper seatunnelDatasourceControllerWrapper;
    private static String uniqueId = "_" + System.currentTimeMillis();

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        seatunnelDatasourceControllerWrapper = new SeatunnelDatasourceControllerWrapper();
    }

    @Test
    public void createDatasource_shouldReturnSuccess() {
        String datasourceId =
                seatunnelDatasourceControllerWrapper.createFakeSourceDatasource("ds1" + uniqueId);
        assertTrue(!datasourceId.isEmpty());
    }

    @Test
    public void testConnect_shouldReturnSuccess() {
        DatasourceCheckReq req = new DatasourceCheckReq();
        req.setPluginName("FakeSource");
        Map<String, String> datasourceConfig = new HashMap<>();
        datasourceConfig.put("fields", "{\"name\" : \"string\", \"age\" : \"int\"}");
        req.setDatasourceConfig(datasourceConfig);
        Result<Boolean> result = seatunnelDatasourceControllerWrapper.testConnect(req);
        assertTrue(result.isSuccess());
    }

    @Test
    public void updateDatasource_shouldReturnSuccess() {
        String datasourceId =
                seatunnelDatasourceControllerWrapper.createFakeSourceDatasource("ds2" + uniqueId);
        Result<DatasourceDetailRes> result =
                seatunnelDatasourceControllerWrapper.getDatasource(datasourceId);
        assertNotNull(result.getData());
        DatasourceReq req = new DatasourceReq();
        req.setDescription("new Description");
        // Populate req with valid data
        Result<Boolean> updateResult =
                seatunnelDatasourceControllerWrapper.updateDatasource(datasourceId, req);
        assertTrue(updateResult.isSuccess());
        result = seatunnelDatasourceControllerWrapper.getDatasource(datasourceId);
        assertEquals(req.getDescription(), result.getData().getDescription());
    }

    @Test
    public void deleteDatasource_shouldReturnSuccess() {
        String id =
                seatunnelDatasourceControllerWrapper.createFakeSourceDatasource("ds3" + uniqueId);
        Result<Boolean> result = seatunnelDatasourceControllerWrapper.deleteDatasource(id);
        assertTrue(result.isSuccess());
    }

    @Test
    public void getDatasourceDetail_shouldReturnSuccess() {
        String id = seatunnelDatasourceControllerWrapper.createConsoleDatasource("ds4" + uniqueId);
        Result<DatasourceDetailRes> result = seatunnelDatasourceControllerWrapper.getDatasource(id);
        assertTrue(result.isSuccess());
    }

    @Test
    public void getDatasourceDetailByName_shouldReturnSuccess() {
        String datasourceName = "ds5" + uniqueId;
        String id = seatunnelDatasourceControllerWrapper.createFakeSourceDatasource(datasourceName);
        Result<PageInfo<DatasourceRes>> datasourceList =
                seatunnelDatasourceControllerWrapper.getDatasourceList(
                        datasourceName, "FakeSource", 1, 10);
        assertTrue(datasourceList.isSuccess());
        assertNotNull(datasourceList.getData());
        assertEquals(1, datasourceList.getData().getData().size());
        assertEquals(id, datasourceList.getData().getData().get(0).getId());
    }

    @Test
    public void createDatasource_shouldFailIfDuplicate() {
        String datasourceName = "ds6" + uniqueId;
        String datasourceId =
                seatunnelDatasourceControllerWrapper.createFakeSourceDatasource(datasourceName);
        assertTrue(!datasourceId.isEmpty());

        DatasourceReq req =
                seatunnelDatasourceControllerWrapper.getFakeSourceDatasourceReq(datasourceName);
        Result<String> result = seatunnelDatasourceControllerWrapper.createDatasource(req);
        assertTrue(result.isFailed());
        assertEquals(SeatunnelErrorEnum.DATASOURCE_NAME_ALREADY_EXISTS.getCode(), result.getCode());
        assertEquals("datasource [" + datasourceName + "] already exists", result.getMsg());
    }

    @AfterAll
    public static void tearDown() {
        seaTunnelWebCluster.stop();
    }
}
