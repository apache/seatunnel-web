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

import { h, ref, watch, onMounted } from 'vue'
import { ClearOutlined } from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import { useTableOperation } from '@/hooks'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import { renderStateCell } from '../instance/use-table'
import { renderTableTime } from '@/common/common'
import type { TableColumns, IRecord } from './types'
import { useTableLink } from '@/hooks'
import { changeProject } from '../../utils/changeProject'
import { useRoute, useRouter } from 'vue-router'

export function useColumns(onCallback: Function) {
  const { t } = useI18n()
  const columns = ref()
  const tableWidth = ref(DefaultTableWidth)
  const route = useRoute()
  const router = useRouter()
  const getColumns = (): TableColumns => {
    const columns = [
      {
        type: 'selection',
        className: 'btn-selected',
        ...COLUMN_WIDTH_CONFIG['selection']
      },
      {
        title: t('project.isolation.task_name'),
        key: 'taskName',
        ...COLUMN_WIDTH_CONFIG['name']
      },
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
                project: String(row.projectCode),
                global: 'false'
              }
            })
          }
        }
      }),
      {
        title: t('project.isolation.operation_status'),
        key: 'taskStatus',
        ...COLUMN_WIDTH_CONFIG['state'],
        render: (rowData: IRecord) => renderStateCell(rowData.taskStatus, t)
      },
      {
        title: t('project.isolation.workflow_instance_name'),
        key: 'workflowInstanceName',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.isolation.create_time'),
        key: 'createTime',
        ...COLUMN_WIDTH_CONFIG['time'],
        render: (row: IRecord) => renderTableTime(row.createTime)
      },
      useTableOperation(
        {
          title: t('project.isolation.operation'),
          key: 'operation',
          buttons: [
            {
              text: t('project.isolation.cancel_isolation'),
              permission: 'project:isolation-task:cancel',
              icon: h(ClearOutlined),
              onClick: (rowData) => void onCallback(rowData, 'cancel')
            }
          ]
        },
        'project'
      )
    ] as TableColumns
    tableWidth.value = calculateTableWidth(columns)
    return columns
  }

  watch(useI18n().locale, () => {
    columns.value = getColumns()
  })

  onMounted(() => {
    columns.value = getColumns()
  })

  return {
    columns
  }
}
