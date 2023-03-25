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
import { NButton, NSpace, NTag } from 'naive-ui'
import { taskInstanceList, taskInstanceKill, taskExecute } from '@/service/task'
import { getTableColumn } from '@/common/table'
import type { ResponseTable } from '@/service/types'
import type { JobDetail } from '@/service/task/types'

export function useTable() {
  const { t } = useI18n()

  const state = reactive({
    columns: [],
    tableData: [],
    pageNo: ref(1),
    pageSize: ref(10),
    totalPage: ref(1),
    loading: ref(false),
    name: ref(''),
    showLogModal: ref(false),
    row: ref({})
  })

  const createColumns = (state: any) => {
    state.columns = [
      ...getTableColumn([
        { key: 'instanceId', title: t('tasks.instance_id') },
        { key: 'jobId', title: t('tasks.job_id') }
      ]),
      {
        title: t('tasks.task_name'),
        key: 'instanceName'
      },
      {
        title: t('tasks.state'),
        key: 'status',
        render: (row: JobDetail) => {
          if (row.status === 'SUCCESS') {
            return h(
              NTag,
              { type: 'success' },
              { default: () => t('tasks.success') }
            )
          } else if (row.status === 'FAILED') {
            return h(
              NTag,
              { type: 'error' },
              { default: () => t('tasks.fail') }
            )
          } else if (row.status === 'STOPPED') {
            return h(
              NTag,
              { type: 'warning' },
              { default: () => t('tasks.stop') }
            )
          } else if (row.status === 'RUNNING') {
            return h(
              NTag,
              { type: 'info' },
              { default: () => t('tasks.running') }
            )
          } else {
            return h(
              NTag,
              { type: 'default' },
              { default: () => t('tasks.unknown') }
            )
          }
        }
      },
      {
        title: t('tasks.run_frequency'),
        key: 'runFrequency'
      },
      {
        title: t('tasks.start_time'),
        key: 'startTime'
      },
      {
        title: t('tasks.end_time'),
        key: 'endTime'
      },
      {
        title: t('tasks.operation'),
        key: 'operation',
        render: (row: JobDetail) =>
          h(NSpace, null, {
            default: () => [
              h(
                NButton,
                {
                  text: true,
                  disabled: row.status === 'RUNNING',
                  onClick: () => handleRerun(row)
                },
                { default: () => t('tasks.rerun') }
              ),
              h(
                NButton,
                {
                  text: true,
                  disabled: row.status !== 'RUNNING',
                  onClick: () => handleKill(row)
                },
                { default: () => t('tasks.kill') }
              ),
              h(
                NButton,
                { text: true, onClick: () => handleViewLog(row) },
                { default: () => t('tasks.view_log') }
              )
            ]
          })
      }
    ]
  }

  const getTableData = (params: any) => {
    if (state.loading) return
    state.loading = true
    taskInstanceList({
      pageNo: params.pageNo,
      pageSize: params.pageSize,
      name: params.name
    }).then((res: ResponseTable<Array<JobDetail> | []>) => {
      state.tableData = res.data.data as any
      state.totalPage = res.data.totalPage
      state.loading = false
    })
  }

  const handleKill = (row: JobDetail) => {
    taskInstanceKill(row.instanceId as number).then(() => {
      getTableData({
        pageSize: state.pageSize,
        pageNo: state.pageNo
      })
    })
  }

  const handleViewLog = (row: JobDetail) => {
    state.showLogModal = true
    state.row = row
  }

  const handleRerun = (row: JobDetail) => {
    taskExecute(row.instanceId as number, {
      objectType: 2,
      executeType: 3
    }).then(() => {
      getTableData({
        pageSize: state.pageSize,
        pageNo: state.pageNo
      })
    })
  }

  return {
    state,
    createColumns,
    getTableData
  }
}
