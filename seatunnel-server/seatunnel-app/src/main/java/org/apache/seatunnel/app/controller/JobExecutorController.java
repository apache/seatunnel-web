package org.apache.seatunnel.app.controller;

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.domain.response.executor.JobExecutorRes;
import org.apache.seatunnel.app.service.IJobExecutorService;
import org.apache.seatunnel.app.service.IJobInstanceService;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import java.io.IOException;

/** @Description @ClassName JobExecutorController @Author zhang @Date 2023/6/30 15:09 */
@Slf4j
@RequestMapping("/seatunnel/api/v1/job/executor")
@RestController
public class JobExecutorController {

    @Autowired IJobExecutorService jobExecutorService;
    @Resource private IJobInstanceService jobInstanceService;

    @GetMapping("/execute")
    @ApiOperation(value = "Execute synchronization tasks", httpMethod = "GET")
    public Result jobExecutor(
            @ApiParam(value = "userId", required = true) @RequestAttribute("userId") Integer userId,
            @ApiParam(value = "jobDefineId", required = true) @RequestParam("jobDefineId")
                    Long jobDefineId) {
        return jobExecutorService.jobExecute(userId, jobDefineId);
    }

    @GetMapping("/resource")
    @ApiOperation(value = "get the resource for job executor", httpMethod = "GET")
    public Result<JobExecutorRes> resource(
            @ApiParam(value = "userId", required = true) @RequestParam Integer userId,
            @ApiParam(value = "Job define id", required = true) @RequestParam Long jobDefineId)
            throws IOException {
        try {
            JobExecutorRes executeResource =
                    jobInstanceService.createExecuteResource(userId, jobDefineId);
            return Result.success(executeResource);
        } catch (Exception e) {
            log.error("Get the resource for job executor error", e);
            throw new SeatunnelException(SeatunnelErrorEnum.ILLEGAL_STATE, e.getMessage());
        }
    }

    @GetMapping("/pause")
    public Result jobPause(
            @ApiParam(value = "userId", required = true) @RequestAttribute("userId") Integer userId,
            @ApiParam(value = "jobInstanceId", required = true) @RequestParam Long jobInstanceId) {
        return jobExecutorService.jobPause(userId, jobInstanceId);
    }

    @GetMapping("/restore")
    public Result jobRestore(
            @ApiParam(value = "userId", required = true) @RequestAttribute("userId") Integer userId,
            @ApiParam(value = "jobInstanceId", required = true) @RequestParam Long jobInstanceId) {
        return jobExecutorService.jobStore(userId, jobInstanceId);
    }
}
