package org.apache.seatunnel.app.service;

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.common.Status;

public interface BaseService {

    void putMsg(Result result, Status status, Object... statusParams);
}
