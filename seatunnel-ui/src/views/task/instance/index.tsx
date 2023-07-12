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
  ref,
  reactive,
  onMounted,
  onUnmounted,
  toRefs,
  watch
} from 'vue'
import {
  NSpace,
  NInput,
  NSelect,
  NDatePicker,
  NButton,
  NIcon,
  NDataTable,
  NPagination,
  NCard,
  NDropdown
} from 'naive-ui'
import { SearchOutlined, ReloadOutlined } from '@vicons/antd'
import { useTable } from './use-table'
import { useI18n } from 'vue-i18n'
import { stateType } from '@/common/common'
import Card from '@/components/card'
import LogModal from '@/components/log-modal'
import { useAsyncState } from '@vueuse/core'
import { queryLog } from '@/service/modules/log'
import styles from './index.module.scss'
import { LogRes } from '@/service/modules/log/types'
import ColumnSelector from '../../components/column-selector'
import ProjectSelector from '@/views/projects/components/projectSelector'
import { useProjectStore } from '@/store/project'
import { getRangeShortCuts } from '@/utils/timePickeroption'
import DependentChainModal from '@/components/dependent-chain-modal'
import DependentTaskModal from '@/components/dependent-task-modal'
import { DownOutlined } from '@vicons/antd'

let initialState: any = null

const TaskInstance = defineComponent({
  name: 'task-instance',
  setup() {
    const {
      t,
      variables,
      getTableData,
      createColumns,
      onSearch,
      onDownloadLogs,
      onBatchCoronation,
      onBatchIsolation,
      batchBtnListClick,
      creatInstanceButtons,
      locale,
      handleDependentChain,
      handleBatchCleanState,
      handleCheckTaskDependentChain
    } = useTable()
    let logTimer: number
    const requestTableData = () => {
      if (initialState === null) {
        initialState = {
          searchVal: variables.searchVal,
          host: variables.host,
          flag: variables.flag,
          stateType: variables.stateType,
          datePickerRange: variables.datePickerRange,
          executorName: variables.executorName,
          processInstanceName: variables.processInstanceName
        }
      }
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal,
        processInstanceId: variables.processInstanceId,
        host: variables.host,
        flag: variables.flag,
        stateType: variables.stateType,
        datePickerRange: variables.datePickerRange,
        executorName: variables.executorName,
        processInstanceName: variables.processInstanceName
      })
    }

    const tableColumn = ref([]) as any

    const rangeShortCuts = reactive({
      rangeOption: {}
    })

    rangeShortCuts.rangeOption = getRangeShortCuts(t)
    const onReset = (): any => {
      variables.searchVal = initialState.searchVal
      variables.host = initialState.host
      variables.flag = initialState.flag
      variables.stateType = initialState.stateType
      variables.datePickerRange = initialState.datePickerRange
      variables.executorName = initialState.executorName
      variables.processInstanceName = initialState.processInstanceName
      variables.projectCode = useProjectStore().getCurrentProject
    }

    const onUpdatePageSize = () => {
      variables.page = 1
      requestTableData()
    }

    const onConfirmModal = () => {
      variables.showModalRef = false
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

    const handleChangeColumn = (options: any) => {
      tableColumn.value = options
    }

    const refreshLogs = (row: any) => {
      variables.logRef = ''
      variables.limit = 1000
      variables.skipLineNum = 0
      getLogs(row)
    }

    const handleBatchCoronation = () => {
      onBatchCoronation()
    }

    const handleBatchIsolation = () => {
      onBatchIsolation()
    }

    const handleConfirmDependentChainModal = () => {
      handleDependentChain()
    }

    const handleCancelDependentChainModal = () => {
      variables.dependentChainShow = false
    }

    const handleConfirmDependentTaskModal = (taskIds: number[]) => {
      variables.dependentTasks = variables.dependentTasks.filter((task: any) =>
        taskIds.includes(task.id)
      )
      handleDependentChain()
    }

    const handleCancelDependentTaskModal = () => {
      variables.dependentTaskShow = false
    }

    const getFlag = () => [
      { label: t('project.task.newest'), value: 'YES' },
      { label: t('project.task.history'), value: 'NO' }
    ]

    onMounted(() => {
      createColumns(variables)
      creatInstanceButtons(variables)
      requestTableData()
    })

    onUnmounted(() => {
      clearTimeout(logTimer)
    })

    watch(useI18n().locale, () => {
      createColumns(variables)
      creatInstanceButtons(variables)
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

    const getProjectCodeList = (codes: any) => {
      if (!codes) {
        variables.projectCode = useProjectStore().getGolbalProject
      } else {
        variables.projectCode = [codes]
      }
    }
    watch(
      () => locale.value,
      () => {
        rangeShortCuts.rangeOption = getRangeShortCuts(t)
      }
    )

    return {
      t,
      ...toRefs(variables),
      requestTableData,
      onUpdatePageSize,
      getProjectCodeList,
      onSearch,
      onReset,
      onConfirmModal,
      refreshLogs,
      onDownloadLogs,
      tableColumn,
      handleChangeColumn,
      handleBatchCoronation,
      handleBatchIsolation,
      batchBtnListClick,
      rangeShortCuts,
      handleBatchCleanState,
      handleConfirmDependentChainModal,
      handleCancelDependentChainModal,
      handleConfirmDependentTaskModal,
      handleCancelDependentTaskModal,
      handleCheckTaskDependentChain,
      getFlag
    }
  },
  render() {
    const {
      t,
      requestTableData,
      onUpdatePageSize,
      onSearch,
      onReset,
      onConfirmModal,
      loadingRef,
      refreshLogs,
      onDownloadLogs
    } = this

    return (
      <>
        <NCard>
          <NSpace justify='end' wrap={false}>
            {this.globalProject && (
              <ProjectSelector
                initCode={
                  this.projectCode.length == 1 ? this.projectCode[0] : null
                }
                size={'small'}
                onGetprojectList={this.getProjectCodeList}
              />
            )}

            <NInput
              v-model={[this.searchVal, 'value']}
              size='small'
              placeholder={t('project.task.task_name')}
              clearable
            />
            <NInput
              v-model={[this.processInstanceName, 'value']}
              size='small'
              placeholder={t('project.task.workflow_instance')}
              clearable
            />
            <NInput
              v-model={[this.executorName, 'value']}
              size='small'
              placeholder={t('project.task.executor')}
              clearable
            />
            <NInput
              v-model={[this.host, 'value']}
              size='small'
              placeholder={t('project.task.host')}
              clearable
            />
            <NSelect
              v-model={[this.flag, 'value']}
              size='small'
              options={this.getFlag()}
              placeholder={t('project.task.flag')}
              style={{ width: '100px' }}
              clearable
            />
            <NSelect
              v-model={[this.stateType, 'value']}
              size='small'
              options={stateType(t).slice(1)}
              placeholder={t('project.task.state')}
              style={{ width: '180px' }}
              clearable
            />
            <NDatePicker
              v-model={[this.datePickerRange, 'value']}
              type='datetimerange'
              size='small'
              shortcuts={this.rangeShortCuts.rangeOption}
              start-placeholder={t('project.task.start_time')}
              end-placeholder={t('project.task.end_time')}
              clearable
            />
            <NButton size='small' onClick={onReset}>
              <NIcon>
                <ReloadOutlined />
              </NIcon>
            </NButton>
            <NButton size='small' type='primary' onClick={onSearch}>
              <NIcon>
                <SearchOutlined />
              </NIcon>
            </NButton>
          </NSpace>
        </NCard>
        <Card class={styles['table-card']} title={t('project.task_instance')}>
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
                {/* <NTooltip>
                  {{
                    default: () => t('project.workflow.coronation'),
                    trigger: () => (
                      <NButton
                        tag='div'
                        type='primary'
                        disabled={this.checkedRowKeys.length <= 0}
                        v-permission='project:coronation-task:submit'
                      >
                        <NPopconfirm
                          disabled={this.checkedRowKeys.length <= 0}
                          onPositiveClick={this.handleBatchCoronation}
                        >
                          {{
                            default: () =>
                              t('project.workflow.coronation_confirm'),
                            trigger: () => t('project.workflow.coronation')
                          }}
                        </NPopconfirm>
                      </NButton>
                    )
                  }}
                </NTooltip>
                <NTooltip>
                  {{
                    default: () => t('project.workflow.isolation'),
                    trigger: () => (
                      <NButton
                        tag='div'
                        type='primary'
                        disabled={this.checkedRowKeys.length <= 0}
                        v-permission='project:isolation-task:submit'
                      >
                        <NPopconfirm
                          disabled={this.checkedRowKeys.length <= 0}
                          onPositiveClick={this.handleBatchIsolation}
                        >
                          {{
                            default: () =>
                              t('project.workflow.pause_recovery_confirm'),
                            trigger: () => t('project.workflow.isolation')
                          }}
                        </NPopconfirm>
                      </NButton>
                    )
                  }}
                </NTooltip> */}
              </NSpace>
            ),
            default: () => (
              <NSpace vertical>
                <NDataTable
                  rowKey={(row) => row.id}
                  loading={loadingRef}
                  columns={this.columns}
                  data={this.tableData}
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
                    onUpdatePage={requestTableData}
                    onUpdatePageSize={onUpdatePageSize as any}
                  />
                </NSpace>
              </NSpace>
            )
          }}
        </Card>
        <LogModal
          showModalRef={this.showModalRef}
          logRef={this.logRef}
          row={this.row}
          showDownloadLog={true}
          logLoadingRef={this.logLoadingRef}
          onConfirmModal={onConfirmModal}
          onRefreshLogs={refreshLogs}
          onDownloadLogs={onDownloadLogs}
        />
        <DependentChainModal
          type='task'
          saving={this.dependentChainSaving}
          show={this.dependentChainShow}
          taskName={this.dependentChainName}
          runWorkflows={this.runWorkflows}
          skipWorkflows={this.skipWorkflows}
          onCancelModal={this.handleCancelDependentChainModal}
          onConfirmModal={this.handleConfirmDependentChainModal}
        />
        <DependentTaskModal
          saving={this.dependentChainSaving}
          show={this.dependentTaskShow}
          data={this.dependentTasks}
          onCancelModal={this.handleCancelDependentTaskModal}
          onConfirmModal={this.handleConfirmDependentTaskModal}
          onCheckDependentChain={this.handleCheckTaskDependentChain}
        />
      </>
    )
  }
})

export default TaskInstance
