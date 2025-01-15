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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SimpleDirContextAuthenticationStrategy;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LdapAuthenticationBuilder {

    @Value("${spring.ldap.url}")
    private String ldapUrl;

    @Value("${spring.ldap.search.base}")
    private String ldapSearchBase;

    @Value("${spring.ldap.search.domain}")
    private String ldapSearchDomain;

    public LdapAuthenticationProvider buildLdapAuthenticationProvider(
            Authentication authentication) {

        LdapContextSource ldapContextSource = new DefaultSpringSecurityContextSource(ldapUrl);
        String ldapBindUser = authentication.getName();
        ldapContextSource.setUserDn(
                new StringBuilder()
                        .append(ldapBindUser)
                        .append("@")
                        .append(ldapSearchDomain)
                        .toString());
        ldapContextSource.setPassword(authentication.getCredentials().toString());
        ldapContextSource.setCacheEnvironmentProperties(false);
        ldapContextSource.setAuthenticationStrategy(new SimpleDirContextAuthenticationStrategy());
        ldapContextSource.afterPropertiesSet();

        String ldapAuthID =
                ldapBindUser.toLowerCase().endsWith("@" + ldapSearchDomain)
                        ? ldapBindUser
                        : ldapBindUser + "@" + ldapSearchDomain;
        String searchFilter = "(userPrincipalName=" + ldapAuthID + ")";
        FilterBasedLdapUserSearch userSearch =
                new FilterBasedLdapUserSearch(ldapSearchBase, searchFilter, ldapContextSource);
        userSearch.setSearchSubtree(true);
        BindAuthenticator authenticator = new BindAuthenticator(ldapContextSource);
        authenticator.setUserSearch(userSearch);
        LdapAuthenticationProvider ldapAuthenticationProvider =
                new LdapAuthenticationProvider(authenticator);
        return ldapAuthenticationProvider;
    }
}
