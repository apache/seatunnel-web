package org.apache.seatunnel.app.dal.dao;

import org.apache.seatunnel.app.dal.entity.TaskDefinition;
import org.apache.seatunnel.app.dal.entity.TaskMainInfo;

import java.util.Collection;
import java.util.List;

public interface TaskDefinitionDao {

    List<TaskMainInfo> queryByDataSourceId(Long dataSourceId);

    List<TaskDefinition> queryTaskDefinitions(Collection<Long> taskCodes);

    List<TaskDefinition> queryByWorkflowDefinitionCodeAndVersion(
            Long workflowDefinitionCode, Integer workflowDefinitionVersion);
}
