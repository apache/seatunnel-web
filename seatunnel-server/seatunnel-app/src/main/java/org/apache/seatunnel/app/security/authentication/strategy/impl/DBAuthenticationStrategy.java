package org.apache.seatunnel.app.security.authentication.strategy.impl;

import org.apache.seatunnel.app.common.Constants;
import org.apache.seatunnel.app.dal.dao.IUserDao;
import org.apache.seatunnel.app.dal.entity.User;
import org.apache.seatunnel.app.domain.request.user.UserLoginReq;
import org.apache.seatunnel.app.security.authentication.strategy.IAuthenticationStrategy;
import org.apache.seatunnel.app.utils.PasswordUtils;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.apache.seatunnel.server.common.SeatunnelErrorEnum.USERNAME_PASSWORD_NO_MATCHED;

@Component
public class DBAuthenticationStrategy implements IAuthenticationStrategy {

    @Autowired private IUserDao userDaoImpl;

    @Value("${user.default.passwordSalt:seatunnel}")
    private String defaultSalt;

    @Override
    public User authenticate(UserLoginReq req) {
        final String password = PasswordUtils.encryptWithSalt(defaultSalt, req.getPassword());
        final User user =
                userDaoImpl.checkPassword(
                        req.getUsername(), password, Constants.AUTHENTICATION_PROVIDER_DB);
        if (Objects.isNull(user)) {
            throw new SeatunnelException(USERNAME_PASSWORD_NO_MATCHED);
        }
        return user;
    }
}
