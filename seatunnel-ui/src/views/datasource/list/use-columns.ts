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

import { getTableColumn } from '@/common/table'
import { h } from 'vue'
import { useI18n } from 'vue-i18n'

import { useTableOperation } from '@/hooks'
import JsonHighlight from '@/views/datasource/components/json-highlight'
import { EditOutlined } from '@vicons/antd'
import { NButton, NPopover } from 'naive-ui'

export function useColumns(onCallback: Function) {
  const { t } = useI18n()
  const getColumns = () => {
    return [
      ...getTableColumn([{ key: 'id', title: t('datasource.id') }]),
      {
        title: t('datasource.datasource_name'),
        key: 'datasourceName',
        align: 'center'
      },
      {
        title: t('datasource.datasource_user_name'),
        key: 'createUserName',
        align: 'center'
      },
      {
        title: t('datasource.datasource_type'),
        key: 'pluginName',
        width: 180,
        align: 'center'
      },
      {
        title: t('datasource.datasource_parameter'),
        key: 'parameter',
        width: 180,
        align: 'center',
        render: (row: any) => {
          return row.datasourceConfig
            ? h(
                NPopover,
                { trigger: 'click' },
                {
                  trigger: () =>
                    h(
                      NButton,
                      { text: true },
                      {
                        default: () => t('datasource.click_to_view')
                      }
                    ),
                  default: () =>
                    h(JsonHighlight, {
                      params: JSON.stringify({
                        url: row.datasourceConfig.url,
                        driver: row.datasourceConfig.driver,
                        user: row.datasourceConfig.user
                      })
                    })
                }
              )
            : '--'
        }
      },
      {
        title: t('datasource.description'),
        key: 'description',
        align: 'center',
        render: (row: any) => row.description || '-'
      },
      {
        title: t('datasource.create_time'),
        align: 'center',
        key: 'createTime'
      },
      {
        title: t('datasource.update_time'),
        align: 'center',
        key: 'updateTime'
      },
      useTableOperation({
        title: t('datasource.operation'),
        key: 'operation',
        buttons: [
          {
            text: t('datasource.edit'),
            icon: h(EditOutlined),
            onClick: (rowData) => void onCallback(rowData.id, 'edit')
          },
          {
            isDelete: true,
            text: t('datasource.delete'),
            onPositiveClick: (rowData) => void onCallback(rowData.id, 'delete'),
            negativeText: t('datasource.cancel'),
            positiveText: t('datasource.confirm'),
            popTips: t('datasource.delete_confirm')
          }
        ]
      })
    ]
  }

  return {
    getColumns
  }
}
