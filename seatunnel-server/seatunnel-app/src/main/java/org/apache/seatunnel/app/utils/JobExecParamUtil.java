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
package org.apache.seatunnel.app.utils;

import org.apache.seatunnel.shade.com.typesafe.config.Config;
import org.apache.seatunnel.shade.com.typesafe.config.ConfigValueFactory;

import org.apache.seatunnel.app.dal.entity.JobTask;
import org.apache.seatunnel.app.domain.request.job.JobExecParam;

import java.util.List;
import java.util.Map;

public class JobExecParamUtil {

    // The maximum length of the job execution error message, 4KB
    private static final int ERROR_MESSAGE_MAX_LENGTH = 4096;

    public static String getJobInstanceErrorMessage(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > ERROR_MESSAGE_MAX_LENGTH
                ? message.substring(0, ERROR_MESSAGE_MAX_LENGTH)
                : message;
    }

    public static Config updateEnvConfig(JobExecParam jobExecParam, Config envConfig) {
        if (jobExecParam == null || jobExecParam.getEnv() == null) {
            return envConfig;
        }
        return updateConfig(envConfig, jobExecParam.getEnv());
    }

    private static Config updateConfig(Config config, Map<String, String> properties) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            config =
                    config.withValue(
                            entry.getKey(), ConfigValueFactory.fromAnyRef(entry.getValue()));
        }
        return config;
    }

    public static Config updateTaskConfig(
            JobExecParam jobExecParam, Config taskConfig, String taskName) {
        if (jobExecParam == null
                || jobExecParam.getTasks() == null
                || jobExecParam.getTasks().get(taskName) == null) {
            return taskConfig;
        }
        return updateConfig(taskConfig, jobExecParam.getTasks().get(taskName));
    }

    public static Config updateQueryTaskConfig(
            JobExecParam jobExecParam, Config taskConfig, String taskName) {
        if (jobExecParam == null
                || jobExecParam.getTasks() == null
                || jobExecParam.getTasks().get(taskName) == null) {
            return taskConfig;
        }
        String query = jobExecParam.getTasks().get(taskName).get("query");
        if (query != null) {
            return taskConfig.withValue("query", ConfigValueFactory.fromAnyRef(query));
        }
        return taskConfig;
    }

    public static void updateDataSource(JobExecParam jobExecParam, List<JobTask> tasks) {
        if (jobExecParam == null || jobExecParam.getDatasource() == null) {
            return;
        }
        // Check current user has permission to access the datasource
        jobExecParam
                .getDatasource()
                .forEach(
                        (taskName, datasourceId) -> {
                            tasks.stream()
                                    .filter(task -> task.getName().equals(taskName))
                                    .findFirst()
                                    .ifPresent(
                                            task ->
                                                    task.setDataSourceId(
                                                            Long.parseLong(datasourceId)));
                        });
    }
}
