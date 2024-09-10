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

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.dal.dao.IJobInstanceDao;
import org.apache.seatunnel.app.dal.entity.JobInstance;
import org.apache.seatunnel.app.domain.request.job.JobExecParam;
import org.apache.seatunnel.app.domain.response.engine.Engine;
import org.apache.seatunnel.app.domain.response.executor.JobExecutorRes;
import org.apache.seatunnel.app.service.IJobExecutorService;
import org.apache.seatunnel.app.service.IJobInstanceService;
import org.apache.seatunnel.app.thirdparty.engine.SeaTunnelEngineProxy;
import org.apache.seatunnel.app.thirdparty.metrics.EngineMetricsExtractorFactory;
import org.apache.seatunnel.app.thirdparty.metrics.IEngineMetricsExtractor;
import org.apache.seatunnel.app.utils.JobUtils;
import org.apache.seatunnel.common.config.Common;
import org.apache.seatunnel.common.config.DeployMode;
import org.apache.seatunnel.engine.client.SeaTunnelClient;
import org.apache.seatunnel.engine.client.job.ClientJobExecutionEnvironment;
import org.apache.seatunnel.engine.client.job.ClientJobProxy;
import org.apache.seatunnel.engine.common.config.ConfigProvider;
import org.apache.seatunnel.engine.common.config.JobConfig;
import org.apache.seatunnel.engine.common.config.SeaTunnelConfig;
import org.apache.seatunnel.engine.common.config.YamlSeaTunnelConfigBuilder;
import org.apache.seatunnel.engine.core.job.JobResult;
import org.apache.seatunnel.engine.core.job.JobStatus;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;

import org.springframework.stereotype.Service;

import com.hazelcast.client.config.ClientConfig;
import jakarta.annotation.Resource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class JobExecutorServiceImpl implements IJobExecutorService {
    @Resource private IJobInstanceService jobInstanceService;
    @Resource private IJobInstanceDao jobInstanceDao;

    @Override
    public Result<Long> jobExecute(Integer userId, Long jobDefineId, JobExecParam executeParam) {

        JobExecutorRes executeResource =
                jobInstanceService.createExecuteResource(userId, jobDefineId, executeParam);
        String jobConfig = executeResource.getJobConfig();

        String configFile = writeJobConfigIntoConfFile(jobConfig, jobDefineId);

        try {
            executeJobBySeaTunnel(userId, configFile, executeResource.getJobInstanceId());
            return Result.success(executeResource.getJobInstanceId());
        } catch (RuntimeException e) {
            Result<Long> failure =
                    Result.failure(SeatunnelErrorEnum.JOB_EXEC_SUBMISSION_ERROR, e.getMessage());
            // Even though job execution submission failed, we still need to return the
            // jobInstanceId to the user
            // as the job instance has been created in the database.
            failure.setData(executeResource.getJobInstanceId());
            return failure;
        }
    }

    public String writeJobConfigIntoConfFile(String jobConfig, Long jobDefineId) {
        String projectRoot = System.getProperty("user.dir");
        String filePath =
                projectRoot + File.separator + "profile" + File.separator + jobDefineId + ".conf";
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }

            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(jobConfig);
            bufferedWriter.close();

            log.info("File created and content written successfully.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return filePath;
    }

    private void executeJobBySeaTunnel(Integer userId, String filePath, Long jobInstanceId) {
        Common.setDeployMode(DeployMode.CLIENT);
        JobConfig jobConfig = new JobConfig();
        jobConfig.setName(jobInstanceId + "_job");
        SeaTunnelClient seaTunnelClient;
        ClientJobProxy clientJobProxy;
        try {
            seaTunnelClient = createSeaTunnelClient();
            SeaTunnelConfig seaTunnelConfig = new YamlSeaTunnelConfigBuilder().build();
            ClientJobExecutionEnvironment jobExecutionEnv =
                    seaTunnelClient.createExecutionContext(filePath, jobConfig, seaTunnelConfig);
            clientJobProxy = jobExecutionEnv.execute();
        } catch (Throwable e) {
            log.error("Job execution submission failed.", e);
            JobInstance jobInstance = jobInstanceDao.getJobInstance(jobInstanceId);
            jobInstance.setJobStatus(JobStatus.FAILED);
            jobInstance.setEndTime(new Date());
            String jobInstanceErrorMessage = JobUtils.getJobInstanceErrorMessage(e.getMessage());
            jobInstance.setErrorMessage(jobInstanceErrorMessage);
            jobInstanceDao.update(jobInstance);
            throw new RuntimeException(e.getMessage(), e);
        }
        JobInstance jobInstance = jobInstanceDao.getJobInstance(jobInstanceId);
        jobInstance.setJobEngineId(Long.toString(clientJobProxy.getJobId()));
        jobInstanceDao.update(jobInstance);
        CompletableFuture.runAsync(
                () -> {
                    waitJobFinish(
                            clientJobProxy,
                            userId,
                            jobInstanceId,
                            Long.toString(clientJobProxy.getJobId()),
                            seaTunnelClient);
                });
    }

    public void waitJobFinish(
            ClientJobProxy clientJobProxy,
            Integer userId,
            Long jobInstanceId,
            String jobEngineId,
            SeaTunnelClient seaTunnelClient) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        CompletableFuture<JobResult> future =
                CompletableFuture.supplyAsync(clientJobProxy::waitForJobCompleteV2, executor);
        JobResult jobResult = new JobResult(JobStatus.FAILED, "");
        try {
            jobResult = future.get();
            executor.shutdown();
        } catch (InterruptedException e) {
            jobResult.setError(e.getMessage());
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            jobResult.setError(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            seaTunnelClient.close();
            log.info("and jobInstanceService.complete begin");
            jobInstanceService.complete(userId, jobInstanceId, jobEngineId, jobResult);
        }
    }

    private SeaTunnelClient createSeaTunnelClient() {
        ClientConfig clientConfig = ConfigProvider.locateAndGetClientConfig();
        return new SeaTunnelClient(clientConfig);
    }

    @Override
    public Result<Void> jobPause(Integer userId, Long jobInstanceId) {
        JobInstance jobInstance = jobInstanceDao.getJobInstance(jobInstanceId);
        if (getJobStatusFromEngine(jobInstance, jobInstance.getJobEngineId())
                == JobStatus.RUNNING) {
            pauseJobInEngine(jobInstance.getJobEngineId());
        }
        return Result.success();
    }

    private JobStatus getJobStatusFromEngine(@NonNull JobInstance jobInstance, String jobEngineId) {

        Engine engine = new Engine(jobInstance.getEngineName(), jobInstance.getEngineVersion());

        IEngineMetricsExtractor engineMetricsExtractor =
                (new EngineMetricsExtractorFactory(engine)).getEngineMetricsExtractor();

        return engineMetricsExtractor.getJobStatus(jobEngineId);
    }

    private void pauseJobInEngine(@NonNull String jobEngineId) {
        SeaTunnelEngineProxy.getInstance().pauseJob(jobEngineId);
    }

    @Override
    public Result<Void> jobStore(Integer userId, Long jobInstanceId) {
        JobInstance jobInstance = jobInstanceDao.getJobInstance(jobInstanceId);

        String projectRoot = System.getProperty("user.dir");
        String filePath =
                projectRoot
                        + File.separator
                        + "profile"
                        + File.separator
                        + jobInstance.getJobDefineId()
                        + ".conf";
        log.info("jobStore filePath:{}", filePath);
        SeaTunnelEngineProxy.getInstance()
                .restoreJob(filePath, jobInstanceId, Long.valueOf(jobInstance.getJobEngineId()));
        return Result.success();
    }
}
