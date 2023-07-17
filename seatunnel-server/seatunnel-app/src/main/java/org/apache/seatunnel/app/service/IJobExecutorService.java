package org.apache.seatunnel.app.service;

import org.apache.seatunnel.app.common.Result;

public interface IJobExecutorService {

    public Result jobExecute(Integer userId, Long jobDefineId);

    public Result jobPause(Integer userId, Long jobInstanceId);

    public Result jobStore(Integer userId, Long jobInstanceId);
}
