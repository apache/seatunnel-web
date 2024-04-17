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
package org.apache.seatunnel.app.thirdparty.engine;

import org.apache.seatunnel.common.config.Common;
import org.apache.seatunnel.common.config.DeployMode;
import org.apache.seatunnel.engine.client.SeaTunnelClient;
import org.apache.seatunnel.engine.client.job.ClientJobProxy;
import org.apache.seatunnel.engine.client.job.JobClient;
import org.apache.seatunnel.engine.common.config.ConfigProvider;
import org.apache.seatunnel.engine.common.config.JobConfig;
import org.apache.seatunnel.engine.common.config.SeaTunnelConfig;
import org.apache.seatunnel.engine.common.config.YamlSeaTunnelConfigBuilder;
import org.apache.seatunnel.engine.core.job.JobDAGInfo;

import com.hazelcast.client.config.ClientConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
public class SeaTunnelEngineProxy {

    private ClientConfig clientConfig = null;

    private static class SeaTunnelEngineProxyHolder {
        private static final SeaTunnelEngineProxy INSTANCE = new SeaTunnelEngineProxy();
    }

    public static SeaTunnelEngineProxy getInstance() {
        return SeaTunnelEngineProxyHolder.INSTANCE;
    }

    private SeaTunnelEngineProxy() {
        clientConfig = ConfigProvider.locateAndGetClientConfig();
    }

    public String getMetricsContent(@NonNull String jobEngineId) {
        SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig);
        try {
            return seaTunnelClient.getJobMetrics(Long.valueOf(jobEngineId));
        } finally {
            seaTunnelClient.close();
        }
    }

    public String getJobPipelineStatusStr(@NonNull String jobEngineId) {
        SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig);
        try {
            return seaTunnelClient.getJobDetailStatus(Long.valueOf(jobEngineId));
        } finally {
            seaTunnelClient.close();
        }
    }

    public JobDAGInfo getJobInfo(@NonNull String jobEngineId) {
        SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig);
        try {
            return seaTunnelClient.getJobInfo(Long.valueOf(jobEngineId));
        } finally {
            seaTunnelClient.close();
        }
    }

    public String getJobStatus(@NonNull String jobEngineId) {
        SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig);
        try {
            return seaTunnelClient.getJobStatus(Long.valueOf(jobEngineId));
        } catch (Exception e) {
            log.warn("Can not get job from engine.", e);
            return null;
        } finally {
            seaTunnelClient.close();
        }
    }

    public Map<String, String> getClusterHealthMetrics() {
        SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig);
        try {
            return seaTunnelClient.getClusterHealthMetrics();
        } finally {
            seaTunnelClient.close();
        }
    }

    public String getAllRunningJobMetricsContent() {

        SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig);
        try {
            return seaTunnelClient.getJobClient().getRunningJobMetrics();
        } finally {
            seaTunnelClient.close();
        }
    }

    public ClientJobProxy executeJob(@NonNull String filePath, @NonNull Long jobInstanceId) {
        Common.setDeployMode(DeployMode.CLIENT);

        JobConfig jobConfig = new JobConfig();
        jobConfig.setName(jobInstanceId + "_job");
        SeaTunnelConfig seaTunnelConfig = new YamlSeaTunnelConfigBuilder().build();
        try (SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig)) {
            return seaTunnelClient.createExecutionContext(filePath, jobConfig, seaTunnelConfig).execute();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void restoreJob(@NonNull String filePath, @NonNull Long jobInstanceId, @NonNull Long jobEngineId) {

        JobConfig jobConfig = new JobConfig();
        jobConfig.setName(jobInstanceId + "_job");
        SeaTunnelConfig seaTunnelConfig = new YamlSeaTunnelConfigBuilder().build();
        try (SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig)) {
            seaTunnelClient
                    .restoreExecutionContext(filePath, jobConfig, seaTunnelConfig, jobEngineId)
                    .execute();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void pauseJob(@NonNull String jobEngineId) {
        try (SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig)) {
            seaTunnelClient.getJobClient().savePointJob(Long.valueOf(jobEngineId));
        } catch (Exception e) {
            log.warn("Can not pause job from engine.", e);
        }
    }

    public void deleteJob(@NonNull String jobEngineId) {
        try (SeaTunnelClient seaTunnelClient = new SeaTunnelClient(clientConfig)) {
            seaTunnelClient.getJobClient().cancelJob(Long.valueOf(jobEngineId));
        } catch (Exception e) {
            log.warn("Can not delete job from engine.", e);
        }
    }
}
