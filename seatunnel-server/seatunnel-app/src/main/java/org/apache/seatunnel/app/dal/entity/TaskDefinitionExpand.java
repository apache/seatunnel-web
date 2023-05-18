package org.apache.seatunnel.app.dal.entity;

import org.apache.seatunnel.app.parameters.DependentParameters;
import org.apache.seatunnel.app.parameters.SubProcessParameters;
import org.apache.seatunnel.app.utils.JSONUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.apache.seatunnel.app.common.Constants.CMD_PARAM_SUB_PROCESS_DEFINE_CODE;

public class TaskDefinitionExpand extends TaskDefinition {

    public SubProcessParameters getSubProcessParameters() {
        String parameter = super.getTaskParams();
        ObjectNode parameterJson = JSONUtils.parseObject(parameter);
        if (parameterJson.get(CMD_PARAM_SUB_PROCESS_DEFINE_CODE) != null) {
            return JSONUtils.parseObject(parameter, SubProcessParameters.class);
        }
        return null;
    }

    public DependentParameters getDependentParameters() {
        return JSONUtils.parseObject(super.getDependence(), DependentParameters.class);
    }
}
