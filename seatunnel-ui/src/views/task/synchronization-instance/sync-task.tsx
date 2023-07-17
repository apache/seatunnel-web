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

import {
  defineComponent,
  onMounted,
  onUnmounted,
  PropType,
  toRefs,
  watch,
  ref,
  reactive
} from 'vue'
import { useSyncTask } from './use-sync-task'
import {
  NSpace,
  NCard,
  NDataTable,
  NPagination,
  NInput,
  NSelect,
  NDatePicker,
  NIcon,
  NButton,
  NGrid,
  NGi,
  NDropdown
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { stateType } from '@/common/common'
import LogModal from '@/components/log-modal'
import { SearchOutlined, ReloadOutlined } from '@vicons/antd'
import { useAsyncState } from '@vueuse/core'
import { queryLog } from '@/service/log'
import { LogRes } from '@/service/log/types'
import ColumnSelector from '../../projects/components/column-selector'
import { useProjectStore } from '@/store/project'
import ProjectSelector from '../../projects/components/projectSelector'
import { getRangeShortCuts } from '@/utils/timePickeroption'
import { useRoute, useRouter } from 'vue-router'
import _ from 'lodash'
import { DownOutlined } from '@vicons/antd'

const props = {
  syncTaskType: {
    type: String as PropType<string>,
    default: 'BATCH'
  }
}

const SyncTask = defineComponent({
  name: 'SyncTask',
  props,
  setup(props) {
    let logTimer: number
    const { t } = useI18n()
    const {
      variables,
      getTableData,
      batchBtnListClick,
      creatInstanceButtons,
      createColumns,
      onReset
    } = useSyncTask(props.syncTaskType)
    const route = useRoute()
    const router = useRouter()

    const tableColumn = ref([]) as any
    const requestData = () => {
      getTableData({
        pageNo: variables.page,
        pageSize: variables.pageSize,
        processInstanceName: variables.workflowInstance,
        taskName: variables.taskName,
        executorName: variables.executeUser,
        host: variables.host,
        stateType: variables.stateType,
        startDate: variables.datePickerRange
          ? variables.datePickerRange[0]
          : '',
        endDate: variables.datePickerRange ? variables.datePickerRange[1] : '',
        syncTaskType: variables.syncTaskType
      })
    }
    const rangeShortCuts = reactive({
      rangeOption: {}
    })
    rangeShortCuts.rangeOption = getRangeShortCuts(t)

    const onUpdatePageSize = () => {
      variables.page = 1
      requestData()
    }

    const getLogs = (row: any) => {
      const { state } = useAsyncState(
        queryLog({
          taskInstanceId: Number(row.id),
          limit: variables.limit,
          skipLineNum: variables.skipLineNum
        }).then((res: LogRes) => {
          if (res.log) {
            variables.logRef += res.log
          }
          if (res.hasNext) {
            variables.limit += 1000
            variables.skipLineNum += 1000
            clearTimeout(logTimer)
            logTimer = setTimeout(() => {
              getLogs(row)
            }, 2000)
          } else {
            variables.logLoadingRef = false
          }
        }),
        {}
      )

      return state
    }

    const refreshLogs = (row: any) => {
      variables.logRef = ''
      variables.limit = 1000
      variables.skipLineNum = 0
      getLogs(row)
    }

    const handleSearch = () => {
      variables.page = 1

      const query = {} as any
      if (variables.taskName) {
        query.taskName = variables.taskName
      }

      if (variables.workflowInstance) {
        query.workflowInstance = variables.workflowInstance
      }

      if (variables.executeUser) {
        query.executeUser = variables.executeUser
      }

      if (variables.host) {
        query.host = variables.host
      }

      if (variables.stateType) {
        query.stateType = variables.stateType
      }

      if (variables.datePickerRange) {
        query.startDate = variables.datePickerRange[0]
        query.endDate = variables.datePickerRange[1]
      }

      router.replace({
        query: !_.isEmpty(query)
          ? {
            ...route.query,
            ...query,
            syncTaskType: props.syncTaskType,
            searchProjectCode: variables.projectCodes
            }
          : {
              ...route.query,
              syncTaskType: props.syncTaskType,
              searchProjectCode: variables.projectCodes
            }
      })
      requestData()
    }

    const handleKeyup = (event: KeyboardEvent) => {
      if (event.key === 'Enter') {
        handleSearch()
      }
    }

    const initSearch = () => {
      const { startDate, endDate } = route.query
      if (startDate && endDate) {
        variables.datePickerRange = [startDate as string, endDate as string]
      }
      variables.taskName = (route.query.taskName as string) || ''
      variables.workflowInstance =
        (route.query.workflowInstance as string) || ''
      variables.executeUser = (route.query.executeUser as string) || ''
      variables.host = (route.query.host as string) || ''
      variables.stateType = (route.query.stateType as string) || null
    }

    onMounted(() => {
      initSearch()
      createColumns(variables)
      creatInstanceButtons(variables)
      requestData()
    })

    onUnmounted(() => {
      clearTimeout(logTimer)
    })

    watch(useI18n().locale, () => {
      createColumns(variables)
      creatInstanceButtons(variables)
      rangeShortCuts.rangeOption = getRangeShortCuts(t)
    })

    watch(
      () => variables.showModalRef,
      () => {
        if (variables.showModalRef) {
          getLogs(variables.row)
        } else {
          variables.row = {}
          variables.logRef = ''
          variables.logLoadingRef = true
          variables.skipLineNum = 0
          variables.limit = 1000
          clearTimeout(logTimer)
        }
      }
    )

    const handleChangeColumn = (options: any) => {
      tableColumn.value = options
    }
    const getProjectCodeList = (codes: any) => {
      if (!codes) {
        variables.projectCodes = useProjectStore().getGolbalProject
      } else {
        variables.projectCodes = [codes]
      }
    }

    return {
      t,
      ...toRefs(variables),
      requestData,
      onUpdatePageSize,
      refreshLogs,
      handleSearch,
      onReset,
      handleKeyup,
      handleChangeColumn,
      batchBtnListClick,
      getProjectCodeList,
      tableColumn,
      rangeShortCuts
    }
  },
  render() {
    const { t } = this
    return (
      <NSpace vertical>
        <NCard>
          <NGrid cols={26} yGap={10} xGap={5}>
            {this.globalProject && (
              <NGi span={5}>
                <ProjectSelector
                  initCode={
                    this.projectCodes.length == 1 ? this.projectCodes[0] : null
                  }
                  onGetprojectList={this.getProjectCodeList}
                  style={{ 'width': '100%' }}
                ></ProjectSelector>
              </NGi>
            )}
            
            <NGi span={5}>
              <NInput
                v-model={[this.taskName, 'value']}
                placeholder={this.t(
                  'project.synchronization_instance.task_name'
                )}
                onKeyup={this.handleKeyup}
              />
            </NGi>
            <NGi span={5}>
              <NInput
                v-model={[this.workflowInstance, 'value']}
                placeholder={this.t(
                  'project.synchronization_instance.workflow_instance'
                )}
                onKeyup={this.handleKeyup}
              />
            </NGi>
            <NGi span={4}>
              <NInput
                v-model={[this.executeUser, 'value']}
                placeholder={this.t(
                  'project.synchronization_instance.execute_user'
                )}
                onKeyup={this.handleKeyup}
              />
            </NGi>
            <NGi span={6}>
              <NInput
                v-model={[this.host, 'value']}
                placeholder={this.t('project.synchronization_instance.host')}
                onKeyup={this.handleKeyup}
              />
            </NGi>
            <NGi span={6}>
              <NSelect
                style={{ width: '100%' }}
                v-model={[this.stateType, 'value']}
                options={stateType(this.t).slice(1)}
                placeholder={this.t('project.synchronization_instance.state')}
                clearable
              />
            </NGi>
            <NGi span={26}>
              <NSpace justify='end'>
                <NDatePicker
                  v-model={[this.datePickerRange, 'formattedValue']}
                  type='datetimerange'
                  start-placeholder={this.t(
                    'project.synchronization_instance.start_time'
                  )}
                  end-placeholder={this.t(
                    'project.synchronization_instance.end_time'
                  )}
                  shortcuts={this.rangeShortCuts.rangeOption}
                />
                <NButton onClick={this.onReset}>
                  <NIcon>
                    <ReloadOutlined />
                  </NIcon>
                </NButton>
                <NButton type='primary' onClick={this.handleSearch}>
                  <NIcon>
                    <SearchOutlined />
                  </NIcon>
                </NButton>
              </NSpace>
            </NGi>
          </NGrid>
        </NCard>
        <NCard title={t('project.synchronizing_task_instance')}>
          {{
            'header-extra': () => (
              <NSpace justify='space-between'>
                <ColumnSelector
                  tableKey='taskInstance'
                  tableColumns={this.columns}
                  onChangeOptions={this.handleChangeColumn}
                ></ColumnSelector>
                <NDropdown
                  options={this.buttonList}
                  trigger={'click'}
                  onSelect={this.batchBtnListClick}
                  width={150}
                >
                  <NButton>
                    {t('project.workflow.operation')}
                    <NIcon style={{ marginLeft: '5px' }}>
                      <DownOutlined />
                    </NIcon>
                  </NButton>
                </NDropdown>
              </NSpace>
            ),
            default: () => (
              <NSpace vertical>
                <NDataTable
                  loading={this.loadingRef}
                  columns={this.tableColumn}
                  data={this.tableData}
                  rowKey={(row) => row.id}
                  scrollX={this.tableWidth}
                  v-model:checked-row-keys={this.checkedRowKeys}
                />
                <NSpace justify='center'>
                  <NPagination
                    v-model:page={this.page}
                    v-model:page-size={this.pageSize}
                    page-count={this.totalPage}
                    show-size-picker
                    page-sizes={[10, 30, 50]}
                    show-quick-jumper
                    onUpdatePage={this.requestData}
                    onUpdatePageSize={this.onUpdatePageSize}
                  />
                </NSpace>
              </NSpace>
            )
          }}
        </NCard>
        <LogModal
          showModalRef={this.showModalRef}
          logRef={this.logRef}
          row={this.row}
          logLoadingRef={this.logLoadingRef}
          onConfirmModal={() => (this.showModalRef = false)}
          onRefreshLogs={this.refreshLogs}
        />
      </NSpace>
    )
  }
})

export { SyncTask }
