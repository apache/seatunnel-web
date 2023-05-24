package org.apache.seatunnel.app.service.impl;

import org.apache.seatunnel.app.dal.entity.JobLine;
import org.apache.seatunnel.app.dal.entity.JobTask;
import org.apache.seatunnel.app.domain.response.executor.JobExecutorRes;
import org.apache.seatunnel.app.service.IJobInstanceService;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Slf4j
public class JobInstanceServiceImpl implements IJobInstanceService {

    @Override
    public JobExecutorRes createExecuteResource(
            @NonNull Integer userId, @NonNull Long projectCode, @NonNull Long jobDefineId) {
        return null;
    }

    @Override
    public String generateJobConfig(
            Long jobId, List<JobTask> tasks, List<JobLine> lines, String envStr) {
        return null;
    }

    @Override
    public JobExecutorRes getExecuteResource(@NonNull Long jobEngineId) {
        return null;
    }

    @Override
    public void complete(
            @NonNull Integer userId,
            @NonNull Long projectCode,
            @NonNull Long jobInstanceId,
            @NonNull String jobEngineId) {}
}
