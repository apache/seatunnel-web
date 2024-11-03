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

import org.apache.seatunnel.app.dal.entity.JobTask;
import org.apache.seatunnel.app.domain.request.job.JobExecParam;
import org.apache.seatunnel.engine.core.job.JobStatus;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobUtils {

    // The maximum length of the job execution error message, 4KB
    private static final int ERROR_MESSAGE_MAX_LENGTH = 4096;
    private static final Pattern placeholderPattern =
            Pattern.compile("(\\\\{0,2})\\$\\{(\\w+)(?::(.*?))?\\}");

    public static String getJobInstanceErrorMessage(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > ERROR_MESSAGE_MAX_LENGTH
                ? message.substring(0, ERROR_MESSAGE_MAX_LENGTH)
                : message;
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

    public static boolean isJobEndStatus(JobStatus jobStatus) {
        return JobStatus.FINISHED == jobStatus
                || JobStatus.CANCELED == jobStatus
                || JobStatus.FAILED == jobStatus;
    }

    // Replace placeholders in job config with actual values
    public static String replaceJobConfigPlaceholders(
            String jobConfigString, JobExecParam jobExecParam) {
        Map<String, String> placeholderValues =
                (jobExecParam != null && jobExecParam.getPlaceholderValues() != null)
                        ? jobExecParam.getPlaceholderValues()
                        : Collections.emptyMap();
        Matcher matcher = placeholderPattern.matcher(jobConfigString);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String escapeCharacter = matcher.group(1);
            String placeholderName = matcher.group(2);

            if (escapeCharacter != null && !escapeCharacter.isEmpty()) {
                String withoutEscape =
                        matcher.group().replace("\\\\${", "${").replace("\\${", "${");
                matcher.appendReplacement(result, Matcher.quoteReplacement(withoutEscape));
                // remove the escape character and continue
                continue;
            }
            String replacement = placeholderValues.getOrDefault(placeholderName, matcher.group(3));
            if (replacement == null) {
                throw new SeatunnelException(
                        SeatunnelErrorEnum.JOB_NO_VALUE_FOUND_FOR_PLACEHOLDER, placeholderName);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
