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
import { NButton, NSpace, NSwitch } from 'naive-ui'
import { taskJobList, taskExecute, taskRecycle } from '@/service/task'
import type { ResponseTable } from '@/service/types'
import type { JobDetail } from '@/service/task/types'

export function useTable() {
  const { t } = useI18n()

  const state = reactive({
    columns: [],
    tableData: [{}],
    name: ref(null),
    pageNo: ref(1),
    pageSize: ref(10),
    totalPage: ref(1),
    loading: ref(false)
  })

  const createColumns = (state: any) => {
    state.columns = [
      {
        title: t('jobs.data_pipe_name'),
        key: 'datapipeName'
      },
      {
        title: t('jobs.plan'),
        key: 'jobPlan'
      },
      {
        title: t('jobs.create_date'),
        key: 'createTime'
      },
      {
        title: t('jobs.publish'),
        key: 'publish',
        render: (row: JobDetail) =>
          h(NSwitch, {
            round: false,
            defaultValue: row.publish,
            disabled: true
          })
      },
      {
        title: t('jobs.operation'),
        key: 'operation',
        render: (row: JobDetail) =>
          h(NSpace, null, {
            default: () => [
              h(
                NButton,
                {
                  text: true,
                  disabled: row.jobStatus === 'OFFLINE',
                  onClick: () => handleExecute(row.jobId)
                },
                t('jobs.execute')
              ),
              h(
                NButton,
                {
                  text: true,
                  disabled: row.jobStatus === 'OFFLINE',
                  onClick: () => handleRecycle(row.jobId)
                },
                t('jobs.recycle')
              )
            ]
          })
      }
    ]
  }

  const getTableData = (params: any) => {
    if (state.loading) return
    state.loading = true
    taskJobList({ ...params }).then(
      (res: ResponseTable<Array<JobDetail> | []>) => {
        state.tableData = res.data.data
        state.totalPage = res.data.totalPage
        state.loading = false
      }
    )
  }

  const handleExecute = (id: number) => {
    taskExecute(id, {
      objectType: 1
    }).then(() => {
      getTableData({
        pageSize: state.pageSize,
        pageNo: state.pageNo
      })
    })
  }

  const handleRecycle = (id: number) => {
    taskRecycle(id).then(() => {
      getTableData({
        pageSize: state.pageSize,
        pageNo: state.pageNo
      })
    })
  }

  return { state, createColumns, getTableData }
}
