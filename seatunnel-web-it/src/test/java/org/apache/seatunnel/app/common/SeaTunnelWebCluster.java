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

import org.apache.seatunnel.app.SeatunnelApplication;
import org.apache.seatunnel.common.utils.ExceptionUtils;
import org.apache.seatunnel.engine.common.config.ConfigProvider;
import org.apache.seatunnel.engine.common.config.SeaTunnelConfig;
import org.apache.seatunnel.engine.server.SeaTunnelServer;
import org.apache.seatunnel.engine.server.SeaTunnelServerStarter;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.hazelcast.config.Config;
import com.hazelcast.instance.impl.HazelcastInstanceImpl;
import com.hazelcast.logging.ILogger;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class SeaTunnelWebCluster {
    private static final String DB_TYPE_H2 = "h2";
    private static final String DB_TYPE_MYSQL = "mysql";
    private static final Set<String> SUPPORTED_DB_TYPES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(DB_TYPE_H2, DB_TYPE_MYSQL)));
    private SeaTunnelServer server;
    private HazelcastInstanceImpl instance;
    private ConfigurableApplicationContext applicationContext;

    public void start() {
        String seatunnelHome = System.getProperty("SEATUNNEL_HOME");
        if (seatunnelHome == null) {
            throw new RuntimeException(
                    "SEATUNNEL_HOME is not set. Please set it before running the tests.");
        }
        if (!new File(seatunnelHome).exists()) {
            throw new RuntimeException(
                    seatunnelHome
                            + " does not exist. Please make sure it exists before running the tests");
        }
        Config hazelcastConfig = Config.loadDefault();
        SeaTunnelConfig seaTunnelConfig = ConfigProvider.locateAndGetSeaTunnelConfig();
        seaTunnelConfig.setHazelcastConfig(hazelcastConfig);
        instance = SeaTunnelServerStarter.createHazelcastInstance(seaTunnelConfig);
        server = instance.node.nodeEngine.getService(SeaTunnelServer.SERVICE_NAME);
        ILogger LOGGER = instance.node.nodeEngine.getLogger(SeaTunnelWebCluster.class);

        // For integration tests, default database is H2, so the default spring profile is h2
        String dbType = System.getProperty("dbType", DB_TYPE_H2);
        if (!SUPPORTED_DB_TYPES.contains(dbType)) {
            throw new IllegalArgumentException(
                    "Invalid dbType: " + dbType + ". Supported values are: " + SUPPORTED_DB_TYPES);
        }

        String springProfile = dbType.equals(DB_TYPE_MYSQL) ? "default" : "h2";
        String[] args = {"--spring.profiles.active=" + springProfile};

        applicationContext = SpringApplication.run(SeatunnelApplication.class, args);
        LOGGER.info("SeaTunnel-web server started.");
        assertTrue(isRunning());
    }

    public boolean isRunning() {
        return server.isMasterNode();
    }

    public void stop() {
        try {
            if (applicationContext != null) {
                int exit = SpringApplication.exit(applicationContext);
                log.info("Sea tunnel application exited with code: {}", exit);
            }
        } catch (Throwable throwable) {
            log.error("Error stopping application context", throwable);
        }

        try {
            if (server != null) {
                server.shutdown(true);
            }

            if (instance != null) {
                instance.shutdown();
            }
        } catch (Exception e) {
            log.error(ExceptionUtils.getMessage(e));
        }
    }
}
