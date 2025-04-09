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
package org.apache.seatunnel.app.security;

import org.apache.seatunnel.app.config.AsyncConfig;
import org.apache.seatunnel.app.dal.entity.User;
import org.apache.seatunnel.app.utils.ServletUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test the passing of user context in asynchronous tasks. This test focuses only on user context
 * functionality without requiring a full Spring context.
 */
@Slf4j
public class AsyncUserContextTest {

    private ThreadPoolTaskExecutor taskExecutor;

    @BeforeEach
    public void setup() {
        // Create a standalone thread pool executor for testing
        taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setMaxPoolSize(20);
        taskExecutor.setQueueCapacity(100);
        taskExecutor.setThreadNamePrefix("TestAsync-");
        taskExecutor.setTaskDecorator(new AsyncConfig.ContextCopyingDecorator());
        taskExecutor.initialize();
    }

    @AfterEach
    public void cleanup() {
        if (taskExecutor != null) {
            taskExecutor.shutdown();
        }
        // Clear any remaining user context
        UserContextHolder.clear();
    }

    @Test
    public void testMultipleUsersAsync() throws Exception {
        log.info("Starting multiple users concurrent test...");
        int userCount = 10;
        CyclicBarrier barrier = new CyclicBarrier(userCount);
        CountDownLatch latch = new CountDownLatch(userCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < userCount; i++) {
            final int userId = i;

            User user = new User();
            user.setId(userId);
            user.setUsername("user" + userId);

            log.info(
                    "Setting main thread user context: userId={}, username={}",
                    userId,
                    user.getUsername());
            UserContextHolder.setUserContext(getUserContext(user));

            CompletableFuture<Void> future =
                    CompletableFuture.runAsync(
                            () -> {
                                try {
                                    log.info(
                                            "Async task waiting to start: userId={}, threadName={}",
                                            userId,
                                            Thread.currentThread().getName());
                                    barrier.await();
                                    log.info(
                                            "Async task started: userId={}, threadName={}",
                                            userId,
                                            Thread.currentThread().getName());

                                    User asyncUser = ServletUtils.getCurrentUser();
                                    log.info(
                                            "Async task got user info: userId={}, username={}, threadName={}",
                                            asyncUser.getId(),
                                            asyncUser.getUsername(),
                                            Thread.currentThread().getName());

                                    assertNotNull(asyncUser, "User info should not be null");
                                    assertEquals(userId, asyncUser.getId(), "User ID mismatch");
                                    assertEquals(
                                            "user" + userId,
                                            asyncUser.getUsername(),
                                            "Username mismatch");

                                    Thread.sleep(100);
                                    log.info(
                                            "Async task completed: userId={}, threadName={}",
                                            userId,
                                            Thread.currentThread().getName());

                                } catch (Exception e) {
                                    log.error("Async task execution failed: userId=" + userId, e);
                                    throw new RuntimeException(e);
                                } finally {
                                    latch.countDown();
                                }
                            },
                            taskExecutor);

            futures.add(future);
        }

        log.info("Waiting for all async tasks to complete...");
        latch.await();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            log.info("All async tasks completed successfully");
        } catch (Exception e) {
            log.error("Some async tasks failed", e);
            throw e;
        }
    }

    private UserContext getUserContext(User user) {
        UserContext userContext = new UserContext();
        userContext.setUser(user);
        return userContext;
    }

    @Test
    public void testNestedAsyncCalls() throws Exception {
        log.info("Starting nested async calls test...");

        User user = new User();
        user.setId(1);
        user.setUsername("testUser");

        log.info(
                "Setting main thread user context: userId={}, username={}",
                user.getId(),
                user.getUsername());
        UserContextHolder.setUserContext(getUserContext(user));

        CompletableFuture<Void> future =
                CompletableFuture.runAsync(
                        () -> {
                            log.info(
                                    "First level async call started: threadName={}",
                                    Thread.currentThread().getName());
                            User firstLevelUser = ServletUtils.getCurrentUser();
                            log.info(
                                    "First level async call got user info: userId={}, username={}",
                                    firstLevelUser.getId(),
                                    firstLevelUser.getUsername());
                            assertEquals(1, firstLevelUser.getId());

                            CompletableFuture.runAsync(
                                            () -> {
                                                log.info(
                                                        "Second level async call started: threadName={}",
                                                        Thread.currentThread().getName());
                                                User secondLevelUser =
                                                        ServletUtils.getCurrentUser();
                                                log.info(
                                                        "Second level async call got user info: userId={}, username={}",
                                                        secondLevelUser.getId(),
                                                        secondLevelUser.getUsername());
                                                assertEquals(1, secondLevelUser.getId());
                                            },
                                            taskExecutor)
                                    .join();

                            log.info("Nested async calls completed");
                        },
                        taskExecutor);

        future.join();
        log.info("Nested async calls test completed");
    }

    @Test
    public void testUserContextIsolation() throws Exception {
        log.info("Starting user context isolation test...");
        CountDownLatch latch = new CountDownLatch(2);

        // First user
        User user1 = new User();
        user1.setId(1);
        UserContextHolder.setUserContext(getUserContext(user1));
        log.info("Setting first user context: userId={}", user1.getId());

        // Capture current user to avoid context loss during thread switching
        User capturedUser1 = user1;
        CompletableFuture<Integer> future1 =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                log.info(
                                        "First user's async task started: threadName={}",
                                        Thread.currentThread().getName());
                                Thread.sleep(1000);
                                UserContextHolder.setUserContext(getUserContext(capturedUser1));
                                User currentUser = ServletUtils.getCurrentUser();
                                log.info(
                                        "First user's async task got user: userId={}",
                                        currentUser.getId());
                                return currentUser.getId();
                            } catch (Exception e) {
                                log.error("First user's async task failed", e);
                                throw new RuntimeException(e);
                            } finally {
                                UserContextHolder.clear();
                                latch.countDown();
                            }
                        },
                        taskExecutor);

        // Second user
        User user2 = new User();
        user2.setId(2);
        UserContextHolder.setUserContext(getUserContext(user2));
        log.info("Setting second user context: userId={}", user2.getId());

        // Capture current user to avoid context loss during thread switching
        User capturedUser2 = user2;
        CompletableFuture<Integer> future2 =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                log.info(
                                        "Second user's async task started: threadName={}",
                                        Thread.currentThread().getName());
                                UserContextHolder.setUserContext(getUserContext(capturedUser2));
                                User currentUser = ServletUtils.getCurrentUser();
                                log.info(
                                        "Second user's async task got user: userId={}",
                                        currentUser.getId());
                                return currentUser.getId();
                            } catch (Exception e) {
                                log.error("Second user's async task failed", e);
                                throw new RuntimeException(e);
                            } finally {
                                UserContextHolder.clear();
                                latch.countDown();
                            }
                        },
                        taskExecutor);

        log.info("Waiting for async tasks to complete...");
        latch.await();

        int result1 = future1.get();
        int result2 = future2.get();
        log.info("Verification results: user1={}, user2={}", result1, result2);
        assertEquals(1, result1);
        assertEquals(2, result2);
        log.info("User context isolation test completed");

        UserContextHolder.clear();
    }
}
