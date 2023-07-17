/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.app.dal.dao.impl;

import org.apache.seatunnel.app.dal.dao.IScriptJobApplyDao;
import org.apache.seatunnel.app.dal.entity.JobDefine;
import org.apache.seatunnel.app.dal.entity.ScriptJobApply;
import org.apache.seatunnel.app.dal.mapper.ScriptJobApplyMapper;
import org.apache.seatunnel.app.domain.dto.job.ScriptJobApplyDto;

import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

import java.util.List;
import java.util.Objects;

@Repository
public class ScriptJobApplyDaoImpl implements IScriptJobApplyDao {

    @Resource private ScriptJobApplyMapper scriptJobApplyMapper;

    @Override
    public void insertOrUpdate(ScriptJobApplyDto dto) {
        ScriptJobApply apply = scriptJobApplyMapper.selectByScriptId(dto.getScriptId());
        if (Objects.isNull(apply)) {
            apply = new ScriptJobApply();
            apply.setScriptId(dto.getScriptId());
            apply.setJobId(dto.getJobId());
            apply.setOperatorId(dto.getUserId());
            apply.setSchedulerConfigId(dto.getSchedulerConfigId());
            scriptJobApplyMapper.insert(apply);
        } else {
            apply.setJobId(dto.getJobId());
            apply.setOperatorId(dto.getUserId());
            apply.setSchedulerConfigId(dto.getSchedulerConfigId());
            scriptJobApplyMapper.update(apply);
        }
    }

    @Override
    public ScriptJobApply getByScriptId(Integer id) {
        return scriptJobApplyMapper.selectByScriptId(id);
    }

    @Override
    public List<JobDefine> selectJobDefineByJobIds(List<Long> jobIds) {
        return scriptJobApplyMapper.selectJobDefineByJobIds(jobIds);
    }

    @Override
    public ScriptJobApply getByJobId(long jobId) {
        return scriptJobApplyMapper.selectByJobId(jobId);
    }
}
