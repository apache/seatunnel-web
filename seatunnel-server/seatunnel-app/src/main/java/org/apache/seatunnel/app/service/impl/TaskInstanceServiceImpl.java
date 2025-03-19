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
import org.apache.seatunnel.app.common.Status;
import org.apache.seatunnel.app.dal.dao.IJobDefinitionDao;
import org.apache.seatunnel.app.dal.dao.IJobInstanceDao;
import org.apache.seatunnel.app.dal.entity.JobInstance;
import org.apache.seatunnel.app.domain.dto.job.SeaTunnelJobInstanceDto;
import org.apache.seatunnel.app.domain.response.executor.JobExecutionStatus;
import org.apache.seatunnel.app.domain.response.metrics.JobSummaryMetricsRes;
import org.apache.seatunnel.app.service.BaseService;
import org.apache.seatunnel.app.service.IJobDefinitionService;
import org.apache.seatunnel.app.service.IJobMetricsService;
import org.apache.seatunnel.app.service.ITaskInstanceService;
import org.apache.seatunnel.app.utils.PageInfo;
import org.apache.seatunnel.common.constants.JobMode;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.apache.commons.collections4.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TaskInstanceServiceImpl implements ITaskInstanceService<SeaTunnelJobInstanceDto> {

    @Autowired IJobInstanceDao jobInstanceDao;

    @Autowired IJobMetricsService jobMetricsService;

    @Autowired IJobDefinitionService jobDefinitionService;

    @Autowired BaseService baseService;

    @Autowired IJobDefinitionDao jobDefinitionDao;

    @Override
    public Result<PageInfo<SeaTunnelJobInstanceDto>> getSyncTaskInstancePaging(
            String jobDefineName,
            String executorName,
            String stateType,
            String startTime,
            String endTime,
            JobMode jobMode,
            Integer pageNo,
            Integer pageSize) {
        Result<PageInfo<SeaTunnelJobInstanceDto>> result = new Result<>();
        PageInfo<SeaTunnelJobInstanceDto> pageInfo = new PageInfo<>(pageNo, pageSize);
        result.setData(pageInfo);
        baseService.putMsg(result, Status.SUCCESS);

        Date startDate = dateConverter(startTime);
        Date endDate = dateConverter(endTime);

        IPage<SeaTunnelJobInstanceDto> jobInstanceIPage =
                jobInstanceDao.queryJobInstanceListPaging(
                        new Page<>(pageNo, pageSize), startDate, endDate, jobDefineName, jobMode);

        List<SeaTunnelJobInstanceDto> records = jobInstanceIPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return result;
        }
        addRunningTimeToResult(records);
        jobPipelineSummaryMetrics(records, jobMode);
        pageInfo.setTotal((int) jobInstanceIPage.getTotal());
        pageInfo.setTotalList(records);
        result.setData(pageInfo);
        return result;
    }

    private void populateExecutionMetricsData(
            JobMode jobMode, List<SeaTunnelJobInstanceDto> records) {
        addRunningTimeToResult(records);
        jobPipelineSummaryMetrics(records, jobMode);
    }

    private void addRunningTimeToResult(List<SeaTunnelJobInstanceDto> records) {
        for (SeaTunnelJobInstanceDto jobInstanceDto : records) {
            long runningTime = 0l;
            Date createTime = jobInstanceDto.getCreateTime();
            long createTimeSecond = createTime.toInstant().getEpochSecond();
            Date endTime = jobInstanceDto.getEndTime();
            if (endTime == null) {
                Date currentData = new Date();
                long currentDateSecond = currentData.toInstant().getEpochSecond();
                runningTime = Math.abs(currentDateSecond - createTimeSecond);
                jobInstanceDto.setRunningTime(runningTime);
            } else {
                long endTimeSecond = jobInstanceDto.getEndTime().toInstant().getEpochSecond();
                runningTime = Math.abs(endTimeSecond - createTimeSecond);
                jobInstanceDto.setRunningTime(runningTime);
            }
        }
    }

    public Date dateConverter(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            return dateFormat.parse(time);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void jobPipelineSummaryMetrics(List<SeaTunnelJobInstanceDto> records, JobMode jobMode) {
        try {
            ArrayList<Long> jobInstanceIdList = new ArrayList<>();
            HashMap<Long, Long> jobInstanceIdAndJobEngineIdMap = new HashMap<>();

            for (SeaTunnelJobInstanceDto jobInstance : records) {
                if (jobInstance.getId() != null && jobInstance.getJobEngineId() != null) {
                    jobInstanceIdList.add(jobInstance.getId());
                    jobInstanceIdAndJobEngineIdMap.put(
                            jobInstance.getId(), Long.valueOf(jobInstance.getJobEngineId()));
                }
            }

            Map<Long, JobSummaryMetricsRes> jobSummaryMetrics =
                    jobMetricsService.getALLJobSummaryMetrics(
                            jobInstanceIdAndJobEngineIdMap, jobInstanceIdList, jobMode);

            for (SeaTunnelJobInstanceDto taskInstance : records) {
                if (jobSummaryMetrics.get(taskInstance.getId()) != null) {
                    taskInstance.setWriteRowCount(
                            jobSummaryMetrics.get(taskInstance.getId()).getWriteRowCount());
                    taskInstance.setReadRowCount(
                            jobSummaryMetrics.get(taskInstance.getId()).getReadRowCount());
                }
            }
        } catch (Exception e) {
            for (SeaTunnelJobInstanceDto taskInstance : records) {
                log.error(
                        "instance {} {} set instance and engine id error", taskInstance.getId(), e);
            }
        }
    }

    @Override
    public Result<JobExecutionStatus> getJobExecutionStatus(long jobInstanceId) {
        JobInstance jobInstance = jobInstanceDao.getJobExecutionStatus(jobInstanceId);
        if (jobInstance == null) {
            throw new SeatunnelException(
                    SeatunnelErrorEnum.RESOURCE_NOT_FOUND, "Job instance not found");
        }
        return Result.success(
                new JobExecutionStatus(jobInstance.getJobStatus(), jobInstance.getErrorMessage()));
    }

    @Override
    public Result<SeaTunnelJobInstanceDto> getJobExecutionDetail(long jobInstanceId) {
        JobInstance jobInstance = jobInstanceDao.getJobInstance(jobInstanceId);
        if (jobInstance == null) {
            throw new SeatunnelException(
                    SeatunnelErrorEnum.RESOURCE_NOT_FOUND, "Job instance not found");
        }
        SeaTunnelJobInstanceDto executionDetails = convertToDto(jobInstance);
        populateExecutionMetricsData(
                jobInstance.getJobType(), Collections.singletonList(executionDetails));
        return Result.success(executionDetails);
    }

    private SeaTunnelJobInstanceDto convertToDto(JobInstance jobInstance) {
        SeaTunnelJobInstanceDto dto = new SeaTunnelJobInstanceDto();
        dto.setId(jobInstance.getId());
        dto.setJobDefineId(jobInstance.getJobDefineId());
        dto.setJobStatus(jobInstance.getJobStatus());
        dto.setJobConfig(jobInstance.getJobConfig());
        dto.setEngineName(jobInstance.getEngineName());
        dto.setEngineVersion(jobInstance.getEngineVersion());
        dto.setJobEngineId(jobInstance.getJobEngineId());
        dto.setCreateUserId(jobInstance.getCreateUserId());
        dto.setUpdateUserId(jobInstance.getUpdateUserId());
        dto.setCreateTime(jobInstance.getCreateTime());
        dto.setUpdateTime(jobInstance.getUpdateTime());
        dto.setEndTime(jobInstance.getEndTime());
        dto.setJobType(jobInstance.getJobType());
        dto.setErrorMessage(jobInstance.getErrorMessage());
        return dto;
    }

    @Override
    public Result<Void> deleteJobInstanceById(long jobInstanceId) {
        jobInstanceDao.deleteById(jobInstanceId);
        return Result.success();
    }
}
