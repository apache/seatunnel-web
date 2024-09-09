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
import org.apache.seatunnel.shade.com.typesafe.config.ConfigFactory;

import org.apache.seatunnel.app.domain.request.job.JobExecParam;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JobUtilsTests {

    @Test
    public void testReplaceJobConfigPlaceholders_AllJobConfigPlaceholdersReplaced() {
        String jobConfigContent =
                "job.mode=${jobModeParam:BATCH}\ncheckpoint.interval=30\njob.name=${jobNameParam}";
        Map<String, String> paramValues = new HashMap<>();
        paramValues.put("jobModeParam", "STREAMING");
        paramValues.put("jobNameParam", "newJob");
        JobExecParam jobExecParam = getJobExecParam(paramValues);

        String expected = "job.mode=STREAMING\ncheckpoint.interval=30\njob.name=newJob";
        String actual = JobUtils.replaceJobConfigPlaceholders(jobConfigContent, jobExecParam);

        assertEquals(expected, actual);
    }

    @Test
    public void testReplaceJobConfigPlaceholders_JobConfig_PlaceholdersRepeat() {
        String jobConfigContent =
                "job.mode=${jobModeParam:BATCH}\ncheckpoint.interval=30\njob.name=${jobModeParam}";
        Map<String, String> paramValues = new HashMap<>();
        paramValues.put("jobModeParam", "STREAMING");
        JobExecParam jobExecParam = getJobExecParam(paramValues);

        String expected = "job.mode=STREAMING\ncheckpoint.interval=30\njob.name=STREAMING";
        String actual = JobUtils.replaceJobConfigPlaceholders(jobConfigContent, jobExecParam);

        assertEquals(expected, actual);
    }

    @Test
    public void testReplaceJobConfigPlaceholdersUsed() {
        String jobConfigContent =
                "job.mode=${jobModeParam:BATCH}\ncheckpoint.interval=30\njob.name=${jobNameParam:DefaultJob}";
        Map<String, String> paramValues = new HashMap<>();
        paramValues.put("jobModeParam", "STREAMING");
        JobExecParam jobExecParam = getJobExecParam(paramValues);

        String expected = "job.mode=STREAMING\ncheckpoint.interval=30\njob.name=DefaultJob";
        String actual = JobUtils.replaceJobConfigPlaceholders(jobConfigContent, jobExecParam);

        assertEquals(expected, actual);
    }

    @Test
    public void testReplaceJobConfigPlaceholders_NoDefaultValueThrowsException() {
        String jobConfigContent =
                "job.mode=${jobModeParam}\ncheckpoint.interval=30\njob.name=${jobNameParam}";
        Map<String, String> paramValues = new HashMap<>();
        paramValues.put("jobModeParam", "STREAMING");
        JobExecParam jobExecParam = getJobExecParam(paramValues);

        assertThrows(
                SeatunnelException.class,
                () -> JobUtils.replaceJobConfigPlaceholders(jobConfigContent, jobExecParam));
    }

    @Test
    public void testReplaceJobConfigPlaceholders_NoJobConfigPlaceholders() {
        String jobConfigContent = "job.mode=STREAMING\ncheckpoint.interval=30\njob.name=newJob";
        Map<String, String> paramValues = new HashMap<>();
        JobExecParam jobExecParam = getJobExecParam(paramValues);

        String expected = "job.mode=STREAMING\ncheckpoint.interval=30\njob.name=newJob";
        String actual = JobUtils.replaceJobConfigPlaceholders(jobConfigContent, jobExecParam);

        assertEquals(expected, actual);
    }

    @Test
    public void testParseConfigWithPlaceHolders() {
        String transformConfig =
                "{\"log.print.data\":\"true\",\"log.print.delay.ms\":\"${logPrintDelayMs:100}\"}";
        Config config = ConfigFactory.parseString(transformConfig);
        assertNotNull(config);
    }

    private JobExecParam getJobExecParam(Map<String, String> paramValues) {
        JobExecParam jobExecParam = new JobExecParam();
        jobExecParam.setPlaceholderValues(paramValues);
        return jobExecParam;
    }
}
