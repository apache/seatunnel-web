package org.apache.seatunnel.app.dal.dao;

import org.apache.seatunnel.app.dal.entity.JobMetricsHistory;

import java.util.List;

public interface IJobMetricsHistoryDao {
    /** 插入一条监控记录 */
    void insert(JobMetricsHistory jobMetricsHistory);

    /** 批量插入监控记录 */
    void insertBatch(List<JobMetricsHistory> jobMetricsHistories);

    /** 根据作业实例ID获取监控历史记录 */
    List<JobMetricsHistory> getByJobInstanceId(Long jobInstanceId);

    /** 根据作业实例ID和管道ID获取监控历史记录 */
    List<JobMetricsHistory> getByJobInstanceIdAndPipelineId(Long jobInstanceId, Integer pipelineId);
}
