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

import { defineComponent, onMounted, toRefs } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NInput,
  NPagination,
  NSpace,
  NSelect
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useTable } from './use-table'

const TasksList = defineComponent({
  setup() {
    const { t } = useI18n()
    const { state, createColumns, getTableData } = useTable()

    const requestData = () => {
      getTableData({
        pageSize: state.pageSize,
        pageNo: state.pageNo,
        name: state.name
      })
    }

    const handleSearch = () => {
      state.pageNo = 1
      requestData()
    }

    const handlePageSize = () => {
      state.pageNo = 1
      requestData()
    }

    onMounted(() => {
      createColumns(state)
      requestData()
    })

    return {
      t,
      handleSearch,
      handlePageSize,
      requestData,
      ...toRefs(state)
    }
  },
  render() {
    return (
      <NSpace vertical>
        <NCard title={this.t('tasks.tasks')}>
          {{
            'header-extra': () => (
              <NSpace>
                <NInput
                  clearable
                  v-model={[this.name, 'value']}
                  placeholder={this.t('tasks.task_name')}
                  style={{ width: '200px' }}
                />
                <NButton onClick={this.handleSearch} type='primary'>
                  {this.t('tasks.search')}
                </NButton>
              </NSpace>
            )
          }}
        </NCard>
        <NCard>
          <NSpace vertical>
            <NDataTable
              loading={this.loading}
              columns={this.columns}
              data={this.tableData}
            />
            <NSpace justify='center'>
              <NPagination
                v-model:page={this.pageNo}
                v-model:page-size={this.pageSize}
                page-count={this.totalPage}
                show-size-picker
                page-sizes={[10, 30, 50]}
                show-quick-jumper
                onUpdatePage={this.requestData}
                onUpdatePageSize={this.handlePageSize}
              />
            </NSpace>
          </NSpace>
        </NCard>
      </NSpace>
    )
  }
})

export default TasksList
