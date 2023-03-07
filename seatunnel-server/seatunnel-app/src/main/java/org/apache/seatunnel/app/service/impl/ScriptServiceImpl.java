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

package org.apache.seatunnel.app.service.impl;

import static org.apache.seatunnel.server.common.SeatunnelErrorEnum.NO_SUCH_SCRIPT;
import static com.google.common.base.Preconditions.checkState;

import org.apache.seatunnel.app.common.ScriptParamStatusEnum;
import org.apache.seatunnel.app.common.ScriptStatusEnum;
import org.apache.seatunnel.app.dal.dao.IScriptDao;
import org.apache.seatunnel.app.dal.dao.IScriptParamDao;
import org.apache.seatunnel.app.dal.dao.IUserDao;
import org.apache.seatunnel.app.dal.entity.Script;
import org.apache.seatunnel.app.dal.entity.ScriptParam;
import org.apache.seatunnel.app.domain.dto.job.PushScriptDto;
import org.apache.seatunnel.app.domain.dto.script.CheckScriptDuplicateDto;
import org.apache.seatunnel.app.domain.dto.script.CreateScriptDto;
import org.apache.seatunnel.app.domain.dto.script.ListScriptsDto;
import org.apache.seatunnel.app.domain.dto.script.UpdateScriptContentDto;
import org.apache.seatunnel.app.domain.dto.script.UpdateScriptParamDto;
import org.apache.seatunnel.app.domain.request.script.CreateScriptReq;
import org.apache.seatunnel.app.domain.request.script.PublishScriptReq;
import org.apache.seatunnel.app.domain.request.script.ScriptListReq;
import org.apache.seatunnel.app.domain.request.script.UpdateScriptContentReq;
import org.apache.seatunnel.app.domain.request.script.UpdateScriptParamReq;
import org.apache.seatunnel.app.domain.response.PageInfo;
import org.apache.seatunnel.app.domain.response.script.CreateScriptRes;
import org.apache.seatunnel.app.domain.response.script.ScriptFullInfoRes;
import org.apache.seatunnel.app.domain.response.script.ScriptParamRes;
import org.apache.seatunnel.app.domain.response.script.ScriptSimpleInfoRes;
import org.apache.seatunnel.app.service.IScriptService;
import org.apache.seatunnel.app.service.ITaskService;
import org.apache.seatunnel.app.utils.Md5Utils;
import org.apache.seatunnel.scheduler.dolphinscheduler.impl.InstanceServiceImpl;
import org.apache.seatunnel.server.common.PageData;

import com.google.common.base.Strings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ScriptServiceImpl implements IScriptService {

    @Resource
    private IScriptDao scriptDaoImpl;

    @Resource
    private IScriptParamDao scriptParamDaoImpl;

    @Resource
    private InstanceServiceImpl instanceService;

    @Resource
    private IUserDao userDaoImpl;

    @Resource
    private ITaskService iTaskService;

    @Override
    public CreateScriptRes createScript(CreateScriptReq createScriptReq) {
        // 1. check script name.
        checkDuplicate(createScriptReq.getName(), createScriptReq.getCreatorId());
        // 2. create  script
        int scriptId = translate(createScriptReq.getName(), createScriptReq.getCreatorId(), createScriptReq.getCreatorId(), createScriptReq.getType(), createScriptReq.getContent());

        final CreateScriptRes res = new CreateScriptRes();
        res.setId(scriptId);
        return res;
    }

    private int translate(String name, Integer creatorId, Integer menderId, Byte type, String content) {
        final CreateScriptDto dto = CreateScriptDto.builder()
                .name(name)
                .menderId(creatorId)
                .creatorId(menderId)
                .type(type)
                .status((byte) ScriptStatusEnum.UNPUBLISHED.getCode())
                .content(content)
                .build();
        return scriptDaoImpl.createScript(dto);
    }

    private void checkDuplicate(String name, Integer creatorId) {
        final CheckScriptDuplicateDto dto = CheckScriptDuplicateDto.builder()
                .creatorId(creatorId)
                .name(name)
                .build();
        scriptDaoImpl.checkScriptDuplicate(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScriptContent(UpdateScriptContentReq updateScriptContentReq) {
        // 1. check content md5 is consistent
        final String content = updateScriptContentReq.getContent();
        final String contentMd5 = Strings.isNullOrEmpty(content) ? "" : Md5Utils.toMd5(content);

        final boolean needSave = checkIfNeedSave(updateScriptContentReq.getScriptId(), contentMd5);

        if (needSave) {
            final UpdateScriptContentDto dto = UpdateScriptContentDto.builder()
                    .id(updateScriptContentReq.getScriptId())
                    .content(content)
                    .contentMd5(contentMd5)
                    .menderId(updateScriptContentReq.getMenderId())
                    .build();
            scriptDaoImpl.updateScriptContent(dto);
        }
    }

    private boolean checkIfNeedSave(int id, String newContentMd5) {
        Script script = scriptDaoImpl.getScript(id);
        checkState(Objects.nonNull(script) && (int) script.getStatus() != ScriptStatusEnum.DELETED.getCode(), NO_SUCH_SCRIPT.getTemplate());

        final String oldContentMd5 = Strings.isNullOrEmpty(script.getContentMd5()) ? "" : script.getContentMd5();
        return !newContentMd5.equals(oldContentMd5);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        // no need check script if exist.
        scriptDaoImpl.deleteScript(id);
    }

    @Override
    public PageInfo<ScriptSimpleInfoRes> list(ScriptListReq scriptListReq) {

        final ListScriptsDto dto = ListScriptsDto.builder()
                .name(scriptListReq.getName())
                .build();

        PageData<Script> scriptPageData = scriptDaoImpl.list(dto, scriptListReq.getRealPageNo(), scriptListReq.getPageSize());
        final List<ScriptSimpleInfoRes> data = scriptPageData.getData().stream().map(this::translate).collect(Collectors.toList());

        final PageInfo<ScriptSimpleInfoRes> pageInfo = new PageInfo<>();
        pageInfo.setPageNo(scriptListReq.getPageNo());
        pageInfo.setPageSize(scriptListReq.getPageSize());
        pageInfo.setTotalCount(scriptPageData.getTotalCount());
        pageInfo.setData(data);
        return pageInfo;
    }

    @Override
    public String fetchScriptContent(Integer id) {
        Script script = scriptDaoImpl.getScript(id);
        checkState(Objects.nonNull(script), NO_SUCH_SCRIPT.getTemplate());
        return script.getContent();
    }

    @Override
    public List<ScriptParamRes> fetchScriptParam(Integer id) {
        List<ScriptParam> scriptParamRes = scriptParamDaoImpl.getParamsByScriptId(id);
        if (CollectionUtils.isEmpty(scriptParamRes)) {
            return Collections.emptyList();
        }
        return scriptParamRes.stream().map(this::translate).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScriptParam(UpdateScriptParamReq updateScriptParamReq) {
        // 1. delete all old params first.
        // 2. save new params. (check params correctness)
        scriptParamDaoImpl.updateStatusByScriptId(updateScriptParamReq.getScriptId(), ScriptParamStatusEnum.DELETED.getCode());

        UpdateScriptParamDto dto = UpdateScriptParamDto.builder()
                .scriptId(updateScriptParamReq.getScriptId())
                .params(updateScriptParamReq.getParams())
                .build();

        scriptParamDaoImpl.batchInsert(dto);
    }

    @Override
    public void publishScript(PublishScriptReq req) {
        final PushScriptDto dto = PushScriptDto.builder()
                .scriptId(req.getScriptId())
                .userId(req.getOperatorId())
                .build();
        iTaskService.pushScriptToScheduler(dto);
    }

    @Override
    public ScriptFullInfoRes detail(Integer scriptId) {
        final Script script = scriptDaoImpl.getScript(scriptId);

        checkState(Objects.nonNull(script), NO_SUCH_SCRIPT.getTemplate());
        return translateToFull(script);
    }

    private ScriptParamRes translate(ScriptParam scriptParam) {
        final ScriptParamRes res = new ScriptParamRes();
        res.setId(scriptParam.getId());
        res.setKey(scriptParam.getKey());
        res.setValue(scriptParam.getValue());
        return res;
    }

    private ScriptSimpleInfoRes translate(Script script) {
        final ScriptSimpleInfoRes res = new ScriptSimpleInfoRes();
        res.setId(script.getId());
        res.setName(script.getName());
        res.setStatus(ScriptStatusEnum.parse(script.getStatus()));
        res.setType(script.getType());
        res.setCreatorId(script.getCreatorId());
        res.setMenderId(script.getMenderId());
        res.setCreateTime(script.getCreateTime());
        res.setUpdateTime(script.getUpdateTime());
        return res;
    }

    private ScriptFullInfoRes translateToFull(Script script) {
        final ScriptFullInfoRes res = new ScriptFullInfoRes();
        res.setId(script.getId());
        res.setName(script.getName());
        res.setStatus(ScriptStatusEnum.parse(script.getStatus()));
        res.setType(script.getType());
        res.setCreatorId(script.getCreatorId());
        res.setMenderId(script.getMenderId());
        res.setCreateTime(script.getCreateTime());
        res.setUpdateTime(script.getUpdateTime());
        res.setContent(script.getContent());
        return res;
    }
}
