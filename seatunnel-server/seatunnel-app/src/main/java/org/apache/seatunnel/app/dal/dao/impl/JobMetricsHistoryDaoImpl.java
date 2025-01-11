package org.apache.seatunnel.app.dal.dao.impl;

import org.apache.seatunnel.app.dal.dao.IJobMetricsHistoryDao;
import org.apache.seatunnel.app.dal.entity.JobMetricsHistory;
import org.apache.seatunnel.app.dal.mapper.JobMetricsHistoryMapper;

import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

@Repository
public class JobMetricsHistoryDaoImpl implements IJobMetricsHistoryDao {

    @Resource private JobMetricsHistoryMapper jobMetricsHistoryMapper;

    @Override
    public void insert(JobMetricsHistory jobMetricsHistory) {
        jobMetricsHistoryMapper.insert(jobMetricsHistory);
    }

    @Override
    public void insertBatch(List<JobMetricsHistory> jobMetricsHistories) {
        if (!jobMetricsHistories.isEmpty()) {
            jobMetricsHistoryMapper.insertBatchMetrics(jobMetricsHistories);
        }
    }

    @Override
    public List<JobMetricsHistory> getByJobInstanceId(Long jobInstanceId) {
        return jobMetricsHistoryMapper.queryJobMetricsHistoryByInstanceId(jobInstanceId);
    }

    @Override
    public List<JobMetricsHistory> getByJobInstanceIdAndPipelineId(
            Long jobInstanceId, Integer pipelineId) {
        return jobMetricsHistoryMapper.queryJobMetricsHistoryByInstanceIdAndPipelineId(
                jobInstanceId, pipelineId);
    }

    @Override
    public List<JobMetricsHistory> getByJobInstanceIdAndTimeRange(
            Long jobInstanceId, Date startTime, Date endTime) {
        return jobMetricsHistoryMapper.queryJobMetricsHistoryByInstanceIdAndTimeRange(
                jobInstanceId, startTime, endTime);
    }
}
