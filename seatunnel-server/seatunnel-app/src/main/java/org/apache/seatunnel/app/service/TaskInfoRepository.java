package org.apache.seatunnel.app.service;

import org.apache.seatunnel.app.domain.TaskInfo;

import java.util.List;

public interface TaskInfoRepository {

    List<TaskInfo> findPendingTasks();
}
