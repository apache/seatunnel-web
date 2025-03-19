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
package org.apache.seatunnel.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SeatunnelLdapAuthenticationProvider implements AuthenticationProvider {

    @Autowired LdapAuthenticationBuilder ldapAuthenticationBuilder;

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        try {
            LdapAuthenticationProvider ldapAuthenticationProvider =
                    ldapAuthenticationBuilder.buildLdapAuthenticationProvider(authentication);
            return ldapAuthenticationProvider.authenticate(authentication);
        } catch (BadCredentialsException ex) {
            log.error("Invalid credentials for user : {}", authentication.getName());
            throw new BadCredentialsException("Invalid credentials");
        } catch (Exception ex) {
            log.error(
                    "Error while authenticating user : {}, reason :{}",
                    authentication.getName(),
                    ex.getMessage());
            throw new AuthenticationException(ex.getMessage().toString()) {
                @Override
                public String getMessage() {
                    return super.getMessage();
                }
            };
        }
    }
}
