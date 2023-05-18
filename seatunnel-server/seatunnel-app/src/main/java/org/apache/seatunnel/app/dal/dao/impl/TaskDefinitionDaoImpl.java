package org.apache.seatunnel.app.dal.dao.impl;

import org.apache.seatunnel.app.dal.dao.TaskDefinitionDao;
import org.apache.seatunnel.app.dal.entity.ProcessTaskRelation;
import org.apache.seatunnel.app.dal.entity.TaskDefinition;
import org.apache.seatunnel.app.dal.entity.TaskMainInfo;
import org.apache.seatunnel.app.dal.mapper.ProcessTaskRelationMapper;
import org.apache.seatunnel.app.dal.mapper.TaskDefinitionMapper;

import org.apache.commons.collections4.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class TaskDefinitionDaoImpl implements TaskDefinitionDao {

    @Autowired private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired private ProcessTaskRelationMapper processTaskRelationMapper;

    @Override
    public List<TaskMainInfo> queryByDataSourceId(Long dataSourceId) {
        return processTaskRelationMapper.queryByDataSourceId(dataSourceId);
    }

    @Override
    public List<TaskDefinition> queryTaskDefinitions(Collection<Long> taskCodes) {
        if (CollectionUtils.isEmpty(taskCodes)) {
            return Collections.emptyList();
        }
        return taskDefinitionMapper.queryByCodeList(taskCodes);
    }

    @Override
    public List<TaskDefinition> queryByWorkflowDefinitionCodeAndVersion(
            Long workflowDefinitionCode, Integer workflowDefinitionVersion) {
        List<ProcessTaskRelation> processTaskRelations =
                processTaskRelationMapper.queryProcessTaskRelationsByProcessDefinitionCode(
                        workflowDefinitionCode, workflowDefinitionVersion);
        Set<Long> taskCodes = new HashSet<>();
        processTaskRelations.forEach(
                processTaskRelation -> {
                    taskCodes.add(processTaskRelation.getPreTaskCode());
                    taskCodes.add(processTaskRelation.getPostTaskCode());
                });
        if (CollectionUtils.isEmpty(taskCodes)) {
            return Collections.emptyList();
        }
        return taskDefinitionMapper.queryByCodeList(taskCodes);
    }
}
