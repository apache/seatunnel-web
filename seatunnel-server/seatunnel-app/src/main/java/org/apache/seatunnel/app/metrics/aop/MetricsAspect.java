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

package org.apache.seatunnel.app.metrics.aop;

import org.apache.seatunnel.app.metrics.annotations.Counted;
import org.apache.seatunnel.app.metrics.annotations.Timed;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@ConditionalOnProperty(name = "seatunnel-web.telemetry.enabled", havingValue = "true")
public class MetricsAspect {

    private final CollectorRegistry collectorRegistry;

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<String, Summary> summaries = new ConcurrentHashMap<>();

    @Autowired
    public MetricsAspect(CollectorRegistry collectorRegistry) {
        this.collectorRegistry = collectorRegistry;
    }

    @Before("@annotation(counted)")
    public void beforeCounted(Counted counted) {
        String name = counted.name();
        String help = counted.help();

        Counter counter =
                counters.computeIfAbsent(
                        name,
                        key ->
                                Counter.build()
                                        .name(key)
                                        .help(help.isEmpty() ? "Counter for " + key : help)
                                        .register(collectorRegistry));

        counter.inc();
    }

    @Around("@annotation(timed)")
    public Object aroundTimed(ProceedingJoinPoint joinPoint, Timed timed) throws Throwable {
        String name = timed.name();
        String help = timed.help();

        Summary summary =
                summaries.computeIfAbsent(
                        name,
                        key ->
                                Summary.build()
                                        .name(key)
                                        .help(help.isEmpty() ? "Summary for " + key : help)
                                        .register(collectorRegistry));

        try (Summary.Timer ignored = summary.startTimer()) {
            return joinPoint.proceed();
        }
    }
}
