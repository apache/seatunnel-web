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

import org.apache.seatunnel.app.dal.entity.JobInstanceHistory;
import org.apache.seatunnel.app.dal.entity.JobMetrics;
import org.apache.seatunnel.app.thirdparty.metrics.IEngineMetricsExtractor;
import org.apache.seatunnel.app.utils.JSONUtils;
import org.apache.seatunnel.engine.core.job.JobDAGInfo;
import org.apache.seatunnel.engine.core.job.JobStatus;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

@Slf4j
/** Engine metrics extractor SeaTunnel Engine implement. */
public class SeaTunnelEngineMetricsExtractor implements IEngineMetricsExtractor {
    @Getter @Setter private SeaTunnelEngineProxy seaTunnelEngineProxy;

    public static final String[] clusterHealthMetricsKeys =
            new String[] {
                "processors",
                "load.systemAverage",
                "physical.memory.total",
                "physical.memory.free",
                "swap.space.total",
                "swap.space.free",
                "heap.memory.used",
                "heap.memory.free",
                "heap.memory.total",
                "heap.memory.max",
                "heap.memory.used/total",
                "heap.memory.used/max",
                "minor.gc.count",
                "minor.gc.time",
                "major.gc.count",
                "major.gc.time",
                "thread.count",
                "thread.peakCount",
                "operations.completed.count",
                "operations.running.count",
                "operations.pending.invocations.percentage",
                "operations.pending.invocations.count",
                "clientEndpoint.count",
                "connection.active.count",
                "client.connection.count",
                "connection.count"
            };

    private SeaTunnelEngineMetricsExtractor() {
        this.seaTunnelEngineProxy = SeaTunnelEngineProxy.getInstance();
    }

    public static SeaTunnelEngineMetricsExtractor getInstance() {
        return SeaTunnelEngineMetricsExtractorHolder.INSTANCE;
    }

    @Override
    public List<JobMetrics> getMetricsByJobEngineId(@NonNull String jobEngineId) {

        LinkedHashMap<Integer, String> jobPipelineStatus = getJobPipelineStatus(jobEngineId);
        String metricsContent = seaTunnelEngineProxy.getMetricsContent(jobEngineId);
        if (StringUtils.isEmpty(metricsContent)) {
            return new ArrayList<>();
        }
        LinkedHashMap<Integer, JobMetrics> metricsMap =
                extractMetrics(
                        jobPipelineStatus, seaTunnelEngineProxy.getMetricsContent(jobEngineId));
        return Arrays.asList(metricsMap.values().toArray(new JobMetrics[0]));
    }

    @Override
    public LinkedHashMap<Integer, String> getJobPipelineStatus(@NonNull String jobEngineId) {
        String jobState = seaTunnelEngineProxy.getJobPipelineStatusStr(jobEngineId);
        LinkedHashMap<Integer, String> pipelineStatusMap = new LinkedHashMap<>();
        String err = JSONUtils.getNodeString(jobState, "err");
        if (StringUtils.isNotBlank(err)) {
            throw new SeatunnelException(SeatunnelErrorEnum.LOAD_ENGINE_METRICS_ERROR, err);
        }
        String pipelineStateMapperMap = JSONUtils.getNodeString(jobState, "pipelineStateMapperMap");
        for (Map.Entry<String, String> next : JSONUtils.toMap(pipelineStateMapperMap).entrySet()) {
            // "PipelineLocation(jobId=650612768629587969, pipelineId=2)"
            String pipelineLocation = next.getKey();
            String pipelineId =
                    pipelineLocation.substring(
                            pipelineLocation.lastIndexOf("=") + 1, pipelineLocation.length() - 1);
            String pipelineStatus = JSONUtils.getNodeString(next.getValue(), "pipelineStatus");
            pipelineStatusMap.put(Integer.valueOf(pipelineId), pipelineStatus);
        }
        return pipelineStatusMap;
    }

    @Override
    public JobInstanceHistory getJobHistoryById(String jobEngineId) {
        JobDAGInfo jobInfo = seaTunnelEngineProxy.getJobInfo(jobEngineId);
        JobInstanceHistory jobInstanceHistory = new JobInstanceHistory();
        jobInstanceHistory.setDag(JSONUtils.toJsonString(jobInfo));
        return jobInstanceHistory;
    }

    @Override
    public boolean isJobEnd(@NonNull String jobEngineId) {
        JobStatus jobStatus = seaTunnelEngineProxy.getJobStatus(jobEngineId);
        return jobStatus == JobStatus.FINISHED
                || jobStatus == JobStatus.CANCELED
                || jobStatus == JobStatus.FAILED;
    }

    @Override
    public JobStatus getJobStatus(@NonNull String jobEngineId) {
        return seaTunnelEngineProxy.getJobStatus(jobEngineId);
    }

    @Override
    public List<Map<String, String>> getClusterHealthMetrics() {
        List<Map<String, String>> zetaClusterMetrics = new ArrayList<>();
        Map<String, String> clusterHealthMetrics = seaTunnelEngineProxy.getClusterHealthMetrics();
        for (Map.Entry<String, String> entry : clusterHealthMetrics.entrySet()) {
            Map<String, String> hostMetrics = new LinkedHashMap<>();
            String[] split = entry.getKey().split(":");
            hostMetrics.put("host", split[0]);
            hostMetrics.put("port", split[1]);

            String value = entry.getValue();
            value = value.replace(" ", "");
            Map<String, String> otherMetrics = JSONUtils.toMap(value);
            for (String key : clusterHealthMetricsKeys) {
                hostMetrics.put(key, otherMetrics.get(key));
            }
            zetaClusterMetrics.add(hostMetrics);
        }
        return zetaClusterMetrics;
    }

    @Override
    public Map<Integer, JobMetrics> getMetricsByJobEngineIdRTMap(@NonNull String jobEngineId) {
        LinkedHashMap<Integer, String> jobPipelineStatus = getJobPipelineStatus(jobEngineId);
        String metricsContent = seaTunnelEngineProxy.getMetricsContent(jobEngineId);
        if (StringUtils.isEmpty(metricsContent)) {
            return new HashMap<>();
        }
        return extractMetrics(jobPipelineStatus, metricsContent);
    }

    private LinkedHashMap<Integer, JobMetrics> extractMetrics(
            LinkedHashMap<Integer, String> jobPipelineStatus, String metrics) {
        LinkedHashMap<Integer, JobMetrics> metricsMap = new LinkedHashMap<>();
        String sourceReceivedCountStr = JSONUtils.getNodeString(metrics, "SourceReceivedCount");
        JsonNode sourceReceivedCount = JSONUtils.toJsonNode(sourceReceivedCountStr);
        if (sourceReceivedCount != null && sourceReceivedCount.isArray()) {
            for (JsonNode node : sourceReceivedCount) {
                Integer pipelineId = node.get("tags").get("pipelineId").asInt();
                JobMetrics currPipelineMetrics =
                        getOrCreatePipelineMetricsMap(metricsMap, jobPipelineStatus, pipelineId);
                currPipelineMetrics.setReadRowCount(
                        currPipelineMetrics.getReadRowCount() + node.get("value").asLong());
            }
        }

        String sinkWriteCountStr = JSONUtils.getNodeString(metrics, "SinkWriteCount");
        JsonNode sinkWriteCount = JSONUtils.toJsonNode(sinkWriteCountStr);
        if (sinkWriteCount != null && sinkWriteCount.isArray()) {
            for (JsonNode node : sinkWriteCount) {
                Integer pipelineId = node.get("tags").get("pipelineId").asInt();
                JobMetrics currPipelineMetrics =
                        getOrCreatePipelineMetricsMap(metricsMap, jobPipelineStatus, pipelineId);
                currPipelineMetrics.setWriteRowCount(
                        currPipelineMetrics.getWriteRowCount() + node.get("value").asLong());
            }
        }

        String sinkWriteQPSStr = JSONUtils.getNodeString(metrics, "SinkWriteQPS");
        JsonNode sinkWriteQPS = JSONUtils.toJsonNode(sinkWriteQPSStr);
        if (sinkWriteQPS != null && sinkWriteQPS.isArray()) {
            for (JsonNode node : sinkWriteQPS) {
                Integer pipelineId = node.get("tags").get("pipelineId").asInt();
                JobMetrics currPipelineMetrics =
                        getOrCreatePipelineMetricsMap(metricsMap, jobPipelineStatus, pipelineId);
                currPipelineMetrics.setWriteQps(
                        currPipelineMetrics.getWriteQps()
                                + (new Double(node.get("value").asDouble())).longValue());
            }
        }

        String sourceReceivedQPSStr = JSONUtils.getNodeString(metrics, "SourceReceivedQPS");
        JsonNode sourceReceivedQPS = JSONUtils.toJsonNode(sourceReceivedQPSStr);
        if (sourceReceivedQPS != null && sourceReceivedQPS.isArray()) {
            for (JsonNode node : sourceReceivedQPS) {
                Integer pipelineId = node.get("tags").get("pipelineId").asInt();
                JobMetrics currPipelineMetrics =
                        getOrCreatePipelineMetricsMap(metricsMap, jobPipelineStatus, pipelineId);
                currPipelineMetrics.setReadQps(
                        currPipelineMetrics.getReadQps()
                                + (new Double(node.get("value").asDouble())).longValue());
            }
        }

        String cdcRecordEmitDelayStr = JSONUtils.getNodeString(metrics, "CDCRecordEmitDelay");
        JsonNode cdcRecordEmitDelay = JSONUtils.toJsonNode(cdcRecordEmitDelayStr);
        if (cdcRecordEmitDelay != null && cdcRecordEmitDelay.isArray()) {
            Map<Integer, List<Long>> dataMap = new HashMap<>();
            for (JsonNode node : cdcRecordEmitDelay) {
                Integer pipelineId = node.get("tags").get("pipelineId").asInt();
                long value = node.get("value").asLong();
                dataMap.computeIfAbsent(pipelineId, n -> new ArrayList<>()).add(value);
            }
            dataMap.forEach(
                    (key, value) -> {
                        JobMetrics currPipelineMetrics =
                                getOrCreatePipelineMetricsMap(metricsMap, jobPipelineStatus, key);
                        OptionalDouble average = value.stream().mapToDouble(a -> a).average();
                        currPipelineMetrics.setRecordDelay(
                                Double.valueOf(average.isPresent() ? average.getAsDouble() : 0)
                                        .longValue());
                    });
        }
        return metricsMap;
    }

    @Override
    public Map<Long, HashMap<Integer, JobMetrics>> getAllRunningJobMetrics() {
        HashMap<Long, HashMap<Integer, JobMetrics>> allRunningJobMetricsHashMap = new HashMap<>();

        try {
            String allJobMetricsContent = seaTunnelEngineProxy.getAllRunningJobMetricsContent();

            if (StringUtils.isEmpty(allJobMetricsContent)) {
                return new HashMap<>();
            }
            JsonNode jsonNode = JSONUtils.toJsonNode(allJobMetricsContent);
            for (JsonNode item : jsonNode) {
                LinkedHashMap<Integer, JobMetrics> metricsMap = new LinkedHashMap<>();

                JsonNode sourceReceivedCount = item.get("metrics").get("SourceReceivedCount");
                long jobEngineId = 0L;
                if (sourceReceivedCount != null && sourceReceivedCount.isArray()) {
                    for (JsonNode node : sourceReceivedCount) {
                        jobEngineId = node.get("tags").get("jobId").asLong();
                        Integer pipelineId = node.get("tags").get("pipelineId").asInt();
                        JobMetrics currPipelineMetrics =
                                getOrCreatePipelineMetricsMapStatusRunning(metricsMap, pipelineId);
                        currPipelineMetrics.setReadRowCount(
                                currPipelineMetrics.getReadRowCount() + node.get("value").asLong());
                    }
                }

                JsonNode sinkWriteCount = item.get("metrics").get("SinkWriteCount");
                if (sinkWriteCount != null && sinkWriteCount.isArray()) {
                    for (JsonNode node : sinkWriteCount) {
                        jobEngineId = node.get("tags").get("jobId").asLong();
                        Integer pipelineId = node.get("tags").get("pipelineId").asInt();
                        JobMetrics currPipelineMetrics =
                                getOrCreatePipelineMetricsMapStatusRunning(metricsMap, pipelineId);
                        currPipelineMetrics.setWriteRowCount(
                                currPipelineMetrics.getWriteRowCount()
                                        + node.get("value").asLong());
                    }
                }

                JsonNode sinkWriteQPS = item.get("metrics").get("SinkWriteQPS");
                if (sinkWriteQPS != null && sinkWriteQPS.isArray()) {
                    for (JsonNode node : sinkWriteQPS) {
                        Integer pipelineId = node.get("tags").get("pipelineId").asInt();
                        JobMetrics currPipelineMetrics =
                                getOrCreatePipelineMetricsMapStatusRunning(metricsMap, pipelineId);
                        currPipelineMetrics.setWriteQps(
                                currPipelineMetrics.getWriteQps()
                                        + (new Double(node.get("value").asDouble())).longValue());
                    }
                }

                JsonNode sourceReceivedQPS = item.get("metrics").get("SourceReceivedQPS");
                if (sourceReceivedQPS != null && sourceReceivedQPS.isArray()) {
                    for (JsonNode node : sourceReceivedQPS) {
                        Integer pipelineId = node.get("tags").get("pipelineId").asInt();
                        JobMetrics currPipelineMetrics =
                                getOrCreatePipelineMetricsMapStatusRunning(metricsMap, pipelineId);
                        currPipelineMetrics.setReadQps(
                                currPipelineMetrics.getReadQps()
                                        + (new Double(node.get("value").asDouble())).longValue());
                    }
                }

                JsonNode cdcRecordEmitDelay = item.get("metrics").get("CDCRecordEmitDelay");
                if (cdcRecordEmitDelay != null && cdcRecordEmitDelay.isArray()) {
                    Map<Integer, List<Long>> dataMap = new HashMap<>();
                    for (JsonNode node : cdcRecordEmitDelay) {
                        Integer pipelineId = node.get("tags").get("pipelineId").asInt();
                        long value = node.get("value").asLong();
                        dataMap.computeIfAbsent(pipelineId, n -> new ArrayList<>()).add(value);
                    }
                    dataMap.forEach(
                            (key, value) -> {
                                JobMetrics currPipelineMetrics =
                                        getOrCreatePipelineMetricsMapStatusRunning(metricsMap, key);
                                OptionalDouble average =
                                        value.stream().mapToDouble(a -> a).average();
                                currPipelineMetrics.setRecordDelay(
                                        Double.valueOf(
                                                        average.isPresent()
                                                                ? average.getAsDouble()
                                                                : 0)
                                                .longValue());
                            });
                }

                log.info("jobEngineId={},metricsMap={}", jobEngineId, metricsMap);

                allRunningJobMetricsHashMap.put(jobEngineId, metricsMap);
            }

        } catch (Exception e) {
            log.error("Failed to fetch running job metrics", e);
        }
        return allRunningJobMetricsHashMap;
    }

    private JobMetrics getOrCreatePipelineMetricsMapStatusRunning(
            LinkedHashMap<Integer, JobMetrics> metricsMap, Integer pipelineId) {
        JobMetrics currPipelineMetrics = metricsMap.get(pipelineId);
        if (currPipelineMetrics == null) {
            currPipelineMetrics = new JobMetrics();
            currPipelineMetrics.setStatus(JobStatus.RUNNING);
            currPipelineMetrics.setPipelineId(pipelineId);
            metricsMap.put(pipelineId, currPipelineMetrics);
        }
        return currPipelineMetrics;
    }

    private JobMetrics getOrCreatePipelineMetricsMap(
            LinkedHashMap<Integer, JobMetrics> metricsMap,
            LinkedHashMap<Integer, String> jobPipelineStatus,
            Integer pipelineId) {
        JobMetrics currPipelineMetrics = metricsMap.get(pipelineId);
        if (currPipelineMetrics == null) {
            currPipelineMetrics = new JobMetrics();
            metricsMap.put(pipelineId, currPipelineMetrics);
            currPipelineMetrics.setStatus(
                    "DEPLOYING".equals(jobPipelineStatus.get(pipelineId))
                            ? JobStatus.SCHEDULED
                            : JobStatus.valueOf(jobPipelineStatus.get(pipelineId)));
            currPipelineMetrics.setPipelineId(pipelineId);
        }
        return currPipelineMetrics;
    }

    private static class SeaTunnelEngineMetricsExtractorHolder {
        private static final SeaTunnelEngineMetricsExtractor INSTANCE =
                new SeaTunnelEngineMetricsExtractor();
    }
}
