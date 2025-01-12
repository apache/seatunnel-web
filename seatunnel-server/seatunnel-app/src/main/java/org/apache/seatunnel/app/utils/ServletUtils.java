package org.apache.seatunnel.app.utils;

import org.apache.seatunnel.app.dal.entity.User;
import org.apache.seatunnel.app.security.UserContext;

public class ServletUtils {

    public static User getCurrentUser() {
        return UserContext.getUser();
    }

    public static Integer getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
