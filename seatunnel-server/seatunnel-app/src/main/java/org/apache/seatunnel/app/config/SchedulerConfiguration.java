package org.apache.seatunnel.app.config;

import org.apache.seatunnel.scheduler.api.IInstanceService;
import org.apache.seatunnel.scheduler.api.IJobService;
import org.apache.seatunnel.scheduler.api.ISchedulerManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Configuration
public class SchedulerConfiguration {

    @Resource
    private ISchedulerManager schedulerManager;

    @Bean
    public IJobService getJobService() {
        return schedulerManager.getJobService();
    }

    @Bean
    public IInstanceService getInstanceService() {
        return schedulerManager.getInstanceService();
    }

    @PreDestroy
    public void close() throws Exception {
        schedulerManager.close();
    }
}
