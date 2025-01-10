package org.apache.seatunnel.app.scheduler;

import org.apache.seatunnel.shade.com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.apache.seatunnel.app.dal.dao.IJobInstanceDao;
import org.apache.seatunnel.app.dal.dao.IJobMetricsHistoryDao;
import org.apache.seatunnel.app.dal.entity.JobInstance;
import org.apache.seatunnel.app.dal.entity.JobMetricsHistory;
import org.apache.seatunnel.app.domain.response.metrics.JobPipelineDetailMetricsRes;
import org.apache.seatunnel.app.service.IJobMetricsService;
import org.apache.seatunnel.app.service.TaskProcessingService;
import org.apache.seatunnel.engine.core.job.JobStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MonitorTaskScheduler {

    @Autowired private TaskProcessingService taskProcessingService;

    private final ExecutorService executorService;

    @Resource private IJobInstanceDao jobInstanceDao;

    @Resource private IJobMetricsService jobMetricsService;

    @Resource private IJobMetricsHistoryDao jobMetricsHistoryDao;

    private final ConcurrentHashMap<Long, JobInstance> jobInstanceMap = new ConcurrentHashMap<>();

    public MonitorTaskScheduler() {
        // 创建线程池
        this.executorService =
                new ThreadPoolExecutor(
                        5, // 核心线程数
                        10, // 最大线程数
                        60L, // 空闲线程存活时间
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(100), // 任务队列
                        new ThreadFactoryBuilder()
                                .setNameFormat("task-processor-%d")
                                .setUncaughtExceptionHandler(
                                        (t, e) -> log.error("线程 {} 发生未捕获异常", t.getName(), e))
                                .build(),
                        new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
                        );
    }

    @Scheduled(initialDelay = 0, fixedRate = 60000) // 立即开始执行，每60秒执行一次
    public void updateJobInstance() {
        try {
            log.info("开始更新任务实例信息...");
            List<JobInstance> allJobInstance =
                    jobInstanceDao.getAllJobInstance(Arrays.asList(16261985715328L));

            Map<Long, JobInstance> newInstanceMap =
                    allJobInstance.stream()
                            .collect(
                                    Collectors.toMap(
                                            JobInstance::getId,
                                            instance -> instance,
                                            (existing, replacement) -> replacement));

            jobInstanceMap.clear();
            jobInstanceMap.putAll(newInstanceMap);

            log.info("任务实例信息更新完成，当前共有 {} 个实例", jobInstanceMap.size());
        } catch (Exception e) {
            log.error("更新任务实例信息异常", e);
        }
    }

    public JobInstance getJobInstance(Long jobInstanceId) {
        return jobInstanceMap.get(jobInstanceId);
    }

    public List<JobInstance> getAllJobInstances() {
        return new ArrayList<>(jobInstanceMap.values());
    }

    @Scheduled(fixedDelay = 5000) // 每5秒执行一次
    public void scheduleTasks() {
        jobInstanceMap
                .values()
                .forEach(
                        jobInstance -> {
                            if (jobInstance.getJobStatus() != JobStatus.RUNNING) {
                                return;
                            }
                            try {
                                executorService.submit(
                                        () -> {
                                            try {
                                                Long jobInstanceId = jobInstance.getId();
                                                List<JobPipelineDetailMetricsRes> metricsResList =
                                                        jobMetricsService
                                                                .getJobPipelineDetailMetricsRes(
                                                                        jobInstance);

                                                if (metricsResList != null
                                                        && !metricsResList.isEmpty()) {
                                                    List<JobMetricsHistory> historyList =
                                                            metricsResList.stream()
                                                                    .map(
                                                                            metrics ->
                                                                                    convertToJobMetricsHistory(
                                                                                            metrics,
                                                                                            jobInstanceId))
                                                                    .collect(Collectors.toList());

                                                    jobMetricsHistoryDao.insertBatch(historyList);
                                                    log.info(
                                                            "成功保存作业 {} 的监控指标，共 {} 条记录",
                                                            jobInstanceId,
                                                            historyList.size());
                                                }
                                            } catch (Exception e) {
                                                log.error("保存作业监控指标异常", e);
                                            }
                                        });
                            } catch (Exception e) {
                                log.error("任务调度异常", e);
                            }
                        });
    }

    private JobMetricsHistory convertToJobMetricsHistory(
            JobPipelineDetailMetricsRes metrics, Long jobInstanceId) {
        return JobMetricsHistory.builder()
                .id(generateId()) // 需要实现一个生成唯一ID的方法
                .jobInstanceId(jobInstanceId)
                .pipelineId(metrics.getPipelineId())
                .readRowCount(metrics.getReadRowCount())
                .writeRowCount(metrics.getWriteRowCount())
                .sourceTableNames(metrics.getSourceTableNames())
                .sinkTableNames(metrics.getSinkTableNames())
                .readQps(metrics.getReadQps())
                .writeQps(metrics.getWriteQps())
                .recordDelay(metrics.getRecordDelay())
                .status(metrics.getStatus())
                .createUserId(-1)
                .updateUserId(-1)
                //                .createTime(new Date())
                //                .updateTime(new Date())
                .build();
    }

    private Long generateId() {
        // 这里可以使用分布式ID生成器，比如雪花算法
        return System.currentTimeMillis();
    }

    @PreDestroy
    public void shutdown() {
        log.info("正在关闭任务调度器...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
