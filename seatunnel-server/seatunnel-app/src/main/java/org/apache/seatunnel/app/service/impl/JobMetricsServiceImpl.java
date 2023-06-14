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

import org.apache.seatunnel.app.dal.dao.IJobInstanceDao;
import org.apache.seatunnel.app.dal.dao.IJobInstanceHistoryDao;
import org.apache.seatunnel.app.dal.dao.IJobMetricsDao;
import org.apache.seatunnel.app.dal.entity.JobInstance;
import org.apache.seatunnel.app.dal.entity.JobInstanceHistory;
import org.apache.seatunnel.app.dal.entity.JobMetrics;
import org.apache.seatunnel.app.domain.response.engine.Engine;
import org.apache.seatunnel.app.domain.response.metrics.JobDAG;
import org.apache.seatunnel.app.domain.response.metrics.JobPipelineDetailMetricsRes;
import org.apache.seatunnel.app.domain.response.metrics.JobPipelineSummaryMetricsRes;
import org.apache.seatunnel.app.domain.response.metrics.JobSummaryMetricsRes;
import org.apache.seatunnel.app.permission.constants.SeatunnelFuncPermissionKeyConstant;
import org.apache.seatunnel.app.service.IJobMetricsService;
import org.apache.seatunnel.app.thirdparty.metrics.EngineMetricsExtractorFactory;
import org.apache.seatunnel.app.thirdparty.metrics.IEngineMetricsExtractor;
import org.apache.seatunnel.server.common.CodeGenerateUtils;
import org.apache.seatunnel.server.common.Constants;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;

import javax.annotation.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobMetricsServiceImpl extends SeatunnelBaseServiceImpl implements IJobMetricsService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Resource private IJobMetricsDao jobMetricsDao;

    @Resource private IJobInstanceHistoryDao jobInstanceHistoryDao;

    @Resource private IJobInstanceDao jobInstanceDao;

    @Override
    public List<JobPipelineSummaryMetricsRes> getJobPipelineSummaryMetrics(
            @NonNull Integer userId,
            @NonNull Long projectCode,
            @NonNull Long jobInstanceId,
            @NonNull String jobEngineId) {
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_METRICS_SUMMARY, userId);
        List<JobMetrics> jobPipelineDetailMetrics =
                getJobPipelineMetrics(userId, projectCode, jobInstanceId, jobEngineId);
        return summaryMetrics(jobPipelineDetailMetrics);
    }

    @Override
    public JobSummaryMetricsRes getJobSummaryMetrics(
            @NonNull Integer userId,
            @NonNull Long projectCode,
            @NonNull Long jobInstanceId,
            @NonNull String jobEngineId) {
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_METRICS_SUMMARY, userId);
        JobInstance jobInstance = jobInstanceDao.getJobInstance(jobInstanceId);
        Engine engine = new Engine(jobInstance.getEngineName(), jobInstance.getEngineVersion());
        IEngineMetricsExtractor engineMetricsExtractor =
                (new EngineMetricsExtractorFactory(engine)).getEngineMetricsExtractor();
        String jobStatus = engineMetricsExtractor.getJobStatus(jobEngineId);

        List<JobMetrics> jobPipelineDetailMetrics =
                getJobPipelineMetrics(userId, projectCode, jobInstanceId, jobEngineId);
        long readCount =
                jobPipelineDetailMetrics.stream().mapToLong(JobMetrics::getReadRowCount).sum();
        long writeCount =
                jobPipelineDetailMetrics.stream().mapToLong(JobMetrics::getWriteRowCount).sum();

        JobSummaryMetricsRes jobSummaryMetricsRes =
                new JobSummaryMetricsRes(
                        jobInstanceId, Long.valueOf(jobEngineId), readCount, writeCount, jobStatus);
        return jobSummaryMetricsRes;
    }

    private List<JobMetrics> getJobPipelineDetailMetrics(
            @NonNull JobInstance jobInstance,
            @NonNull Integer userId,
            @NonNull Long projectCode,
            @NonNull String jobEngineId,
            @NonNull String jobStatus,
            @NonNull IEngineMetricsExtractor engineMetricsExtractor) {

        // If job is not end state, get metrics from engine.
        List<JobMetrics> jobMetrics = new ArrayList<>();
        if (engineMetricsExtractor.isJobEndStatus(jobStatus)) {
            jobMetrics = getJobMetricsFromDb(jobInstance, userId, projectCode, jobEngineId);
            if (CollectionUtils.isEmpty(jobMetrics)) {
                syncMetricsToDb(jobInstance, userId, projectCode, jobEngineId);
                jobMetrics = getJobMetricsFromEngine(jobInstance, jobEngineId);
            }
        } else {
            jobMetrics = getJobMetricsFromEngine(jobInstance, jobEngineId);
        }

        return jobMetrics;
    }

    @Override
    public List<JobPipelineDetailMetricsRes> getJobPipelineDetailMetricsRes(
            @NonNull Integer userId,
            @NonNull Long projectCode,
            @NonNull Long jobInstanceId,
            @NonNull String jobEngineId) {
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_DETAIL, userId);
        List<JobMetrics> jobPipelineDetailMetrics =
                getJobPipelineMetrics(userId, projectCode, jobInstanceId, jobEngineId);
        return jobPipelineDetailMetrics.stream()
                .map(this::wrapperJobMetrics)
                .collect(Collectors.toList());
    }

    private List<JobMetrics> getJobPipelineMetrics(
            @NonNull Integer userId,
            @NonNull Long projectCode,
            @NonNull Long jobInstanceId,
            @NonNull String jobEngineId) {

        JobInstance jobInstance = jobInstanceDao.getJobInstance(jobInstanceId);
        Engine engine = new Engine(jobInstance.getEngineName(), jobInstance.getEngineVersion());
        IEngineMetricsExtractor engineMetricsExtractor =
                (new EngineMetricsExtractorFactory(engine)).getEngineMetricsExtractor();
        String jobStatus = engineMetricsExtractor.getJobStatus(jobEngineId);
        List<JobMetrics> jobPipelineDetailMetrics =
                getJobPipelineDetailMetrics(
                        jobInstance,
                        userId,
                        projectCode,
                        jobEngineId,
                        jobStatus,
                        engineMetricsExtractor);
        return jobPipelineDetailMetrics;
    }

    @Override
    public JobDAG getJobDAG(
            @NonNull Integer userId,
            @NonNull Long projectCode,
            @NonNull Long jobInstanceId,
            @NonNull String jobEngineId)
            throws JsonProcessingException {
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_DAG, userId);
        JobInstance jobInstance = jobInstanceDao.getJobInstance(jobInstanceId);
        JobInstanceHistory history =
                getJobHistoryFromDb(jobInstance, userId, jobEngineId, projectCode);
        if (history != null) {
            String dag = history.getDag();
            try {
                return OBJECT_MAPPER.readValue(dag, JobDAG.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Engine engine = new Engine(jobInstance.getEngineName(), jobInstance.getEngineVersion());
        IEngineMetricsExtractor engineMetricsExtractor =
                (new EngineMetricsExtractorFactory(engine)).getEngineMetricsExtractor();

        if (engineMetricsExtractor.isJobEnd(jobEngineId)) {
            syncJobInfoToDb(jobInstance, projectCode, jobEngineId);
            history = getJobHistoryFromDb(jobInstance, userId, jobEngineId, projectCode);
        } else {
            history = getJobHistoryFromEngine(jobInstance, jobEngineId);
        }
        if (history != null) {
            String dag = history.getDag();
            try {
                return OBJECT_MAPPER.readValue(dag, JobDAG.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private JobInstanceHistory getJobHistoryFromEngine(
            @NonNull JobInstance jobInstance, String jobEngineId) {

        Engine engine = new Engine(jobInstance.getEngineName(), jobInstance.getEngineVersion());

        IEngineMetricsExtractor engineMetricsExtractor =
                (new EngineMetricsExtractorFactory(engine)).getEngineMetricsExtractor();

        return engineMetricsExtractor.getJobHistoryById(jobEngineId);
    }

    private JobInstanceHistory getJobHistoryFromDb(
            @NonNull JobInstance jobInstance,
            Integer userId,
            String jobEngineId,
            Long projectCode) {
        // relation jobInstanceId and jobEngineId
        relationJobInstanceAndJobEngineId(jobInstance, userId, jobEngineId);
        return jobInstanceHistoryDao.getByInstanceId(jobInstance.getId(), projectCode);
    }

    @Override
    public void syncJobDataToDb(
            @NonNull JobInstance jobInstance,
            @NonNull Integer userId,
            @NonNull Long projectCode,
            @NonNull String jobEngineId) {
        relationJobInstanceAndJobEngineId(jobInstance, userId, jobEngineId);
        syncMetricsToDb(jobInstance, userId, projectCode, jobEngineId);
        syncJobInfoToDb(jobInstance, projectCode, jobEngineId);
    }

    private void syncMetricsToDb(
            @NonNull JobInstance jobInstance,
            @NonNull Integer userId,
            @NonNull Long projectCode,
            @NonNull String jobEngineId) {
        List<JobMetrics> jobMetricsFromEngine = getJobMetricsFromEngine(jobInstance, jobEngineId);

        jobMetricsFromEngine.forEach(
                metrics -> {
                    try {
                        metrics.setId(CodeGenerateUtils.getInstance().genCode());
                    } catch (CodeGenerateUtils.CodeGenerateException e) {
                        throw new SeatunnelException(
                                SeatunnelErrorEnum.JOB_RUN_GENERATE_UUID_ERROR);
                    }
                    metrics.setJobInstanceId(jobInstance.getId());
                    metrics.setCreateUserId(userId);
                    metrics.setUpdateUserId(userId);
                });

        if (!jobMetricsFromEngine.isEmpty()) {
            jobMetricsDao.getJobMetricsMapper().insertBatchMetrics(jobMetricsFromEngine);
        }
    }

    private void syncJobInfoToDb(
            @NonNull JobInstance jobInstance,
            @NonNull Long projectCode,
            @NonNull String jobEngineId) {
        JobInstanceHistory jobHistoryFromEngine = getJobHistoryFromEngine(jobInstance, jobEngineId);

        jobHistoryFromEngine.setId(jobInstance.getId());

        jobInstanceHistoryDao.insert(jobHistoryFromEngine);
    }

    private void relationJobInstanceAndJobEngineId(
            @NonNull JobInstance jobInstance,
            @NonNull Integer userId,
            @NonNull String jobEngineId) {
        // relation jobInstanceId and jobEngineId
        if (StringUtils.isEmpty(jobInstance.getJobEngineId())) {
            jobInstance.setJobEngineId(jobEngineId);
            jobInstance.setUpdateUserId(userId);
            jobInstanceDao.update(jobInstance);
        }
    }

    private List<JobMetrics> getJobMetricsFromEngine(
            @NonNull JobInstance jobInstance, @NonNull String jobEngineId) {
        Engine engine = new Engine(jobInstance.getEngineName(), jobInstance.getEngineVersion());

        IEngineMetricsExtractor engineMetricsExtractor =
                (new EngineMetricsExtractorFactory(engine)).getEngineMetricsExtractor();

        return engineMetricsExtractor.getMetricsByJobEngineId(jobEngineId);
    }

    private List<JobPipelineSummaryMetricsRes> summaryMetrics(
            @NonNull List<JobMetrics> jobPipelineDetailedMetrics) {
        return jobPipelineDetailedMetrics.stream()
                .map(
                        metrics ->
                                new JobPipelineSummaryMetricsRes(
                                        metrics.getPipelineId(),
                                        metrics.getReadRowCount(),
                                        metrics.getWriteRowCount(),
                                        metrics.getStatus()))
                .collect(Collectors.toList());
    }

    private List<JobMetrics> getJobMetricsFromDb(
            @NonNull JobInstance jobInstance,
            @NonNull Integer userId,
            @NonNull Long projectCode,
            @NonNull String jobEngineId) {

        // relation jobInstanceId and jobEngineId
        relationJobInstanceAndJobEngineId(jobInstance, userId, jobEngineId);

        // get metrics from db
        return jobMetricsDao.getByInstanceId(projectCode, jobInstance.getId());
    }

    @Override
    public ImmutablePair<Long, String> getInstanceIdAndEngineId(@NonNull String key) {
        if (!key.contains(Constants.METRICS_QUERY_KEY_SPLIT)
                || key.split(Constants.METRICS_QUERY_KEY_SPLIT).length != 2) {
            throw new SeatunnelException(SeatunnelErrorEnum.JOB_METRICS_QUERY_KEY_ERROR, key);
        }

        String[] split = key.split(Constants.METRICS_QUERY_KEY_SPLIT);
        Long jobInstanceId = Long.valueOf(split[0]);
        String jobEngineId = split[1];
        return new ImmutablePair<>(jobInstanceId, jobEngineId);
    }

    private JobPipelineDetailMetricsRes wrapperJobMetrics(@NonNull JobMetrics metrics) {
        return new JobPipelineDetailMetricsRes(
                metrics.getId(),
                metrics.getPipelineId(),
                metrics.getReadRowCount(),
                metrics.getWriteRowCount(),
                metrics.getSourceTableNames(),
                metrics.getSinkTableNames(),
                metrics.getReadQps(),
                metrics.getWriteQps(),
                metrics.getRecordDelay(),
                metrics.getStatus());
    }
}
