package org.apache.seatunnel.app.security.authentication.strategy;

import org.apache.seatunnel.app.dal.entity.User;
import org.apache.seatunnel.app.domain.request.user.UserLoginReq;

public interface IAuthenticationStrategy {
    User authenticate(UserLoginReq req);
}
