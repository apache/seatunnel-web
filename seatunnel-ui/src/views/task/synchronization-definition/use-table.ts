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

import { useTableOperation } from '@/hooks'
import {
  deleteSyncTaskDefinition,
  executeJob,
  querySyncTaskDefinitionPaging
} from '@/service/sync-task-definition'
import { EditOutlined, PlayCircleOutlined } from '@vicons/antd'
import { h, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { Router } from 'vue-router'
import { useRouter } from 'vue-router'
import type { JobType } from './dag/types'

export function useTable() {
  const { t } = useI18n()
  const router: Router = useRouter()
  const variables = reactive({
    columns: [],
    tableData: [],
    page: ref(1),
    pageSize: ref(10),
    searchName: ref(''),
    totalPage: ref(1),
    showModalRef: ref(false),
    statusRef: ref(0),
    row: {},
    loadingRef: ref(false)
  })

  const JOB_TYPE = {
    DATA_REPLICA: 'whole_library_sync',
    DATA_INTEGRATION: 'data_integration'
  } as { [key in JobType]: string }

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: t(
          'project.synchronization_definition.synchronization_task_name'
        ),
        key: 'name'
      },
      {
        title: t('project.synchronization_definition.business_model'),
        key: 'jobKey',
        render: (row: { jobType: JobType }) =>
          t(`project.synchronization_definition.${JOB_TYPE[row.jobType]}`)
      },
      {
        title: t('project.synchronization_definition.task_describe'),
        key: 'description'
      },
      {
        title: t('project.synchronization_definition.create_user'),
        key: 'createUserName'
      },
      {
        title: t('project.synchronization_definition.create_time'),
        key: 'createTime'
      },
      {
        title: t('project.synchronization_definition.update_user'),
        key: 'updateUserName'
      },
      {
        title: t('project.synchronization_definition.update_time'),
        key: 'updateTime'
      },
      useTableOperation({
        title: t('project.synchronization_definition.operation'),
        key: 'operation',
        buttons: [
          {
            text: t('project.synchronization_definition.edit'),
            onClick: (row: any) => {
              router.push({
                path: `/task/synchronization-definition/${row.id}`
              })
            },
            icon: h(EditOutlined)
          },

          {
            text: t('project.synchronization_definition.start'),
            onClick: (row: any) => void handleRun(row),
            icon: h(PlayCircleOutlined)
          },
          {
            isDelete: true,
            text: t('project.synchronization_definition.delete'),
            onPositiveClick: (row: any) => void handleDelete(row),
            popTips: t('security.token.delete_confirm')
          }
        ]
      })
    ]
  }

  const getTableData = (params: any) => {
    if (variables.loadingRef) return
    variables.loadingRef = true

    querySyncTaskDefinitionPaging(params)
      .then((res: any) => {
        variables.tableData = res.data
        variables.totalPage = res.totalPage
        variables.loadingRef = false
      })
      .catch(() => {
        variables.loadingRef = false
      })
  }

  const handleRun = (row: any) => {
    executeJob(row.id).then(() => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchName: variables.searchName
      })
    })
  }

  const handleDelete = (row: any) => {
    if (variables.tableData.length === 1 && variables.page > 1) {
      --variables.page
    }

    deleteSyncTaskDefinition({
      projectCode: row.projectCode,
      id: row.id
    }).then(() => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchName: variables.searchName
      })
    })
  }

  return {
    variables,
    createColumns,
    getTableData
  }
}
