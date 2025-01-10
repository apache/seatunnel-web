package org.apache.seatunnel.app.service;

import org.apache.seatunnel.app.domain.TaskInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TaskProcessingService {

    private TaskInfoRepository taskInfoRepository;

    @Autowired private HttpClientService httpClientService;

    @Transactional
    public void processTask(TaskInfo taskInfo) {
        try {
            // 更新任务状态为处理中
            taskInfo.setStatus("PROCESSING");
            taskInfo.setUpdateTime(LocalDateTime.now());
            //            taskInfoRepository.save(taskInfo);

            // 调用API获取数据
            String result = httpClientService.fetchDataFromApi(taskInfo.getTaskId());

            // 更新处理结果
            taskInfo.setResult(result);
            taskInfo.setStatus("COMPLETED");
            taskInfo.setUpdateTime(LocalDateTime.now());

        } catch (Exception e) {
            log.error("任务处理失败，taskId: {}", taskInfo.getTaskId(), e);
            taskInfo.setStatus("FAILED");
            taskInfo.setResult("处理失败：" + e.getMessage());
        }

        //        taskInfoRepository.save(taskInfo);
    }

    public List<TaskInfo> getPendingTasks() {
        //        return taskInfoRepository.findPendingTasks();
        return new ArrayList<TaskInfo>();
    }
}
