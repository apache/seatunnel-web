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

package org.apache.seatunnel.scheduler.dolphinscheduler.impl;

import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.DEPENDENT_MODE_DEFAULT;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.DRY_RUN;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.EXEC_TYPE_COMPLEMENT;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.EXEC_TYPE_DEFAULT;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.FAILURE_STRATEGY_DEFAULT;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.MAX_WAITING_TIMES_DEFAULT;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.PAGE_NO_DEFAULT;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.PAGE_SIZE_MIN;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.PROCESS_INSTANCE_PRIORITY_DEFAULT;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.RELEASE_STATE_OFFLINE;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.RELEASE_STATE_ONLINE;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.RUN_MODE_DEFAULT;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.RUN_MODE_PARALLEL;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.TASK_DEPEND_TYPE_DEFAULT;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.WAITING_SLEEP_TIME_DEFAULT;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.WARNING_GROUP_ID_DEFAULT;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.WARNING_TYPE_DEFAULT;
import static org.apache.seatunnel.scheduler.dolphinscheduler.constants.DolphinSchedulerConstants.WORKER_GROUP_DEFAULT;
import static org.apache.seatunnel.server.common.Constants.COMMA;
import static org.apache.seatunnel.server.common.DateUtils.DEFAULT_DATETIME_FORMAT;

import org.apache.seatunnel.scheduler.api.IInstanceService;
import org.apache.seatunnel.scheduler.api.IJobService;
import org.apache.seatunnel.scheduler.api.dto.ComplementDataDto;
import org.apache.seatunnel.scheduler.api.dto.ExecuteDto;
import org.apache.seatunnel.scheduler.api.dto.InstanceDto;
import org.apache.seatunnel.scheduler.api.dto.InstanceListDto;
import org.apache.seatunnel.scheduler.api.dto.JobDto;
import org.apache.seatunnel.scheduler.api.dto.JobListDto;
import org.apache.seatunnel.scheduler.api.dto.JobSimpleInfoDto;
import org.apache.seatunnel.scheduler.dolphinscheduler.IDolphinSchedulerService;
import org.apache.seatunnel.scheduler.dolphinscheduler.dto.ListProcessDefinitionDto;
import org.apache.seatunnel.scheduler.dolphinscheduler.dto.ProcessDefinitionDto;
import org.apache.seatunnel.scheduler.dolphinscheduler.dto.SchedulerDto;
import org.apache.seatunnel.scheduler.dolphinscheduler.dto.StartProcessDefinitionDto;
import org.apache.seatunnel.scheduler.dolphinscheduler.dto.TaskDescriptionDto;
import org.apache.seatunnel.scheduler.dolphinscheduler.dto.UpdateProcessDefinitionDto;
import org.apache.seatunnel.server.common.DateUtils;
import org.apache.seatunnel.server.common.PageData;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class JobServiceImpl implements IJobService {

    private final IDolphinSchedulerService dolphinSchedulerService;
    private final IInstanceService instanceService;

    public JobServiceImpl(IDolphinSchedulerService dolphinSchedulerService, IInstanceService instanceService) {
        this.dolphinSchedulerService = dolphinSchedulerService;
        this.instanceService = instanceService;
    }

    private long maxWaitingTimes = MAX_WAITING_TIMES_DEFAULT;
    private long waitingSleepTime = WAITING_SLEEP_TIME_DEFAULT;

    @Override
    public long submitJob(JobDto dto) {
        // one process == one seatunnel script == one job

        final ProcessDefinitionDto processDefinition = getProcessDefinitionDto(dto);

        dto.setJobId(processDefinition.getCode());
        dolphinSchedulerService.updateProcessDefinitionState(processDefinition.getCode(), processDefinition.getName(), RELEASE_STATE_ONLINE);
        final SchedulerDto schedulerDto = dolphinSchedulerService.createOrUpdateSchedule(dto);
        dolphinSchedulerService.scheduleOnline(schedulerDto.getId());

        return processDefinition.getCode();
    }

    @Override
    public void offlineJob(JobDto dto) {
        dolphinSchedulerService.updateProcessDefinitionState(dto.getJobId(), dto.getJobName(), RELEASE_STATE_OFFLINE);
    }

    @Override
    public PageData<JobSimpleInfoDto> list(JobListDto dto) {
        final ListProcessDefinitionDto listDto = new ListProcessDefinitionDto();
        listDto.setName(dto.getName());
        listDto.setPageNo(dto.getPageNo());
        listDto.setPageSize(dto.getPageSize());

        final PageData<ProcessDefinitionDto> processPageData = dolphinSchedulerService.listProcessDefinition(listDto);
        final List<JobSimpleInfoDto> data = processPageData.getData().stream().map(p -> JobSimpleInfoDto.builder()
                .jobId(p.getCode())
                .jobStatus(p.getReleaseState())
                .createTime(p.getCreateTime())
                .updateTime(p.getUpdateTime())
                .creatorName(p.getUsername())
                .menderName(p.getUsername())
                .build())
                .collect(Collectors.toList());
        return new PageData<>(processPageData.getTotalCount(), data);
    }

    @Override
    @SuppressWarnings("magicnumber")
    public InstanceDto execute(ExecuteDto dto) {
        ProcessDefinitionDto processDefinition = null;
        final JobDto jobDto = dto.getJobDto();
        if (Objects.isNull(jobDto.getJobId())) {
            // need to create a temporary process definition and execute it.
            processDefinition = getProcessDefinitionDto(jobDto);
            jobDto.setJobId(processDefinition.getCode());
            dolphinSchedulerService.updateProcessDefinitionState(processDefinition.getCode(), processDefinition.getName(), RELEASE_STATE_ONLINE);
        }

        final ComplementDataDto complementDataDto = dto.getComplementDataDto();
        String execType;
        String runMode = RUN_MODE_DEFAULT;

        Date startTime = new Date();
        Date endTime = new Date();
        int parallelismNum = 1;
        if (Objects.isNull(complementDataDto)) {
            execType = EXEC_TYPE_DEFAULT;
        } else {
            execType = EXEC_TYPE_COMPLEMENT;
            if (Objects.nonNull(complementDataDto.getParallelismNum())) {
                runMode = RUN_MODE_PARALLEL;
            }
            startTime = complementDataDto.getStartTime();
            endTime = complementDataDto.getEndTime();
            parallelismNum = complementDataDto.getParallelismNum();
        }

        final StartProcessDefinitionDto startProcessDefinitionDto = StartProcessDefinitionDto.builder()
                .processDefinitionCode(jobDto.getJobId())
                .failureStrategy(FAILURE_STRATEGY_DEFAULT)
                .warningType(WARNING_TYPE_DEFAULT)
                .warningGroupId(WARNING_GROUP_ID_DEFAULT)
                .taskDependType(TASK_DEPEND_TYPE_DEFAULT)
                .runMode(runMode)
                .processInstancePriority(PROCESS_INSTANCE_PRIORITY_DEFAULT)
                .workerGroup(WORKER_GROUP_DEFAULT)
                .dryRun(DRY_RUN)
                .scheduleTime(DateUtils.format(startTime, DEFAULT_DATETIME_FORMAT).concat(COMMA).concat(DateUtils.format(endTime, DEFAULT_DATETIME_FORMAT)))
                .execType(execType)
                .dependentMode(DEPENDENT_MODE_DEFAULT)
                .expectedParallelismNumber(parallelismNum)
                .build();
        dolphinSchedulerService.startProcessDefinition(startProcessDefinitionDto);

        if (Objects.nonNull(processDefinition)){

            final long code = processDefinition.getCode();
            final String name = processDefinition.getName();

            InstanceDto instanceDto = null;
            // waiting dolphinscheduler generate instance.
            for (int i = 0; i < maxWaitingTimes; i++) {
                // get instance by process definition name
                final InstanceListDto instanceListDto = InstanceListDto.builder()
                        .pageNo(PAGE_NO_DEFAULT)
                        .pageSize(PAGE_SIZE_MIN)
                        .name(processDefinition.getName())
                        .build();
                final PageData<InstanceDto> instancePageData = instanceService.list(instanceListDto);
                if (!CollectionUtils.isEmpty(instancePageData.getData())) {
                    instanceDto = instancePageData.getData().get(0);
                    break;
                }
                try {
                    Thread.sleep(waitingSleepTime);
                } catch (InterruptedException e) {
                    log.error("waiting for getting instance failed", e);
                    throw new SeatunnelException(SeatunnelErrorEnum.GET_INSTANCE_FAILED);
                }
            }

            CompletableFuture.runAsync(() -> {
                // clear temporary process definition
                dolphinSchedulerService.updateProcessDefinitionState(code, name, RELEASE_STATE_OFFLINE);
            }).whenComplete((_return, e) -> {
                if (Objects.nonNull(e)) {
                    log.error("clear temporary process definition failed, process definition code is [{}], name is [{}]",
                            code, name, e);
                }
            });
            return instanceDto;

        } else {
            return null;
        }
    }

    @Override
    public void kill(Long instanceId) {
        dolphinSchedulerService.killProcessInstance(instanceId);
    }

    private ProcessDefinitionDto getProcessDefinitionDto(JobDto dto) {
        final TaskDescriptionDto taskDescriptionDto = TaskDescriptionDto.builder()
                .name(dto.getJobName())
                .executeScript(dto.getExecutorScript())
                .content(dto.getJobContent())
                .params(dto.getParams())
                .retryInterval(dto.getSchedulerConfigDto().getRetryInterval())
                .retryTimes(dto.getSchedulerConfigDto().getRetryTimes())
                .build();

        final UpdateProcessDefinitionDto processDto = UpdateProcessDefinitionDto.builder()
                .name(dto.getJobName())
                .startTime(dto.getSchedulerConfigDto().getStartTime())
                .endTime(dto.getSchedulerConfigDto().getEndTime())
                .cronExpression(dto.getSchedulerConfigDto().getTriggerExpression())
                .taskDescriptionDto(taskDescriptionDto)
                .processDefinitionCode(dto.getJobId())
                .build();

        return dolphinSchedulerService.createOrUpdateProcessDefinition(processDto);
    }
}
