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
import org.apache.seatunnel.app.controller.ConnectorControllerWrapper;
import org.apache.seatunnel.app.domain.response.connector.ConnectorInfo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConnectorControllerTest {
    private static final SeaTunnelWebCluster seaTunnelWebCluster = new SeaTunnelWebCluster();
    private static ConnectorControllerWrapper connectorControllerWrapper;

    @BeforeAll
    public static void setUp() {
        seaTunnelWebCluster.start();
        connectorControllerWrapper = new ConnectorControllerWrapper();
    }

    @Test
    public void testListAllTransform() {
        List<ConnectorInfo> listResult = connectorControllerWrapper.listAllTransform();
        assertFalse(listResult.isEmpty());
    }

    @Test
    public void testListSource() {
        List<ConnectorInfo> result = connectorControllerWrapper.listSource("ALL");
        assertFalse(result.isEmpty());
    }

    @Test
    public void testListSink() {
        List<ConnectorInfo> result = connectorControllerWrapper.listSink("ALL");
        assertFalse(result.isEmpty());
    }

    @Test
    void testSync() {
        Result<Void> sync = connectorControllerWrapper.sync();
        assertTrue(sync.isSuccess());
    }

    @Test
    void testGetConnectorFormStructure() {
        Result<Void> connectorFormStructure =
                connectorControllerWrapper.getConnectorFormStructure("source", "FakeSource");
        assertTrue(connectorFormStructure.isSuccess());
    }

    @AfterAll
    public static void tearDown() {
        seaTunnelWebCluster.stop();
    }
}
