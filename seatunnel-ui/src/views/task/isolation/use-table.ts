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

import { reactive, onMounted } from 'vue'
import {
  queryIsolationTasks,
  cancleIsolation
} from '@/service/modules/isolation-task'
import type { IRecord } from './types'
import { useProjectStore } from '@/store/project'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import _ from 'lodash'
export const useTable = () => {
  const { t } = useI18n()
  const projectStore = useProjectStore()
  const route = useRoute()
  const router = useRouter()

  const state = reactive({
    page: 1,
    pageSize: 10,
    totalPage: 1,
    taskName: '',
    workflowInstanceName: '',
    list: [] as IRecord[],
    loading: false,
    checkedRowKeys: [],
    projectCode:
      route.query.searchProjectCode || projectStore.getCurrentProject,
    globalProject: projectStore.getGlobalFlag
  })
  const getList = async () => {
    if (
      state.loading ||
      !state.projectCode ||
      state.projectCode.length === 0 ||
      typeof state.projectCode[0] === 'undefined'
    )
      return
    state.loading = true
    try {
      const result = await queryIsolationTasks({
        pageNo: state.page,
        pageSize: state.pageSize,
        taskName: state.taskName,
        workflowInstanceName: state.workflowInstanceName,
        projectCodes: state.projectCode
      })
      state.list = result.totalList
      state.totalPage = result.totalPage
    } catch (err) {}
    state.loading = false
  }
  const onSearch = () => {
    state.page = 1

    const query = {} as any
    if (state.taskName) {
      query.taskName = state.taskName
    }

    if (state.workflowInstanceName) {
      query.workflowInstanceName = state.workflowInstanceName
    }

    router.replace({
      query: !_.isEmpty(query)
        ? {
            ...query,
            project: route.query.project,
            global: route.query.global,
            searchProjectCode: state.projectCode
          }
        : {
            ...route.query,
            searchProjectCode: state.projectCode
          }
    })

    getList()
  }
  const onChangePage = (page: number) => {
    state.page = page
    getList()
  }
  const onChangePageSize = (pageSize: number) => {
    state.page = 1
    state.pageSize = pageSize
    getList()
  }
  const onUpdateList = () => {
    if (state.list.length === 1 && state.page > 1) {
      --state.page
    }
    getList()
  }

  const onCancleIsolation = () => {
    cancleIsolation({ isolationTaskIds: state.checkedRowKeys }).then(() => {
      window.$message.success(t('project.workflow.success'))
      if (state.list.length === 1 && state.page > 1) {
        state.page -= 1
      }
      getList()
    })
  }

  const onCancel = async (id: number) => {
    await cancleIsolation({ isolationTaskIds: id })
    onUpdateList()
  }

  const onCallback = (rowData: IRecord, type: string) => {
    if (type === 'cancel') {
      onCancel(rowData.id)
      return
    }
  }

  const initSearch = () => {
    const { taskName, workflowInstanceName } = route.query
    if (taskName) {
      state.taskName = taskName as string
    }

    if (workflowInstanceName) {
      state.workflowInstanceName = workflowInstanceName as string
    }
  }

  onMounted(() => {
    initSearch()
    onUpdateList()
  })

  return {
    state,
    onSearch,
    onChangePage,
    onChangePageSize,
    onUpdateList,
    onCallback,
    onCancleIsolation
  }
}
