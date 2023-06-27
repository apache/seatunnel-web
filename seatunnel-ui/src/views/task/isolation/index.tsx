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
import { defineComponent, ref } from 'vue'
import {
  NButton,
  NSpace,
  NInput,
  NIcon,
  NDataTable,
  NPagination,
  NPopconfirm
} from 'naive-ui'
import { SearchOutlined, ReloadOutlined } from '@vicons/antd'
import Card from '@/components/card'
import DetailModal from './detail-modal'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { useTable } from './use-table'
import { useColumns } from './use-columns'
import ProjectSelector from '../../components/projectSelector'
import { useProjectStore } from '@/store/project'

const TaskIsolation = defineComponent({
  name: 'task-isolation',
  setup() {
    const { t, locale } = useI18n()
    const route = useRoute()
    const {
      state,
      onUpdateList,
      onSearch,
      onChangePage,
      onChangePageSize,
      onCallback,
      onCancleIsolation
    } = useTable()
    const { columns } = useColumns(onCallback)
    const showDetailModal = ref(false)

    const onReset = () => {
      state.taskName = ''
      state.workflowInstanceName = ''
      state.projectCode = useProjectStore().getCurrentProject
    }

    const onCreate = () => {
      showDetailModal.value = true
    }
    const handleCancleIsolation = () => {
      onCancleIsolation()
      onSearch()
    }
    const onDownloadTemplate = () => {
      window.location.href = `/dolphinscheduler/${
        locale.value === 'zh_CN'
          ? 'isolation-task-excel-template-cn'
          : 'isolation-task-excel-template-en'
      }.xlsx`
    }

    const handleKeyup = (event: KeyboardEvent) => {
      if (event.key === 'Enter') {
        onSearch()
      }
    }
    const getProjectCodeList = (codes: any) => {
      if (!codes) {
        state.projectCode = useProjectStore().getGolbalProject
      } else {
        state.projectCode = [codes]
      }
    }

    return () => (
      <NSpace vertical>
        <Card title=''>
          {{
            default: () => (
              <NSpace justify='space-between' itemStyle={{ flexGrow: 1 }}>
                <NSpace align='center'>
                  <NButton
                    onClick={onCreate}
                    type='primary'
                    v-permission='project:isolation-task:parse'
                  >
                    {t('project.isolation.upload_isolation_tasks')}
                  </NButton>
                  <NButton
                    text
                    tag='a'
                    type='primary'
                    onClick={onDownloadTemplate}
                  >
                    {t('project.isolation.download_template')}
                  </NButton>
                </NSpace>
                <NSpace justify='end' wrap={false}>
                  {state.globalProject && (
                    <ProjectSelector
                      initCode={
                        state.projectCode.length == 1
                          ? state.projectCode[0]
                          : null
                      }
                      onGetprojectList={getProjectCodeList}
                    />
                  )}
                  <NInput
                    v-model={[state.taskName, 'value']}
                    placeholder={`${t('project.isolation.task_name')}`}
                    onKeyup={handleKeyup}
                  />
                  <NInput
                    v-model={[state.workflowInstanceName, 'value']}
                    placeholder={`${t(
                      'project.isolation.workflow_instance_name'
                    )}`}
                    onKeyup={handleKeyup}
                  />
                  <NButton onClick={onReset}>
                    <NIcon>
                      <ReloadOutlined />
                    </NIcon>
                  </NButton>
                  <NButton type='primary' onClick={onSearch}>
                    <NIcon>
                      <SearchOutlined />
                    </NIcon>
                  </NButton>
                </NSpace>
              </NSpace>
            )
          }}
        </Card>
        <Card title={t('menu.task_isolation')}>
          {{
            'header-extra': () => (
              <NSpace justify='space-between'>
                <NButton
                  tag='div'
                  type='primary'
                  disabled={state.checkedRowKeys.length <= 0}
                >
                  <NPopconfirm
                    disabled={state.checkedRowKeys.length <= 0}
                    onPositiveClick={handleCancleIsolation}
                  >
                    {{
                      default: () =>
                        t('project.workflow.cancle_isolation__confirm'),
                      trigger: () => t('project.workflow.cancle_isolation')
                    }}
                  </NPopconfirm>
                </NButton>
              </NSpace>
            ),
            default: () => (
              <NSpace vertical>
                <NDataTable
                  columns={columns.value}
                  data={state.list}
                  loading={state.loading}
                  rowKey={(row) => row.id}
                  v-model:checked-row-keys={state.checkedRowKeys}
                  striped
                />
                <NSpace justify='center'>
                  <NPagination
                    page={state.page}
                    page-size={state.pageSize}
                    page-count={state.totalPage}
                    show-quick-jumper
                    show-size-picker
                    page-sizes={[10, 30, 50]}
                    on-update:page={onChangePage}
                    on-update:page-size={onChangePageSize}
                  />
                </NSpace>
              </NSpace>
            )
          }}
        </Card>
        <DetailModal
          show={showDetailModal.value}
          projectCode={route.params.projectCode as string}
          onCancel={() => void (showDetailModal.value = false)}
          onUpdate={onUpdateList}
        />
      </NSpace>
    )
  }
})

export default TaskIsolation
