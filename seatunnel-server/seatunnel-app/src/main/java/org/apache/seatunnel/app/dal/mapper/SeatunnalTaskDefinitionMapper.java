package org.apache.seatunnel.app.dal.mapper;

import org.apache.seatunnel.app.dal.entity.TaskDefinition;
import org.apache.seatunnel.app.dal.entity.TaskDefinitionExpand;
import org.apache.seatunnel.app.dal.entity.TaskMainInfo;

import org.apache.ibatis.annotations.Param;

import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Set;

@Primary
public interface SeatunnalTaskDefinitionMapper extends TaskDefinitionMapper {

    /**
     * Query all specific task type from single process definition
     *
     * @param processCode process definition code
     * @param taskType Task type of this process definition code
     * @return List of ProcessTaskRelationMapper
     */
    List<TaskDefinitionExpand> queryProcessTaskType(
            @Param("processCode") Long processCode, @Param("taskType") String taskType);

    /**
     * query all task definition list
     *
     * @param taskCodesList taskCodesList
     * @return task definition list
     */
    List<TaskDefinition> queryAllTaskProcessDefinition(
            @Param("taskCodesList") List<Long> taskCodesList);

    /**
     * query task definition by project codes and task types
     *
     * @param projectCodes
     * @param definitionCodes
     * @param taskTypes
     * @return
     */
    List<TaskDefinition> queryTaskDefinitionByProjectCodesAndTaskTypes(
            @Param("projectCodes") Set<Long> projectCodes,
            @Param("definitionCodes") Set<Long> definitionCodes,
            @Param("taskTypes") List<String> taskTypes);

    List<TaskMainInfo> queryTaskDefinitionBySubprocessTask(
            @Param("processDefinitionCode") Long processDefinitionCode);

    List<TaskMainInfo> queryTaskDefinitionByDependentTaskWithTaskCode(
            @Param("taskCode") Long taskCode);

    List<TaskMainInfo> queryTaskDefinitionByDependentTaskWithProcessCode(
            @Param("processDefinitionCode") Long processDefinitionCode);
}
