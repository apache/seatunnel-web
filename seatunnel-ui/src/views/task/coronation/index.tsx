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

import { defineComponent, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { SearchOutlined, ReloadOutlined } from '@vicons/antd'
import {
  NButton,
  NDataTable,
  NIcon,
  NInput,
  NPagination,
  NSpace,
  NPopconfirm
} from 'naive-ui'
import Card from '@/components/card'
import { useTable } from './use-table'
import ImportModal from './components/import-modal'
import ProjectSelector from '../../components/projectSelector'
import { useProjectStore } from '@/store/project'
import { useRoute, useRouter } from 'vue-router'
import _ from 'lodash'

const TaskCoronation = defineComponent({
  name: 'task-coronation',
  setup() {
    const { t } = useI18n()
    const route = useRoute()
    const router = useRouter()

    const {
      variables,
      getTableData,
      createColumns,
      handleImport,
      downloadTemplate,
      onCancleCoronation
    } = useTable()

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        taskName: variables.taskName,
        workflowInstanceName: variables.workflowInstanceName
      })
    }

    const handleChangePageSize = () => {
      variables.page = 1
      requestData()
    }

    const handleSearch = () => {
      variables.page = 1

      const query = {} as any
      if (variables.taskName) {
        query.taskName = variables.taskName
      }

      if (variables.workflowInstanceName) {
        query.workflowInstanceName = variables.workflowInstanceName
      }

      router.replace({
        query: !_.isEmpty(query)
          ? {
              ...query,
              project: route.query.project,
              global: route.query.global,
              searchProjectCode: variables.projectCodes
            }
          : {
              ...route.query,
              searchProjectCode: variables.projectCodes
            }
      })
      requestData()
    }

    const onReset = () => {
      variables.taskName = null
      variables.workflowInstanceName = null
      variables.projectCodes = useProjectStore().getCurrentProject
    }
    const handleCancleCoronation = () => {
      onCancleCoronation()
      requestData()
    }
    const handleKeyup = (event: KeyboardEvent) => {
      if (event.key === 'Enter') {
        handleSearch()
      }
    }

    const onCancelModal = () => {
      variables.showModalRef = false
    }

    const onConfirmModal = () => {
      variables.showModalRef = false
      requestData()
    }

    const initSearch = () => {
      const { taskName, workflowInstanceName } = route.query
      if (taskName) {
        variables.taskName = taskName as any
      }

      if (workflowInstanceName) {
        variables.workflowInstanceName = workflowInstanceName as any
      }
    }

    onMounted(() => {
      initSearch()
      createColumns(variables)
      requestData()
    })

    watch(useI18n().locale, () => {
      createColumns(variables)
    })
    const getProjectCodeList = (codes: any) => {
      if (!codes) {
        variables.projectCodes = useProjectStore().getGolbalProject
      } else {
        variables.projectCodes = [codes]
      }
    }

    return () => (
      <NSpace vertical>
        <Card>
          <NSpace justify='space-between'>
            <NSpace>
              <NButton
                size='small'
                onClick={handleImport}
                type='primary'
                v-permission='project:coronation-task:parse'
              >
                {t('project.task.import_coronation_task')}
              </NButton>
              <NButton quaternary color='#1890ff' onClick={downloadTemplate}>
                {t('project.workflow.download_template')}
              </NButton>
            </NSpace>
            <NSpace>
              {variables.globalProject && (
                <ProjectSelector
                  initCode={
                    variables.projectCodes.length == 1
                      ? variables.projectCodes[0]
                      : null
                  }
                  size={'small'}
                  onGetprojectList={getProjectCodeList}
                />
              )}
              <NInput
                size='small'
                v-model={[variables.taskName, 'value']}
                placeholder={t('project.task.task_name')}
                clearable
                onKeyup={handleKeyup}
              />
              <NInput
                size='small'
                v-model={[variables.workflowInstanceName, 'value']}
                placeholder={t('project.task.workflow_instance_name')}
                clearable
                onKeyup={handleKeyup}
              />
              <NButton size='small' onClick={onReset}>
                <NIcon>
                  <ReloadOutlined />
                </NIcon>
              </NButton>
              <NButton size='small' type='primary' onClick={handleSearch}>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </NSpace>
          </NSpace>
        </Card>
        <Card title={t('menu.coronation_list')}>
          {{
            'header-extra': () => (
              <NSpace justify='space-between'>
                <NButton
                  tag='div'
                  type='primary'
                  disabled={variables.checkedRowKeys.length <= 0}
                >
                  <NPopconfirm
                    disabled={variables.checkedRowKeys.length <= 0}
                    onPositiveClick={handleCancleCoronation}
                  >
                    {{
                      default: () =>
                        t('project.workflow.cancle_coronation_confirm'),
                      trigger: () => t('project.workflow.cancle_coronation')
                    }}
                  </NPopconfirm>
                </NButton>
              </NSpace>
            ),
            default: () => (
              <NSpace vertical>
                <NDataTable
                  loading={variables.loadingRef}
                  columns={variables.columns}
                  data={variables.tableData}
                  scrollX={variables.tableWidth}
                  rowKey={(row) => row.id}
                  v-model:checked-row-keys={variables.checkedRowKeys}
                />
                <NSpace justify='center'>
                  <NPagination
                    v-model:page={variables.page}
                    v-model:page-size={variables.pageSize}
                    page-count={variables.totalPage}
                    show-size-picker
                    page-sizes={[10, 30, 50]}
                    show-quick-jumper
                    onUpdatePage={requestData}
                    onUpdatePageSize={handleChangePageSize}
                  />
                </NSpace>
              </NSpace>
            )
          }}
        </Card>
        <ImportModal
          showModalRef={variables.showModalRef}
          onCancelModal={onCancelModal}
          onConfirmModal={onConfirmModal}
        />
      </NSpace>
    )
  }
})

export default TaskCoronation
