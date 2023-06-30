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

import { useI18n } from 'vue-i18n'
import { h, reactive, ref } from 'vue'
import { useAsyncState } from '@vueuse/core'
import {
  queryTaskListPaging,
  forceSuccess,
  downloadLog,
  cleanState,
  checkDependentChain,
  dependentChainRerun
} from '@/service/modules/task-instances'
import { NIcon, NTooltip, NSpin, NButton, NDropdown, NTag } from 'naive-ui'
import {
  AlignLeftOutlined,
  CheckCircleOutlined,
  ClearOutlined,
  PaperClipOutlined,
  PartitionOutlined
} from '@vicons/antd'
import { useRoute, useRouter } from 'vue-router'
import { parseTime, renderTableTime, tasksState } from '@/common/common'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import { mergedPath } from '@/common/path'
import { useTableLink, useTableOperation } from '@/hooks'
import { getTime, format, subHours, addHours } from 'date-fns'
import { throttle } from 'lodash'
import { submitIsolationTasks } from '@/service/modules/isolation-task'
import type { Router, TaskInstancesRes, IRecord, ITaskState } from './types'
import {
  submitTaskCoronation,
  cleanStateByIds,
  forcedSuccessByIds
} from '@/service/modules/task-coronation'
import { useProjectStore } from '@/store/project'
import { changeProject } from '../../utils/changeProject'

export function useTable() {
  const { t, locale } = useI18n()
  const route = useRoute()
  const router: Router = useRouter()
  const projectCode =
    route.params.projectCode || useProjectStore().getCurrentProject[0]
  const date = route.query.date
    ? (route.query.date as string).split(',')
    : route.query.date === undefined
    ? [getTime(subHours(Date.now(), 23)), getTime(addHours(Date.now(), 1))]
    : []
  const projectStore = useProjectStore()
  const variables = reactive({
    columns: [],
    checkedRowKeys: [] as Array<number>,
    tableWidth: DefaultTableWidth,
    tableData: [] as IRecord[],
    page: ref(1),
    pageSize: ref(10),
    searchVal: (route.query.searchVal as string) || null,
    processInstanceId: route.query.processInstanceId
      ? Number(route.query.processInstanceId)
      : null,
    host: (route.query.host as string) || null,
    flag: (route.query.flag as string) || null,
    stateType: (route.query.stateType as string) || null,
    datePickerRange: ref(
      date.length === 2 ? [Number(date[0]), Number(date[1])] : null
    ),
    executorName: (route.query.executorName as string) || null,
    processInstanceName: (route.query.processInstanceName as string) || null,
    totalPage: ref(1),
    showModalRef: ref(false),
    row: {},
    loadingRef: ref(false),
    logRef: '',
    logLoadingRef: ref(true),
    skipLineNum: ref(0),
    limit: ref(1000),
    buttonList: [],
    projectCode:
      route.query.searchProjectCode || projectStore.getCurrentProject,
    dependentChainSaving: false,
    dependentTaskShow: false,
    dependentChainShow: false,
    dependentChainName: '',
    dependentTasks: [] as any,
    runWorkflows: [] as any,
    skipWorkflows: [] as any,
    globalProject: projectStore.getGlobalFlag
  })

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        type: 'selection',
        className: 'btn-selected',
        ...COLUMN_WIDTH_CONFIG['selection']
      },
      {
        title: t('project.task.task_name'),
        key: 'name',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      useTableLink(
        {
          title: t('project.task.workflow_instance'),
          key: 'processInstanceName',
          ...COLUMN_WIDTH_CONFIG['link_name'],
          button: {
            permission: 'project:process-instance:update',
            getHref: (row: any) => {
              return mergedPath(
                `projects/${row.projectCode}/workflow/instances/${
                  row.processInstanceId
                }?code=${row.projectCode}&project=${
                  (route.query.project as string) || 'all'
                }&global=${String(projectStore.getGlobalFlag)}`
              )
            }
          }
        },
        'project'
      ),
      useTableLink({
        title: t('project.project_name'),
        key: 'projectName',
        ...COLUMN_WIDTH_CONFIG['link_name'],
        button: {
          onClick: (row: any) => {
            changeProject(row.projectCode)
            router.push({
              path: route.path,
              query: {
                project: row.projectCode,
                global: 'false'
              }
            })
          }
        }
      }),
      {
        title: t('project.task.executor'),
        key: 'executorName',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.task.node_type'),
        key: 'taskType',
        ...COLUMN_WIDTH_CONFIG['type']
      },
      {
        title: t('project.task.flag'),
        key: 'flag',
        ...COLUMN_WIDTH_CONFIG['type'],
        render: (rowData: any) =>
          h(
            NTag,
            {
              size: 'small',
              round: true,
              type: rowData.flag === 'YES' ? 'info' : 'default'
            },
            {
              default: () =>
                rowData.flag === 'YES'
                  ? t('project.task.newest')
                  : t('project.task.history')
            }
          )
      },
      {
        title: t('project.task.state'),
        key: 'state',
        ...COLUMN_WIDTH_CONFIG['state'],
        render: (row: IRecord) => renderStateCell(row.state, t)
      },
      {
        title: t('project.task.submit_time'),
        ...COLUMN_WIDTH_CONFIG['time'],
        key: 'submitTime',
        render: (row: IRecord) => renderTableTime(row.submitTime)
      },
      {
        title: t('project.task.start_time'),
        ...COLUMN_WIDTH_CONFIG['time'],
        key: 'startTime',
        render: (row: IRecord) => renderTableTime(row.startTime)
      },
      {
        title: t('project.task.end_time'),
        ...COLUMN_WIDTH_CONFIG['time'],
        key: 'endTime',
        render: (row: IRecord) => renderTableTime(row.endTime)
      },
      {
        title: t('project.task.duration'),
        key: 'duration',
        ...COLUMN_WIDTH_CONFIG['duration'],
        render: (row: any) => h('span', null, row.duration ? row.duration : '-')
      },
      {
        title: t('project.task.retry_count'),
        key: 'retryTimes',
        ...COLUMN_WIDTH_CONFIG['times']
      },
      {
        title: t('project.task.dry_run_flag'),
        key: 'dryRun',
        ...COLUMN_WIDTH_CONFIG['dryRun'],
        render: (row: IRecord) => (row.dryRun === 1 ? 'YES' : 'NO')
      },
      {
        title: t('project.task.host'),
        key: 'host',
        ...COLUMN_WIDTH_CONFIG['name'],
        render: (row: IRecord) => row.host || '-'
      },
      useTableOperation(
        {
          title: t('project.task.operation'),
          noPermission: projectStore.getGlobalFlag,
          key: 'operation',
          itemNum: 4,
          buttons: [
            {
              text: t('project.task.clean_state'),
              permission: 'project:task-instance:clean-state',
              isCustom: true,
              customFunc: (rowData: any) => {
                return h(
                  NDropdown,
                  {
                    trigger: 'click',
                    placement: 'bottom-end',
                    options: [
                      {
                        label: t('project.task.only_current'),
                        key: 'CURRENT'
                      },
                      {
                        label: t('project.task.current_downstream'),
                        key: 'DOWNSTREAM'
                      },
                      {
                        label: t('project.task.cascaded_dependency_chain'),
                        key: 'CHAIN'
                      }
                    ],
                    onSelect: (key) => handleCleanState(key, rowData)
                  },
                  {
                    default: () =>
                      h(NTooltip, null, {
                        trigger: () =>
                          h(
                            NButton,
                            {
                              tag: 'div',
                              circle: true,
                              size: 'small',
                              type: 'info',
                              disabled:
                                rowData.state === 'RUNNING_EXECUTION' ||
                                rowData.isEdit === false
                            },
                            {
                              default: () =>
                                h(NIcon, null, {
                                  default: () => h(ClearOutlined)
                                })
                            }
                          ),
                        default: () => t('project.task.clean_state')
                      })
                  }
                )
              }
            },
            {
              text: t('project.task.forced_success'),
              permission: 'project:task-instance:force-success',
              icon: h(CheckCircleOutlined),
              onClick: (rowData) => void handleForcedSuccess(rowData),
              disabled: (rowData) =>
                !(
                  rowData.state === 'FAILURE' ||
                  rowData.state === 'NEED_FAULT_TOLERANCE' ||
                  rowData.state === 'KILL'
                )
            },
            {
              text: t('project.task.view_log'),
              icon: h(AlignLeftOutlined),
              onClick: (rowData) => void handleLog(rowData),
              disabled: (rowData) => !rowData.host
            },
            {
              text: t('project.workflow.impact_anaysis'),
              permission: 'project:task-instance:lineage',
              icon: h(PaperClipOutlined),
              onClick: (rowData) => void gotoWorkflowImpactAnalysis(rowData)
            }
          ]
        },
        'project'
      )
    ]
    if (variables.tableWidth) {
      variables.tableWidth = calculateTableWidth(variables.columns)
    }
  }

  const creatInstanceButtons = (variables: any) => {
    variables.buttonList = [
      {
        label: t('project.task.clean_state'),
        key: 'clean_state',
        children: [
          {
            label: t('project.task.only_current'),
            key: 'CURRENT'
          },
          {
            label: t('project.task.current_downstream'),
            key: 'DOWNSTREAM'
          },
          {
            label: t('project.task.cascaded_dependency_chain'),
            key: 'CHAIN'
          }
        ]
      },
      {
        label: t('project.task.forced_success'),
        key: 'forced_success'
      },
      {
        type: 'divider',
        key: 'd1'
      },
      {
        label: t('project.workflow.coronation'),
        key: 'coronation'
      },
      {
        label: t('project.workflow.isolation'),
        key: 'isolation'
      }
    ]
  }

  const gotoWorkflowImpactAnalysis = (row: any) => {
    const url = router.resolve({
      name: 'impact-analysis',
      params: {
        projectCode: row.projectCode
      },
      query: {
        taskCode: row.id,
        projectCode: row.projectCode,
        workflowCode: row.processInstanceId,
        workflowType: 'instance'
      }
    })
    window.open(url.href)
  }

  const handleLog = (row: any) => {
    variables.showModalRef = true
    variables.row = row
  }

  let cleaning = false
  const handleCleanState = throttle((key: string, row: any) => {
    if (cleaning) return
    if (key === 'CURRENT' || key === 'DOWNSTREAM') {
      cleaning = true
      cleanState(row.projectCode, [row.id], key === 'DOWNSTREAM')
        .then(() => {
          window.$message.success(t('project.workflow.success'))
          getList()
        })
        .finally(() => {
          cleaning = false
        })
    } else if (key === 'CHAIN') {
      variables.runWorkflows = []
      variables.skipWorkflows = []
      variables.loadingRef = true
      checkDependentChain(projectCode, [row.id]).then((res: any) => {
        variables.dependentChainName = ''
        variables.dependentChainShow = true
        variables.dependentTasks = [{ id: row.id, name: row.name }]
        variables.loadingRef = false
        variables.runWorkflows = res.runWorkflows.map((workflow: any) => ({
          label: workflow.workflowName,
          key: workflow.workflowName,
          prefix: () => h(NIcon, null, { default: () => h(PartitionOutlined) }),
          children: workflow.tasks.map((task: string) => ({
            label: task,
            key: task
          }))
        }))
        variables.skipWorkflows = res.skipWorkflows.map((workflow: any) => ({
          label: workflow.workflowName,
          key: workflow.workflowName,
          prefix: () => h(NIcon, null, { default: () => h(PartitionOutlined) }),
          children: workflow.tasks.map((task: string) => ({
            label: task,
            key: task
          }))
        }))
      })
    }
  }, 3000)

  const handleDependentChain = () => {
    const taskInstanceIds = variables.dependentTasks.map((task: any) => task.id)
    variables.dependentChainSaving = true
    dependentChainRerun(projectCode, taskInstanceIds)
      .then(() => {
        variables.checkedRowKeys = []
        window.$message.success(t('project.workflow.success'))
        getList()
      })
      .finally(() => {
        variables.dependentTaskShow = false
        variables.dependentChainShow = false
        variables.dependentChainSaving = false
      })
  }

  const handleForcedSuccess = (row: any) => {
    forceSuccess({ id: row.id }, { projectCode: row.projectCode }).then(() => {
      getList()
    })
  }

  const getTableData = (params: any) => {
    if (
      variables.loadingRef ||
      !variables.projectCode ||
      variables.projectCode.length === 0 ||
      typeof variables.projectCode[0] === 'undefined'
    )
      return
    variables.loadingRef = true
    const data = {
      pageSize: params.pageSize,
      pageNo: params.pageNo,
      searchVal: params.searchVal,
      processInstanceId: params.processInstanceId,
      host: params.host,
      flag: params.flag,
      stateType: params.stateType,
      startDate: params.datePickerRange
        ? format(parseTime(params.datePickerRange[0]), 'yyyy-MM-dd HH:mm:ss')
        : '',
      endDate: params.datePickerRange
        ? format(parseTime(params.datePickerRange[1]), 'yyyy-MM-dd HH:mm:ss')
        : '',
      executorName: params.executorName,
      processInstanceName: params.processInstanceName,
      projectCodes: variables.projectCode
    }

    const { state } = useAsyncState(
      queryTaskListPaging(data)
        .then((res: TaskInstancesRes) => {
          variables.tableData = res.totalList as any
          variables.totalPage = res.totalPage
          variables.loadingRef = false
        })
        .catch(() => {
          variables.loadingRef = false
        }),
      {}
    )

    return state
  }
  const onSearch = () => {
    const query = {} as {
      searchVal?: string
      processInstanceName?: string
      host?: string
      executorName?: string
      flag?: string
      stateType?: string
      date?: string
      project?: string
    }
    if (variables.searchVal) query.searchVal = variables.searchVal
    if (variables.processInstanceName)
      query.processInstanceName = variables.processInstanceName
    if (variables.host) query.host = variables.host
    if (variables.executorName) query.executorName = variables.executorName
    if (variables.flag) query.flag = variables.flag
    if (variables.stateType) query.stateType = variables.stateType
    query.date = variables.datePickerRange
      ? variables.datePickerRange.join(',')
      : ''
    router.replace({
      name: 'task-instance',
      params: {
        projectCode: route.params.projectCode
      },
      query: {
        ...query,
        project: route.query.project,
        global: route.query.global,
        searchProjectCode: variables.projectCode
      }
    })
  }

  const getList = () => {
    getTableData({
      pageSize: variables.pageSize,
      pageNo:
        variables.tableData.length === 1 && variables.page > 1
          ? variables.page - 1
          : variables.page,
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

  const onDownloadLogs = (row: { id: number }) => {
    downloadLog(row.id)
  }

  const getTaskList = () => {
    return variables.tableData.filter((row: any) =>
      variables.checkedRowKeys.includes(row.id)
    )
  }

  const onBatchCoronation = () => {
    const tasks = getTaskList()
    submitTaskCoronation(0, {
      coronationTasks: tasks.map((task: any) => ({
        workflowInstanceId: task.processInstanceId,
        workflowInstanceName: task.processInstanceName,
        taskCode: task.taskCode,
        taskNode: task.name
      }))
    }).then(() => {
      window.$message.success(t('project.workflow.success'))
      variables.checkedRowKeys = []
      getList()
    })
  }

  const onBatchIsolation = () => {
    const tasks = getTaskList()
    submitIsolationTasks(
      tasks.map((task: any) => ({
        workflowInstanceId: task.processInstanceId,
        workflowInstanceName: task.processInstanceName,
        taskCode: task.taskCode,
        taskName: task.name
      })),
      0
    ).then(() => {
      window.$message.success(t('project.workflow.success'))
      variables.checkedRowKeys = []
      getList()
    })
  }

  // eslint-disable-next-line @typescript-eslint/no-unused-vars, no-unused-vars
  const onBatchCleanState = () => {
    cleanStateByIds(variables.checkedRowKeys).then(() => {
      window.$message.success(t('project.workflow.success'))
      variables.checkedRowKeys = []
      getList()
    })
  }
  const onBatchForcedSuccess = () => {
    forcedSuccessByIds(variables.checkedRowKeys).then(() => {
      window.$message.success(t('project.workflow.success'))
      variables.checkedRowKeys = []
      getList()
    })
  }

  const batchBtnListClick = (key: string) => {
    if (variables.checkedRowKeys.length == 0) {
      window.$message.warning(t('project.select_task_instance'))
      return
    }
    switch (key) {
      case 'coronation':
        onBatchCoronation()
        break
      case 'isolation':
        onBatchIsolation()
        break
      case 'CURRENT':
        handleBatchCleanState()
        break
      case 'DOWNSTREAM':
        handleBatchCleanState(true)
        break
      case 'CHAIN':
        handleBatchCleanStateChain()
        break
      case 'forced_success':
        onBatchForcedSuccess()
        break
    }
  }

  const handleBatchCleanState = (cleanDownstream = false) => {
    cleanState(projectCode, variables.checkedRowKeys, cleanDownstream).then(
      () => {
        variables.checkedRowKeys = []
        window.$message.success(t('project.workflow.success'))
        getList()
      }
    )
  }

  const handleBatchCleanStateChain = () => {
    variables.dependentTaskShow = true
    variables.dependentTasks = variables.tableData.filter((rowData: any) =>
      variables.checkedRowKeys.includes(rowData.id)
    )
  }

  const handleCheckTaskDependentChain = (taskId: number, taskName: string) => {
    variables.runWorkflows = []
    variables.skipWorkflows = []
    checkDependentChain(projectCode, [taskId]).then((res: any) => {
      variables.dependentChainShow = true
      variables.dependentChainName = taskName
      variables.runWorkflows = res.runWorkflows.map((workflow: any) => ({
        label: workflow.workflowName,
        key: workflow.workflowName,
        prefix: () => h(NIcon, null, { default: () => h(PartitionOutlined) }),
        children: workflow.tasks.map((task: string) => ({
          label: task,
          key: task
        }))
      }))
      variables.skipWorkflows = res.skipWorkflows.map((workflow: any) => ({
        label: workflow.workflowName,
        key: workflow.workflowName,
        prefix: () => h(NIcon, null, { default: () => h(PartitionOutlined) }),
        children: workflow.tasks.map((task: string) => ({
          label: task,
          key: task
        }))
      }))
    })
  }

  return {
    t,
    variables,
    getTableData,
    createColumns,
    onSearch,
    onDownloadLogs,
    onBatchCoronation,
    onBatchIsolation,
    creatInstanceButtons,
    batchBtnListClick,
    locale,
    handleBatchCleanState,
    handleDependentChain,
    handleCheckTaskDependentChain,
    route
  }
}

export function renderStateCell(state: ITaskState, t: Function) {
  if (!state) return ''
  const stateOption = tasksState(t)[state]
  if (!stateOption) return ''
  const Icon = h(
    NIcon,
    {
      color: stateOption.color,
      class: stateOption.classNames,
      style: {
        display: 'flex'
      },
      size: 20
    },
    () => h(stateOption.icon)
  )
  return h(NTooltip, null, {
    trigger: () => {
      if (!stateOption.isSpin) return Icon
      return h(NSpin, { size: 20 }, { icon: () => Icon })
    },
    default: () => stateOption.desc
  })
}
