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
package org.apache.seatunnel.app.interceptor;

import org.apache.seatunnel.app.security.UserContext;
import org.apache.seatunnel.app.security.UserContextHolder;

import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.apache.seatunnel.app.common.Constants.SESSION_USER_CONTEXT;

/**
 * Interceptor for managing user context in web requests. This interceptor sets up and cleans up the
 * user context for each request, ensuring proper user information availability throughout request
 * processing.
 */
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {

    /**
     * Sets up the user context before request processing. Retrieves user information from request
     * attributes and stores it in ThreadLocal.
     *
     * @param request Current HTTP request
     * @param response Current HTTP response
     * @param handler Chosen handler to execute
     * @return true to continue processing, false to stop
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        UserContext userContext = (UserContext) request.getAttribute(SESSION_USER_CONTEXT);
        if (userContext != null) {
            log.debug("Setting user context for user: {}", userContext.getUser().getUsername());
            UserContextHolder.setUserContext(userContext);
        } else {
            log.warn("No user context found in request attributes");
        }
        return true;
    }

    /**
     * Cleans up the user context after request completion. Ensures ThreadLocal resources are
     * properly released.
     *
     * @param request Current HTTP request
     * @param response Current HTTP response
     * @param handler Handler that was executed
     * @param ex Exception that was thrown during handler execution
     */
    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        log.debug("Clearing user context");
        UserContextHolder.clear();
    }
}
