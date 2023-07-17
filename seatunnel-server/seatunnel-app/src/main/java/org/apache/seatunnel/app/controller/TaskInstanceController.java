package org.apache.seatunnel.app.controller;

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.domain.dto.job.SeaTunnelJobInstanceDto;
import org.apache.seatunnel.app.domain.response.PageInfo;
import org.apache.seatunnel.app.service.ITaskInstanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

/** @Description @ClassName TaskInstance @Author zhang @Date 2023/7/4 11:35 */
@RequestMapping("/seatunnel/api/v1/task")
@RestController
public class TaskInstanceController {

    @Autowired ITaskInstanceService taskInstanceService;

    @GetMapping("/jobMetrics")
    @ApiOperation(value = "get the jobMetrics list ", httpMethod = "GET")
    public Result<PageInfo<SeaTunnelJobInstanceDto>> getTaskInstanceList(
            @RequestAttribute(name = "userId") Integer userId,
            @RequestParam(name = "jobDefineName", required = false) String jobDefineName,
            @RequestParam(name = "executorName", required = false) String executorName,
            @RequestParam(name = "stateType", required = false) String stateType,
            @RequestParam(name = "startDate", required = false) String startTime,
            @RequestParam(name = "endDate", required = false) String endTime,
            @RequestParam("syncTaskType") String syncTaskType,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("pageSize") Integer pageSize) {
        Result<PageInfo<SeaTunnelJobInstanceDto>> result =
                taskInstanceService.getSyncTaskInstancePaging(
                        userId,
                        jobDefineName,
                        executorName,
                        stateType,
                        startTime,
                        endTime,
                        syncTaskType,
                        pageNo,
                        pageSize);

        return result;
    }
}
