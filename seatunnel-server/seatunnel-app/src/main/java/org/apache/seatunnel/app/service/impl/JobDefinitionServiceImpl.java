package org.apache.seatunnel.app.service.impl;

import org.apache.seatunnel.app.common.EngineType;
import org.apache.seatunnel.app.dal.dao.IJobDefinitionDao;
import org.apache.seatunnel.app.dal.dao.IJobTaskDao;
import org.apache.seatunnel.app.dal.dao.IJobVersionDao;
import org.apache.seatunnel.app.dal.entity.JobDefinition;
import org.apache.seatunnel.app.dal.entity.JobTask;
import org.apache.seatunnel.app.dal.entity.JobVersion;
import org.apache.seatunnel.app.domain.request.job.DataSourceOption;
import org.apache.seatunnel.app.domain.request.job.JobReq;
import org.apache.seatunnel.app.domain.response.PageInfo;
import org.apache.seatunnel.app.domain.response.job.JobDefinitionRes;
import org.apache.seatunnel.app.permission.constants.SeatunnelFuncPermissionKeyConstant;
import org.apache.seatunnel.app.service.IJobDefinitionService;
import org.apache.seatunnel.common.constants.JobMode;
import org.apache.seatunnel.server.common.CodeGenerateUtils;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;

import javax.annotation.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class JobDefinitionServiceImpl extends SeatunnelBaseServiceImpl
        implements IJobDefinitionService {

    private static final String DEFAULT_VERSION = "1.0";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Resource(name = "jobDefinitionDaoImpl")
    private IJobDefinitionDao jobDefinitionDao;

    @Resource(name = "jobTaskDaoImpl")
    private IJobTaskDao jobTaskDao;

    @Resource(name = "jobVersionDaoImpl")
    private IJobVersionDao jobVersionDao;

    @Override
    @Transactional
    public long createJob(int userId, JobReq jobReq)
            throws CodeGenerateUtils.CodeGenerateException {
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_DEFINITION_CREATE, userId);
        long uuid = CodeGenerateUtils.getInstance().genCode();
        jobDefinitionDao.add(
                JobDefinition.builder()
                        .id(uuid)
                        .name(jobReq.getName())
                        .description(jobReq.getDescription())
                        .createUserId(userId)
                        .updateUserId(userId)
                        .jobType(jobReq.getJobType().name())
                        .projectCode(jobReq.getProjectCode())
                        .build());
        jobVersionDao.createVersion(
                JobVersion.builder()
                        .jobId(uuid)
                        .createUserId(userId)
                        .updateUserId(userId)
                        .name(DEFAULT_VERSION)
                        .id(uuid)
                        .engineName(EngineType.SeaTunnel.name())
                        .jobMode(JobMode.BATCH.name())
                        .engineVersion("2.3.0")
                        .build());
        return uuid;
    }

    @Override
    public PageInfo<JobDefinitionRes> getJob(
            String name, Integer pageNo, Integer pageSize, List<Long> projectCodes) {
        return getJob(name, pageNo, pageSize, projectCodes, null);
    }

    @Override
    public PageInfo<JobDefinitionRes> getJob(
            String searchName,
            Integer pageNo,
            Integer pageSize,
            List<Long> projectCodes,
            String jobMode) {
        if (CollectionUtils.isEmpty(projectCodes)) {
            return new PageInfo<>();
        }
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_DEFINITION_VIEW, 0);
        if (StringUtils.isNotEmpty(jobMode)) {
            try {
                JobMode.valueOf(jobMode);
            } catch (Exception e) {
                throw new SeatunnelException(
                        SeatunnelErrorEnum.ILLEGAL_STATE, "Unsupported JobMode");
            }
        }
        PageInfo<JobDefinition> jobDefinitionPageInfo =
                jobDefinitionDao.getJob(searchName, pageNo, pageSize, projectCodes, jobMode);
        List<Integer> userIds =
                jobDefinitionPageInfo.getData().stream()
                        .map(JobDefinition::getCreateUserId)
                        .collect(Collectors.toList());
        userIds.addAll(
                jobDefinitionPageInfo.getData().stream()
                        .map(JobDefinition::getUpdateUserId)
                        .collect(Collectors.toList()));
        List<JobDefinitionRes> jobDefinitionResList = new ArrayList<>();
        for (int i = 0; i < jobDefinitionPageInfo.getData().size(); i++) {
            JobDefinition jobDefinition = jobDefinitionPageInfo.getData().get(i);
            JobDefinitionRes jobDefinitionRes = new JobDefinitionRes();
            jobDefinitionRes.setId(jobDefinition.getId());
            jobDefinitionRes.setName(jobDefinition.getName());
            jobDefinitionRes.setDescription(jobDefinition.getDescription());
            jobDefinitionRes.setJobType(jobDefinition.getJobType());
            jobDefinitionRes.setCreateUserId(jobDefinition.getCreateUserId());
            jobDefinitionRes.setUpdateUserId(jobDefinitionRes.getUpdateUserId());
            jobDefinitionRes.setCreateTime(jobDefinition.getCreateTime());
            jobDefinitionRes.setUpdateTime(jobDefinition.getUpdateTime());
            jobDefinitionRes.setProjectCode(jobDefinition.getProjectCode());
            jobDefinitionResList.add(jobDefinitionRes);
        }
        PageInfo<JobDefinitionRes> pageInfo = new PageInfo<>();
        pageInfo.setPageNo(jobDefinitionPageInfo.getPageNo());
        pageInfo.setPageSize(jobDefinitionPageInfo.getPageSize());
        pageInfo.setTotalCount(jobDefinitionPageInfo.getTotalCount());
        pageInfo.setData(jobDefinitionResList);
        return pageInfo;
    }

    @Override
    public Map<Long, String> getJob(@NonNull List<Long> projectCodes, @NonNull String name) {
        if (CollectionUtils.isEmpty(projectCodes)) {
            return Collections.emptyMap();
        }
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_DEFINITION_VIEW, 0);
        List<JobDefinition> job = jobDefinitionDao.getJob(projectCodes, name);
        if (CollectionUtils.isEmpty(job)) {
            return new HashMap<>();
        }

        Map<Long, String> jobDefineMap = new HashMap<>();
        job.forEach(
                jobDefine -> {
                    jobDefineMap.put(jobDefine.getId(), jobDefine.getName());
                });

        return jobDefineMap;
    }

    @Override
    public JobDefinition getJobDefinitionByJobId(long jobId) {
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_DEFINITION_DETAIL, 0);
        return jobDefinitionDao.getJob(jobId);
    }

    @Override
    public List<JobVersion> getJobVersionByDataSourceId(long datasourceId) {
        List<Long> versionIds =
                jobTaskDao.getJobTaskByDataSourceId(datasourceId).stream()
                        .map(JobTask::getVersionId)
                        .distinct()
                        .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(versionIds)) {
            return new ArrayList<>();
        }
        return jobVersionDao.getVersionsByIds(versionIds);
    }

    @Override
    public boolean getUsedByDataSourceIdAndVirtualTable(long datasourceId, String tableName) {
        List<DataSourceOption> options =
                jobTaskDao.getJobTaskByDataSourceId(datasourceId).stream()
                        .map(JobTask::getDataSourceOption)
                        .distinct()
                        .map(
                                option -> {
                                    try {
                                        return StringUtils.isEmpty(option)
                                                ? null
                                                : OBJECT_MAPPER.readValue(
                                                        option, DataSourceOption.class);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        return options.stream().anyMatch(option -> option.getTables().contains(tableName));
    }

    @Override
    public void deleteJob(long id, long projectCode) {
        funcPermissionCheck(SeatunnelFuncPermissionKeyConstant.JOB_DEFINITION_DELETE, 0);
        jobDefinitionDao.delete(id, projectCode);
    }
}
