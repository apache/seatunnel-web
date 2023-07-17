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

import { reactive, ref, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { NSpace, NButton } from 'naive-ui'
import { userList, userDelete, userEnable, userDisable } from '@/service/user'
import { getTableColumn } from '@/common/table'
import type { ResponseTable } from '@/service/types'
import type { UserDetail } from '@/service/user/types'

export function useTable() {
  const { t } = useI18n()
  const state = reactive({
    columns: [],
    tableData: [],
    pageNo: ref(1),
    pageSize: ref(10),
    totalPage: ref(1),
    row: {},
    loading: ref(false),
    showFormModal: ref(false),
    showDeleteModal: ref(false),
    status: ref(0)
  })

  const createColumns = (state: any) => {
    state.columns = [
      ...getTableColumn([{ key: 'id', title: t('user_manage.id') }]),
      {
        title: t('user_manage.username'),
        key: 'name'
      },
      {
        title: t('user_manage.create_time'),
        key: 'createTime'
      },
      {
        title: t('user_manage.update_time'),
        key: 'updateTime'
      },
      {
        title: t('user_manage.operation'),
        key: 'operation',
        render: (row: UserDetail) =>
          h(NSpace, null, {
            default: () => [
              h(
                NButton,
                { text: true, onClick: () => handleStatus(row) },
                {
                  default: () =>
                    row.status === 1
                      ? t('user_manage.enable')
                      : t('user_manage.disable')
                }
              ),
              h(
                NButton,
                { text: true, onClick: () => handleEdit(row) },
                { default: () => t('user_manage.edit') }
              ),
              h(
                NButton,
                { text: true, onClick: () => handleDelete(row) },
                { default: () => t('user_manage.delete') }
              )
            ]
          })
      }
    ]
  }

  const handleStatus = (row: UserDetail) => {
    const req = row.status === 1 ? userEnable : userDisable
    req(row.id as number).then(() => {
      getTableData({
        pageSize: state.pageSize,
        pageNo: state.pageNo
      })
    })
  }

  const handleEdit = (row: UserDetail) => {
    state.showFormModal = true
    state.status = 1
    state.row = row
  }

  const handleDelete = (row: UserDetail) => {
    state.showDeleteModal = true
    state.row = row
  }

  const handleConfirmDeleteModal = () => {
    if (state.tableData.length === 1 && state.pageNo > 1) {
      --state.pageNo
    }

    userDelete((state.row as UserDetail).id as number).then(() => {
      state.showDeleteModal = false
      getTableData({
        pageSize: state.pageSize,
        pageNo: state.pageNo
      })
    })
  }

  const getTableData = (params: any) => {
    if (state.loading) return
    state.loading = true
    userList({ ...params }).then(
      (res: ResponseTable<Array<UserDetail> | []>) => {
        state.tableData = res.data as any
        state.totalPage = res.data.totalPage
        state.loading = false
      }
    )
  }

  return { state, createColumns, getTableData, handleConfirmDeleteModal }
}
