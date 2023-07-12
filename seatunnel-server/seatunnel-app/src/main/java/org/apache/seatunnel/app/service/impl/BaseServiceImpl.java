package org.apache.seatunnel.app.service.impl;

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.app.common.Status;
import org.apache.seatunnel.app.service.BaseService;

import org.springframework.stereotype.Service;

import java.text.MessageFormat;

@Service
public class BaseServiceImpl implements BaseService {
    @Override
    public void putMsg(Result result, Status status, Object... statusParams) {
        result.setCode(status.getCode());
        if (statusParams != null && statusParams.length > 0) {
            result.setMsg(MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.setMsg(status.getMsg());
        }
    }
}
