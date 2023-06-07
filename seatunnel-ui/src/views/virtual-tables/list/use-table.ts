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
import { onMounted, reactive } from 'vue'
import {
  virtualTableList
  //deleteVirtualTable
} from '@/service/virtual-tables'
import { useRoute, useRouter } from 'vue-router'
//import type { Params } from '../types'

export function useTable() {
  const initialParams: any = {
    pluginName: null,
    datasourceName: null
  }
  const state = reactive({
    params: { ...initialParams },
    list: [],
    loading: false,
    page: 1,
    pageSize: 10,
    itemCount: 0
  })
  const route = useRoute()
  const router = useRouter()

  const getList = async () => {
    const result = await virtualTableList({
      pageNo: state.page,
      pageSize: state.pageSize,
      ...state.params
    })
    console.log(result)
    state.list = result?.data
    state.itemCount = result?.total
  }

  const updateList = () => {
    if (state.list.length === 1 && state.page > 1) {
      --state.page
    }
    getList()
  }

  const onDelete = async (id: string) => {
    //await deleteVirtualTable(id)
    updateList()
  }

  const initSearch = () => {
    const { pluginName, datasourceName } = route.query
    if (pluginName) {
      state.params.pluginName = pluginName as string
      if (datasourceName) {
        state.params.datasourceName = datasourceName as string
      }
    }
  }

  const onSearch = () => {
    const query = (
      state.params.pluginName ? { pluginName: state.params.pluginName } : null
    ) as any

    if (state.params.datasourceName) {
      query.datasourceName = state.params.datasourceName
    }

    if (query) {
      router.replace({ query: { tab: route.query.tab, ...query } })
    }

    state.page = 1
    getList()
  }

  const onPageChange = (page: number) => {
    state.page = page
    getList()
  }

  const onPageSizeChange = (pageSize: number) => {
    state.page = 1
    state.pageSize = pageSize
    getList()
  }

  onMounted(() => {
    initSearch()
    onSearch()
  })

  return {
    state,
    onSearch,
    onDelete,
    onPageChange,
    onPageSizeChange
  }
}
