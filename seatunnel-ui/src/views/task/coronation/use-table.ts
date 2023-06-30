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

import { reactive, h, withDirectives } from 'vue'
import { NButton, NIcon, NPopconfirm, NSpace, NSpin, NTooltip } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { RollbackOutlined } from '@vicons/antd'
import { useRouter, useRoute } from 'vue-router'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import { useLocalesStore } from '@/store/locales/locales'
import {
  queryTaskCoronationListPaging,
  cancleCoronation
} from '@/service/modules/task-coronation'
import { useTableLink } from '@/hooks'
import type { Router } from 'vue-router'
import { permission } from '@/directives/permission'
import { tasksState } from '@/common/common'
import type { ITaskState } from '@/common/types'
import type { RowKey } from 'naive-ui/lib/data-table/src/interface'
import { useProjectStore } from '@/store/project'
import { changeProject } from '../../utils/changeProject'

export function useTable() {
  const { t } = useI18n()
  const router: Router = useRouter()
  const route = useRoute()
  const projectStore = useProjectStore()

  const handleImport = () => {
    variables.showModalRef = true
  }

  const handleCancel = (row: any) => {
    cancleCoronation({ coronationTaskIds: [row.id] }).then(() => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo:
          variables.tableData.length === 1 && variables.page > 1
            ? variables.page - 1
            : variables.page,
        taskName: variables.taskName,
        workflowInstanceName: variables.workflowInstanceName
      })
    })
  }

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        type: 'selection',
        className: 'btn-selected',
        ...COLUMN_WIDTH_CONFIG['selection']
      },
      {
        title: '#',
        key: 'index',
        render: (row: any, index: number) => index + 1,
        ...COLUMN_WIDTH_CONFIG['index']
      },
      {
        title: t('project.task.task_name'),
        key: 'taskName',
        ...COLUMN_WIDTH_CONFIG['note']
      },
      useTableLink({
        title: t('project.project_name'),
        key: 'projectName',
        ...COLUMN_WIDTH_CONFIG['name'],
        button: {
          onClick: (row: any) => {
            changeProject(row.projectCode)
            router.push({
              path: route.path,
              query: {
                project: String(row.projectCode),
                global: 'false'
              }
            })
          }
        }
      }),
      {
        title: t('project.task.run_state'),
        key: 'taskStatus',
        ...COLUMN_WIDTH_CONFIG['state'],
        render: (row: any) => renderStateCell(row.taskStatus, t)
      },
      {
        title: t('project.task.workflow_instance_name'),
        key: 'workflowInstanceName',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('project.task.create_time'),
        key: 'createTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('project.task.operation'),
        key: 'actions',
        ...COLUMN_WIDTH_CONFIG['operation'](1),
        render(row: any) {
          return h(NSpace, null, {
            default: () => [
              h(
                NPopconfirm,
                {
                  onPositiveClick: () => {
                    handleCancel(row)
                  }
                },
                {
                  trigger: () =>
                    h(
                      NTooltip,
                      {},
                      {
                        trigger: () =>
                          withDirectives(
                            h(
                              NButton,
                              {
                                circle: true,
                                type: 'info',
                                size: 'small'
                              },
                              {
                                icon: () =>
                                  h(NIcon, null, {
                                    default: () => h(RollbackOutlined)
                                  })
                              }
                            ),
                            [[permission, 'project:coronation-task:cancel']]
                          ),
                        default: () => t('project.task.cancel_coronation')
                      }
                    ),
                  default: () => t('project.task.cancel_confirm')
                }
              )
            ]
          })
        }
      }
    ]
    if (variables.tableWidth) {
      variables.tableWidth = calculateTableWidth(variables.columns)
    }
  }

  const onCancleCoronation = () => {
    cancleCoronation({ coronationTaskIds: variables.checkedRowKeys }).then(
      () => {
        window.$message.success(t('project.workflow.success'))
        if (variables.tableData.length === 1 && variables.page > 1) {
          variables.page -= 1
        }
        getTableData({
          pageSize: variables.pageSize,
          pageNo: variables.page,
          taskName: variables.taskName,
          workflowInstanceName: variables.workflowInstanceName
        })
      }
    )
  }

  const variables = reactive({
    columns: [],
    tableWidth: DefaultTableWidth,
    tableData: [],
    page: 1,
    pageSize: 10,
    searchVal: null,
    totalPage: 1,
    showModalRef: false,
    statusRef: 0,
    row: {},
    loadingRef: false,
    checkedRowKeys: [] as Array<RowKey>,
    projectCodes:
      route.query.searchProjectCode || projectStore.getCurrentProject,
    globalProject: projectStore.getGlobalFlag,
    taskName: null as null | string,
    workflowInstanceName: null as null | string
  })

  const getTableData = (params: any) => {
    if (
      variables.loadingRef ||
      !variables.projectCodes ||
      variables.projectCodes.length === 0 ||
      typeof variables.projectCodes[0] === 'undefined'
    )
      return
    variables.loadingRef = true
    params.projectCodes = variables.projectCodes
    queryTaskCoronationListPaging({ ...params })
      .then((res: any) => {
        variables.tableData = res.totalList.map((item: any, unused: number) => {
          return {
            ...item
          }
        })
        variables.totalPage = res.totalPage
      })
      .finally(() => {
        variables.loadingRef = false
      })
  }

  const downloadTemplate = () => {
    const localesStore = useLocalesStore()
    window.location.href = `/dolphinscheduler/${
      localesStore.getLocales === 'zh_CN'
        ? 'coronation-task-excel-template-cn'
        : 'coronation-task-excel-template-en'
    }.xlsx`
  }

  function renderStateCell(state: ITaskState, t: Function) {
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

  return {
    variables,
    getTableData,
    createColumns,
    downloadTemplate,
    handleImport,
    onCancleCoronation
  }
}
