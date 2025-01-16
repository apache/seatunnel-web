package org.apache.seatunnel.app.security.authentication.strategy.impl;

import org.apache.seatunnel.app.common.Constants;
import org.apache.seatunnel.app.config.SeatunnelLdapAuthenticationProvider;
import org.apache.seatunnel.app.dal.dao.IUserDao;
import org.apache.seatunnel.app.dal.entity.User;
import org.apache.seatunnel.app.domain.dto.user.UpdateUserDto;
import org.apache.seatunnel.app.domain.request.user.UserLoginReq;
import org.apache.seatunnel.app.security.authentication.strategy.IAuthenticationStrategy;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import static org.apache.seatunnel.server.common.SeatunnelErrorEnum.USERNAME_PASSWORD_NO_MATCHED;

@Component
public class LDAPAuthenticationStrategy implements IAuthenticationStrategy {

    @Autowired private IUserDao userDaoImpl;

    @Autowired private SeatunnelLdapAuthenticationProvider seatunnelLdapAuthenticationProvider;

    @Override
    public User authenticate(UserLoginReq req) {
        String username = req.getUsername();
        String password = req.getPassword();
        Authentication authenticationRequest =
                new UsernamePasswordAuthenticationToken(username, password);
        try {
            seatunnelLdapAuthenticationProvider.authenticate(authenticationRequest);
        } catch (AuthenticationException ex) {
            throw new SeatunnelException(USERNAME_PASSWORD_NO_MATCHED);
        }

        if (userDaoImpl.getByName(username) == null) {
            // 2. add a new user.
            final UpdateUserDto dto =
                    UpdateUserDto.builder()
                            .id(null)
                            .username(username)
                            .password("")
                            .authProvider(Constants.AUTHENTICATION_PROVIDER_LDAP)
                            .build();
            userDaoImpl.add(dto);
        }
        return userDaoImpl.getByName(username);
    }
}
