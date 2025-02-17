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

import org.apache.seatunnel.app.common.EngineType;
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
import org.apache.seatunnel.app.thirdparty.engine.SeaTunnelEngineProxy;
import org.apache.seatunnel.app.thirdparty.metrics.EngineMetricsExtractorFactory;
import org.apache.seatunnel.app.thirdparty.metrics.IEngineMetricsExtractor;
import org.apache.seatunnel.app.utils.JobUtils;
import org.apache.seatunnel.app.utils.ServletUtils;
import org.apache.seatunnel.common.constants.JobMode;
import org.apache.seatunnel.common.utils.JsonUtils;
import org.apache.seatunnel.engine.core.job.JobStatus;
import org.apache.seatunnel.server.common.CodeGenerateUtils;
import org.apache.seatunnel.server.common.Constants;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JobMetricsServiceImpl extends SeatunnelBaseServiceImpl implements IJobMetricsService {
    @Resource private IJobMetricsDao jobMetricsDao;

    @Resource private IJobInstanceHistoryDao jobInstanceHistoryDao;

    @Resource private IJobInstanceDao jobInstanceDao;

    @Override
    public List<JobPipelineSummaryMetricsRes> getJobPipelineSummaryMetrics(
            @NonNull Long jobInstanceId) {
        int userId = ServletUtils.getCurrentUserId();
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_METRICS_SUMMARY, userId);
        JobInstance jobInstance = jobInstanceDao.getJobInstance(jobInstanceId);
        List<JobMetrics> jobPipelineDetailMetrics = getJobPipelineDetailMetrics(jobInstance);
        return summaryMetrics(jobPipelineDetailMetrics);
    }

    @Override
    public JobSummaryMetricsRes getJobSummaryMetrics(
            @NonNull Long jobInstanceId, @NonNull String jobEngineId) {
        int userId = ServletUtils.getCurrentUserId();
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_METRICS_SUMMARY, userId);
        JobInstance jobInstance = jobInstanceDao.getJobInstance(jobInstanceId);
        Engine engine = new Engine(jobInstance.getEngineName(), jobInstance.getEngineVersion());
        IEngineMetricsExtractor engineMetricsExtractor =
                (new EngineMetricsExtractorFactory(engine)).getEngineMetricsExtractor();
        JobStatus jobStatus = engineMetricsExtractor.getJobStatus(jobEngineId);

        List<JobMetrics> jobPipelineDetailMetrics = getJobPipelineDetailMetrics(jobInstance);
        long readCount =
                jobPipelineDetailMetrics.stream().mapToLong(JobMetrics::getReadRowCount).sum();
        long writeCount =
                jobPipelineDetailMetrics.stream().mapToLong(JobMetrics::getWriteRowCount).sum();

        return new JobSummaryMetricsRes(
                jobInstanceId, Long.parseLong(jobEngineId), readCount, writeCount, jobStatus);
    }

    @Override
    public Map<Long, JobSummaryMetricsRes> getALLJobSummaryMetrics(
            @NonNull Map<Long, Long> jobInstanceIdAndJobEngineIdMap,
            @NonNull List<Long> jobInstanceIdList,
            @NonNull JobMode jobMode) {
        log.info("jobInstanceIdAndJobEngineIdMap={}", jobInstanceIdAndJobEngineIdMap);
        int userId = ServletUtils.getCurrentUserId();
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_METRICS_SUMMARY, userId);
        List<JobInstance> allJobInstance = jobInstanceDao.getAllJobInstance(jobInstanceIdList);
        if (allJobInstance.isEmpty()) {
            log.warn(
                    "getALLJobSummaryMetrics : allJobInstance is empty, task id list is {}",
                    jobInstanceIdList);
            return new HashMap<>();
        }
        Map<Long, JobSummaryMetricsRes> result = null;
        Map<Long, HashMap<Integer, JobMetrics>> allRunningJobMetricsFromEngine =
                getAllRunningJobMetricsFromEngine(
                        allJobInstance.get(0).getEngineName(),
                        allJobInstance.get(0).getEngineVersion());

        if (JobMode.BATCH == jobMode) {
            result =
                    getMatricsListIfTaskTypeIsBatch(
                            allJobInstance,
                            allRunningJobMetricsFromEngine,
                            jobInstanceIdAndJobEngineIdMap);
        } else if (JobMode.STREAMING == jobMode) {
            result =
                    getMatricsListIfTaskTypeIsStreaming(
                            allJobInstance,
                            allRunningJobMetricsFromEngine,
                            jobInstanceIdAndJobEngineIdMap);
        }

        log.info("result is {}", result == null ? "null" : result.toString());
        return result;
    }

    private Map<Long, JobSummaryMetricsRes> getMatricsListIfTaskTypeIsBatch(
            List<JobInstance> allJobInstance,
            Map<Long, HashMap<Integer, JobMetrics>> allRunningJobMetricsFromEngine,
            Map<Long, Long> jobInstanceIdAndJobEngineIdMap) {

        HashMap<Long, JobSummaryMetricsRes> jobSummaryMetricsResMap = new HashMap<>();

        log.info("allRunningJobMetricsFromEngine is {}", allRunningJobMetricsFromEngine.toString());

        // Traverse all jobInstances in allJobInstance
        for (JobInstance jobInstance : allJobInstance) {
            log.info("jobEngineId={}", jobInstance.getJobEngineId());

            if (jobInstance.getJobStatus() == null
                    || jobInstance.getJobStatus() == JobStatus.FAILED
                    || jobInstance.getJobStatus() == JobStatus.RUNNING) {
                // Obtain monitoring information from the collection of running jobs returned from
                // the engine
                if (!allRunningJobMetricsFromEngine.isEmpty()
                        && allRunningJobMetricsFromEngine.containsKey(
                                jobInstanceIdAndJobEngineIdMap.get(jobInstance.getId()))) {
                    JobSummaryMetricsRes jobMetricsFromEngineRes =
                            getRunningJobMetricsFromEngine(
                                    allRunningJobMetricsFromEngine,
                                    jobInstanceIdAndJobEngineIdMap,
                                    jobInstance);
                    jobSummaryMetricsResMap.put(jobInstance.getId(), jobMetricsFromEngineRes);
                    modifyAndUpdateJobInstanceAndJobMetrics(
                            jobInstance,
                            allRunningJobMetricsFromEngine,
                            jobInstanceIdAndJobEngineIdMap);

                } else {
                    log.info(
                            "The job does not exist on the engine, it is directly returned from the database");
                    JobSummaryMetricsRes jobMetricsFromDb =
                            getJobSummaryMetricsResByDb(
                                    jobInstance,
                                    Long.toString(
                                            jobInstanceIdAndJobEngineIdMap.get(
                                                    jobInstance.getId())));
                    if (jobMetricsFromDb != null) {
                        jobSummaryMetricsResMap.put(jobInstance.getId(), jobMetricsFromDb);
                    }
                    if (jobInstance.getJobStatus() == JobStatus.RUNNING) {
                        // Set the job status of jobInstance and jobMetrics in the database to
                        // finished
                        jobInstance.setJobStatus(JobStatus.FINISHED);
                        jobInstanceDao.getJobInstanceMapper().updateById(jobInstance);
                    }
                }
            } else if (jobInstance.getJobStatus() == JobStatus.FINISHED
                    || jobInstance.getJobStatus() == JobStatus.CANCELED) {
                // If the status of the job is finished or cancelled, the monitoring information is
                // directly obtained from MySQL
                JobSummaryMetricsRes jobMetricsFromDb =
                        getJobSummaryMetricsResByDb(
                                jobInstance,
                                Long.toString(
                                        jobInstanceIdAndJobEngineIdMap.get(jobInstance.getId())));
                log.info("jobStatus=finish oe canceled,JobSummaryMetricsRes={}", jobMetricsFromDb);
                jobSummaryMetricsResMap.put(jobInstance.getId(), jobMetricsFromDb);
            }
        }

        return jobSummaryMetricsResMap;
    }

    private void modifyAndUpdateJobInstanceAndJobMetrics(
            JobInstance jobInstance,
            Map<Long, HashMap<Integer, JobMetrics>> allRunningJobMetricsFromEngine,
            Map<Long, Long> jobInstanceIdAndJobEngineIdMap) {
        jobInstance.setJobStatus(JobStatus.RUNNING);
        HashMap<Integer, JobMetrics> jobMetricsFromEngine =
                allRunningJobMetricsFromEngine.get(
                        jobInstanceIdAndJobEngineIdMap.get(jobInstance.getId()));
        List<JobMetrics> jobMetricsFromDb = jobMetricsDao.getByInstanceId(jobInstance.getId());
        log.info("001jobMetricsFromDb={}", jobMetricsFromDb);

        if (jobMetricsFromDb.isEmpty()) {
            log.info("002jobMetricsFromDb == null");
            syncMetricsToDbRunning(jobInstance, jobMetricsFromEngine);
            jobInstanceDao.update(jobInstance);
        } else {
            jobMetricsFromDb.forEach(
                    jobMetrics ->
                            jobMetrics.setReadRowCount(
                                    jobMetricsFromEngine
                                            .get(jobMetrics.getPipelineId())
                                            .getReadRowCount()));
            jobMetricsFromDb.forEach(
                    jobMetrics ->
                            jobMetrics.setWriteRowCount(
                                    jobMetricsFromEngine
                                            .get(jobMetrics.getPipelineId())
                                            .getWriteRowCount()));
            jobMetricsFromDb.forEach(jobMetrics -> jobMetrics.setStatus(JobStatus.RUNNING));

            updateJobInstanceAndMetrics(jobInstance, jobMetricsFromDb);
        }
    }

    private Map<Long, JobSummaryMetricsRes> getMatricsListIfTaskTypeIsStreaming(
            List<JobInstance> allJobInstance,
            Map<Long, HashMap<Integer, JobMetrics>> allRunningJobMetricsFromEngine,
            Map<Long, Long> jobInstanceIdAndJobEngineIdMap) {

        HashMap<Long, JobSummaryMetricsRes> jobSummaryMetricsResMap = new HashMap<>();

        // Traverse all jobInstances in allJobInstance
        for (JobInstance jobInstance : allJobInstance) {

            try {
                if (jobInstance.getJobStatus() != null
                        && jobInstance.getJobStatus() == JobStatus.CANCELED) {
                    // If the status of the job is finished or cancelled
                    // the monitoring information is directly obtained from MySQL
                    JobSummaryMetricsRes jobMetricsFromDb =
                            getJobSummaryMetricsResByDb(
                                    jobInstance,
                                    Long.toString(
                                            jobInstanceIdAndJobEngineIdMap.get(
                                                    jobInstance.getId())));
                    jobSummaryMetricsResMap.put(jobInstance.getId(), jobMetricsFromDb);

                } else if (jobInstance.getJobStatus() != null
                        && (jobInstance.getJobStatus() == JobStatus.FINISHED
                                || jobInstance.getJobStatus() == JobStatus.FAILED)) {
                    // Obtain monitoring information from the collection of running jobs returned
                    // from
                    // the engine
                    if (!allRunningJobMetricsFromEngine.isEmpty()
                            && allRunningJobMetricsFromEngine.containsKey(
                                    jobInstanceIdAndJobEngineIdMap.get(jobInstance.getId()))) {
                        // If it can be found, update the information in MySQL and return it to the
                        // front-end data
                        modifyAndUpdateJobInstanceAndJobMetrics(
                                jobInstance,
                                allRunningJobMetricsFromEngine,
                                jobInstanceIdAndJobEngineIdMap);

                        // Return data from the front-end
                        JobSummaryMetricsRes jobMetricsFromEngineRes =
                                getRunningJobMetricsFromEngine(
                                        allRunningJobMetricsFromEngine,
                                        jobInstanceIdAndJobEngineIdMap,
                                        jobInstance);
                        jobSummaryMetricsResMap.put(jobInstance.getId(), jobMetricsFromEngineRes);
                    } else {
                        // If not found, obtain information from MySQL
                        JobSummaryMetricsRes jobMetricsFromDb =
                                getJobSummaryMetricsResByDb(
                                        jobInstance,
                                        Long.toString(
                                                jobInstanceIdAndJobEngineIdMap.get(
                                                        jobInstance.getId())));
                        jobSummaryMetricsResMap.put(jobInstance.getId(), jobMetricsFromDb);
                    }
                } else {
                    // Obtain monitoring information from the collection of running jobs returned
                    // from
                    // the engine
                    if (!allRunningJobMetricsFromEngine.isEmpty()
                            && allRunningJobMetricsFromEngine.containsKey(
                                    jobInstanceIdAndJobEngineIdMap.get(jobInstance.getId()))) {
                        modifyAndUpdateJobInstanceAndJobMetrics(
                                jobInstance,
                                allRunningJobMetricsFromEngine,
                                jobInstanceIdAndJobEngineIdMap);
                        // Return data from the front-end
                        JobSummaryMetricsRes jobMetricsFromEngineRes =
                                getRunningJobMetricsFromEngine(
                                        allRunningJobMetricsFromEngine,
                                        jobInstanceIdAndJobEngineIdMap,
                                        jobInstance);
                        jobSummaryMetricsResMap.put(jobInstance.getId(), jobMetricsFromEngineRes);
                    } else {
                        JobStatus jobStatus = null;
                        try {
                            jobStatus =
                                    getJobStatusByJobEngineId(
                                            String.valueOf(
                                                    jobInstanceIdAndJobEngineIdMap.get(
                                                            jobInstance.getId())));
                        } catch (Exception e) {
                            log.warn(
                                    "getMetricsListIfTaskTypeIsStreaming getJobStatusByJobEngineId is exception jobInstanceId is : {}",
                                    jobInstance.getId());
                        }

                        if (jobStatus != null) {
                            jobInstance.setJobStatus(jobStatus);
                            jobInstanceDao.update(jobInstance);
                            JobSummaryMetricsRes jobSummaryMetricsResByDb =
                                    getJobSummaryMetricsResByDb(
                                            jobInstance,
                                            String.valueOf(
                                                    jobInstanceIdAndJobEngineIdMap.get(
                                                            jobInstance.getId())));
                            jobSummaryMetricsResMap.put(
                                    jobInstance.getId(), jobSummaryMetricsResByDb);
                            List<JobMetrics> jobMetricsFromDb =
                                    getJobMetricsFromDb(
                                            jobInstance,
                                            String.valueOf(
                                                    jobInstanceIdAndJobEngineIdMap.get(
                                                            jobInstance.getId())));
                            if (!jobMetricsFromDb.isEmpty()) {
                                JobStatus finalJobStatusByJobEngineId = jobStatus;
                                jobMetricsFromDb.forEach(
                                        jobMetrics ->
                                                jobMetrics.setStatus(finalJobStatusByJobEngineId));
                                for (JobMetrics jobMetrics : jobMetricsFromDb) {
                                    jobMetricsDao.getJobMetricsMapper().updateById(jobMetrics);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return jobSummaryMetricsResMap;
    }

    private JobSummaryMetricsRes getRunningJobMetricsFromEngine(
            Map<Long, HashMap<Integer, JobMetrics>> allRunningJobMetricsFromEngine,
            Map<Long, Long> jobInstanceIdAndJobEngineIdMap,
            JobInstance jobInstance) {

        // If there is job information in the engine
        HashMap<Integer, JobMetrics> jobMetricsFromEngine =
                allRunningJobMetricsFromEngine.get(
                        jobInstanceIdAndJobEngineIdMap.get(jobInstance.getId()));
        log.info("0706jobMetricsFromEngine={}", jobMetricsFromEngine);
        long readCount =
                jobMetricsFromEngine.values().stream().mapToLong(JobMetrics::getReadRowCount).sum();
        long writeCount =
                jobMetricsFromEngine.values().stream()
                        .mapToLong(JobMetrics::getWriteRowCount)
                        .sum();

        log.info("jobInstance={}", jobInstance);

        return new JobSummaryMetricsRes(
                jobInstance.getId(), 1L, readCount, writeCount, JobStatus.RUNNING);
    }

    private JobSummaryMetricsRes getJobSummaryMetricsResByDb(
            JobInstance jobInstance, String jobEngineId) {
        List<JobMetrics> jobMetricsFromDb = getJobMetricsFromDb(jobInstance, jobEngineId);
        if (!jobMetricsFromDb.isEmpty()) {
            long readCount = jobMetricsFromDb.stream().mapToLong(JobMetrics::getReadRowCount).sum();
            long writeCount =
                    jobMetricsFromDb.stream().mapToLong(JobMetrics::getWriteRowCount).sum();
            return new JobSummaryMetricsRes(
                    jobInstance.getId(),
                    Long.parseLong(jobInstance.getJobEngineId()),
                    readCount,
                    writeCount,
                    jobInstance.getJobStatus());
        }
        return null;
    }

    private Map<Long, HashMap<Integer, JobMetrics>> getAllRunningJobMetricsFromEngine(
            EngineType engineName, String engineVersion) {
        Engine engine = new Engine(engineName, engineVersion);

        IEngineMetricsExtractor engineMetricsExtractor =
                (new EngineMetricsExtractorFactory(engine)).getEngineMetricsExtractor();

        return engineMetricsExtractor.getAllRunningJobMetrics();
    }

    private void updateJobInstanceAndMetrics(JobInstance jobInstance, List<JobMetrics> jobMetrics) {
        if (jobInstance != null && jobMetrics != null) {
            jobInstanceDao.update(jobInstance);
            // jobMetricsFromDb
            for (JobMetrics jobMetric : jobMetrics) {
                jobMetricsDao.getJobMetricsMapper().updateById(jobMetric);
            }
        }
    }

    private JobStatus getJobStatusByJobEngineId(String jobEngineId) {
        return SeaTunnelEngineProxy.getInstance().getJobStatus(jobEngineId);
    }

    private Map<Integer, JobMetrics> getJobMetricsFromEngineMap(
            @NonNull JobInstance jobInstance, @NonNull String jobEngineId) {

        log.info("enter getJobMetricsFromEngine");
        Engine engine = new Engine(jobInstance.getEngineName(), jobInstance.getEngineVersion());

        IEngineMetricsExtractor engineMetricsExtractor =
                (new EngineMetricsExtractorFactory(engine)).getEngineMetricsExtractor();

        return engineMetricsExtractor.getMetricsByJobEngineIdRTMap(jobEngineId);
    }

    private List<JobMetrics> getJobPipelineDetailMetrics(@NonNull JobInstance jobInstance) {
        List<JobMetrics> jobMetrics;
        if (JobUtils.isJobEndStatus(jobInstance.getJobStatus())) {
            jobMetrics = getJobMetricsFromDb(jobInstance, jobInstance.getJobEngineId());
            if (CollectionUtils.isEmpty(jobMetrics)) {
                jobMetrics = getJobMetricsFromEngine(jobInstance, jobInstance.getJobEngineId());
                if (!jobMetrics.isEmpty()) {
                    // If engine returns some metrics then it makes sens to insert into database
                    syncMetricsToDb(jobInstance, jobInstance.getJobEngineId());
                }
            }
        } else {
            // If job is not end state, get metrics from engine.
            jobMetrics = getJobMetricsFromEngine(jobInstance, jobInstance.getJobEngineId());
        }
        return jobMetrics;
    }

    @Override
    public List<JobPipelineDetailMetricsRes> getJobPipelineDetailMetricsRes(
            @NonNull Long jobInstanceId) {
        int userId = ServletUtils.getCurrentUserId();
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_DETAIL, userId);
        JobInstance jobInstance = jobInstanceDao.getJobInstance(jobInstanceId);
        List<JobMetrics> jobPipelineDetailMetrics = getJobPipelineDetailMetrics(jobInstance);
        return jobPipelineDetailMetrics.stream()
                .map(this::wrapperJobMetrics)
                .collect(Collectors.toList());
    }

    @Override
    public JobDAG getJobDAG(@NonNull Long jobInstanceId) {
        int userId = ServletUtils.getCurrentUserId();
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_DAG, userId);
        JobInstance jobInstance = jobInstanceDao.getJobInstance(jobInstanceId);
        String jobEngineId = jobInstance.getJobEngineId();
        JobInstanceHistory history = getJobHistoryFromDb(jobInstance, jobEngineId);
        if (history != null) {
            String dag = history.getDag();
            return JsonUtils.parseObject(dag, JobDAG.class);
        }
        Engine engine = new Engine(jobInstance.getEngineName(), jobInstance.getEngineVersion());
        IEngineMetricsExtractor engineMetricsExtractor =
                (new EngineMetricsExtractorFactory(engine)).getEngineMetricsExtractor();

        if (engineMetricsExtractor.isJobEnd(jobEngineId)) {
            syncHistoryJobInfoToDb(jobInstance, jobEngineId);
            history = getJobHistoryFromDb(jobInstance, jobEngineId);
        } else {
            history = getJobHistoryFromEngine(jobInstance, jobEngineId);
        }
        if (history != null) {
            String dag = history.getDag();
            return JsonUtils.parseObject(dag, JobDAG.class);
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
            @NonNull JobInstance jobInstance, String jobEngineId) {
        // relation jobInstanceId and jobEngineId
        relationJobInstanceAndJobEngineId(jobInstance, jobEngineId);
        return jobInstanceHistoryDao.getByInstanceId(jobInstance.getId());
    }

    @Override
    public void syncJobDataToDb(@NonNull JobInstance jobInstance, @NonNull String jobEngineId) {
        relationJobInstanceAndJobEngineId(jobInstance, jobEngineId);
        syncMetricsToDb(jobInstance, jobEngineId);
        syncHistoryJobInfoToDb(jobInstance, jobEngineId);
        syncCompleteJobInfoToDb(jobInstance);
    }

    private void syncMetricsToDb(@NonNull JobInstance jobInstance, @NonNull String jobEngineId) {
        Map<Integer, JobMetrics> jobMetricsFromEngineMap =
                getJobMetricsFromEngineMap(jobInstance, jobEngineId);
        int userId = ServletUtils.getCurrentUserId();
        List<JobMetrics> jobMetricsFromDb = getJobMetricsFromDb(jobInstance, jobEngineId);
        if (jobMetricsFromDb.isEmpty()) {
            List<JobMetrics> jobMetricsFromEngine =
                    Arrays.asList(jobMetricsFromEngineMap.values().toArray(new JobMetrics[0]));
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
        } else {
            JobStatus jobStatus = getJobStatusByJobEngineId(jobEngineId);
            for (JobMetrics jobMetrics : jobMetricsFromDb) {
                Integer pipelineId = jobMetrics.getPipelineId();
                JobMetrics currentPiplinejobMetricsFromEngine =
                        jobMetricsFromEngineMap.get(pipelineId);
                jobMetrics.setWriteQps(currentPiplinejobMetricsFromEngine.getWriteQps());
                jobMetrics.setReadQps(currentPiplinejobMetricsFromEngine.getReadQps());
                jobMetrics.setReadRowCount(currentPiplinejobMetricsFromEngine.getReadRowCount());
                jobMetrics.setWriteRowCount(currentPiplinejobMetricsFromEngine.getWriteRowCount());
                jobMetrics.setStatus(jobStatus);
                jobMetricsDao.getJobMetricsMapper().updateById(jobMetrics);
            }
        }
    }

    private void syncHistoryJobInfoToDb(
            @NonNull JobInstance jobInstance, @NonNull String jobEngineId) {
        JobInstanceHistory jobHistoryFromEngine = getJobHistoryFromEngine(jobInstance, jobEngineId);

        jobHistoryFromEngine.setId(jobInstance.getId());

        JobInstanceHistory byInstanceId =
                jobInstanceHistoryDao.getByInstanceId(jobInstance.getId());
        if (byInstanceId == null) {
            try {
                jobInstanceHistoryDao.insert(jobHistoryFromEngine);
            } catch (DuplicateKeyException e) {
                // Handle the race condition gracefully
                jobInstanceHistoryDao.updateJobInstanceHistory(jobHistoryFromEngine);
            }
        } else {
            jobInstanceHistoryDao.updateJobInstanceHistory(jobHistoryFromEngine);
        }
    }

    private void syncCompleteJobInfoToDb(@NonNull JobInstance jobInstance) {
        jobInstance.setEndTime(new Date());
        jobInstanceDao.update(jobInstance);
    }

    private void relationJobInstanceAndJobEngineId(
            @NonNull JobInstance jobInstance, @NonNull String jobEngineId) {
        // relation jobInstanceId and jobEngineId
        if (StringUtils.isEmpty(jobInstance.getJobEngineId())) {
            int userId = ServletUtils.getCurrentUserId();
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
            @NonNull JobInstance jobInstance, @NonNull String jobEngineId) {

        // relation jobInstanceId and jobEngineId
        relationJobInstanceAndJobEngineId(jobInstance, jobEngineId);

        // get metrics from db
        return jobMetricsDao.getByInstanceId(jobInstance.getId());
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

    private void syncMetricsToDbRunning(
            @NonNull JobInstance jobInstance, @NonNull Map<Integer, JobMetrics> jobMetricsMap) {
        int userId = ServletUtils.getCurrentUserId();
        ArrayList<JobMetrics> list = new ArrayList<>();
        for (Map.Entry<Integer, JobMetrics> entry : jobMetricsMap.entrySet()) {
            JobMetrics jobMetrics = entry.getValue();
            jobMetrics.setId(CodeGenerateUtils.getInstance().genCode());
            jobMetrics.setJobInstanceId(jobInstance.getId());
            jobMetrics.setCreateUserId(userId);
            jobMetrics.setUpdateUserId(userId);
            list.add(jobMetrics);
        }
        if (!list.isEmpty()) {
            log.info("003list={}", list);
            jobMetricsDao.getJobMetricsMapper().insertBatchMetrics(list);
        }
    }
}
