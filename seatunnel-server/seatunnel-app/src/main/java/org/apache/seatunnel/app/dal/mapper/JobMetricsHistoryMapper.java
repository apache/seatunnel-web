package org.apache.seatunnel.app.dal.mapper;

import org.apache.seatunnel.app.dal.entity.JobMetricsHistory;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface JobMetricsHistoryMapper extends BaseMapper<JobMetricsHistory> {

    /** 批量插入监控历史记录 */
    void insertBatchMetrics(
            @Param("jobMetricsHistories") List<JobMetricsHistory> jobMetricsHistories);

    /** 根据作业实例ID查询监控历史记录 */
    List<JobMetricsHistory> queryJobMetricsHistoryByInstanceId(
            @Param("jobInstanceId") Long jobInstanceId);

    /** 根据作业实例ID和管道ID查询监控历史记录 */
    List<JobMetricsHistory> queryJobMetricsHistoryByInstanceIdAndPipelineId(
            @Param("jobInstanceId") Long jobInstanceId, @Param("pipelineId") Integer pipelineId);

    /** 根据作业实例ID和时间范围查询监控历史记录 */
    List<JobMetricsHistory> queryJobMetricsHistoryByInstanceIdAndTimeRange(
            @Param("jobInstanceId") Long jobInstanceId,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime);
}
