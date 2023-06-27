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

import { defineComponent, onMounted, toRefs, watch } from 'vue'
import {
  NSpace,
  NCard,
  NButton,
  NInput,
  NIcon,
  NDataTable,
  NPagination
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { SearchOutlined, ReloadOutlined } from '@vicons/antd'
import { useTable } from './use-table'
import { TaskModal } from './task-modal'
import ProjectSelector from '@/views/projects/components/projectSelector'
// import { useProjectStore } from '@/store/project'
import { useRoute, useRouter } from 'vue-router'
import _ from 'lodash'

const SynchronizationDefinition = defineComponent({
  name: 'SynchronizationDefinition',
  setup() {
    const { t } = useI18n()
    const route = useRoute()
    const router = useRouter()
    const { variables, createColumns, getTableData } = useTable()

    const requestData = () => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchName: variables.searchName
      })
    }

    const onUpdatePageSize = () => {
      variables.page = 1
      requestData()
    }

    const onCancelModal = () => {
      variables.showModalRef = false
    }

    const onConfirmModal = () => {
      variables.showModalRef = false
      requestData()
    }

    const handleModalChange = () => {
      variables.showModalRef = true
    }

    const onSearch = () => {
      variables.page = 1

      const query = {} as any
      if (variables.searchName) {
        query.searchName = variables.searchName
      }

      router.replace({
        query: !_.isEmpty(query)
          ? {
              ...query,
              ...route.query,
              searchProjectCode: variables.projectCodes
            }
          : {
              ...route.query,
              searchProjectCode: variables.projectCodes
            }
      })
      requestData()
    }

    const onReset = () => {
      variables.searchName = ''
      // variables.projectCodes = useProjectStore().getCurrentProject
    }

    const handleKeyup = (event: KeyboardEvent) => {
      if (event.key === 'Enter') {
        onSearch()
      }
    }

    const initSearch = () => {
      const { searchName } = route.query
      if (searchName) {
        variables.searchName = searchName as string
      }
    }

    onMounted(() => {
      initSearch()
      createColumns(variables)
      requestData()
    })

    watch(useI18n().locale, () => {
      createColumns(variables)
    })
    const getProjectCodeList = (codes: any) => {
      if (!codes) {
        // variables.projectCodes = useProjectStore().getGolbalProject
      } else {
        // variables.projectCodes = [codes]
      }
    }

    return {
      t,
      ...toRefs(variables),
      onUpdatePageSize,
      requestData,
      onCancelModal,
      onConfirmModal,
      handleModalChange,
      onSearch,
      onReset,
      getProjectCodeList,
      handleKeyup,
    }
  },
  render() {
    return (
      <NSpace vertical>
        <NCard>
          <NSpace justify='space-between' itemStyle={{ flexGrow: 1 }}>
            <NButton
              type='info'
              onClick={this.handleModalChange}
            >
              {this.t(
                'project.synchronization_definition.create_synchronization_task'
              )}
            </NButton>
            <NSpace justify='end'>
              {/* {this.globalProject && (
                <ProjectSelector
                  initCode={
                    this.projectCodes.length == 1 ? this.projectCodes[0] : null
                  }
                  onGetprojectList={this.getProjectCodeList}
                ></ProjectSelector>
              )} */}
              <NInput
                clearable
                v-model={[this.searchName, 'value']}
                placeholder={this.t(
                  'project.synchronization_definition.task_name'
                )}
                onKeyup={this.handleKeyup}
              />
              <NButton onClick={this.onReset}>
                <NIcon>
                  <ReloadOutlined />
                </NIcon>
              </NButton>
              <NButton type='primary' onClick={this.onSearch}>
                <NIcon>
                  <SearchOutlined />
                </NIcon>
              </NButton>
            </NSpace>
          </NSpace>
        </NCard>
        <NCard>
          <NSpace vertical>
            <NDataTable
              loading={this.loadingRef}
              columns={this.columns}
              data={this.tableData}
            />
            <NSpace justify='center'>
              <NPagination
                v-model:page={this.page}
                v-model:page-size={this.pageSize}
                page-count={this.totalPage}
                show-size-picker
                page-sizes={[10, 30, 50]}
                show-quick-jumper
                onUpdatePage={this.requestData}
                onUpdatePageSize={this.onUpdatePageSize}
              />
            </NSpace>
          </NSpace>
        </NCard>
        <TaskModal
          showModalRef={this.showModalRef}
          onCancelModal={this.onCancelModal}
          onConfirmModal={this.onConfirmModal}
        />
      </NSpace>
    )
  }
})

export default SynchronizationDefinition
