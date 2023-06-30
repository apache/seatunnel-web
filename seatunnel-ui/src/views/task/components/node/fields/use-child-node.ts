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

import { ref, onMounted, h } from 'vue'
import { NIcon, NButton } from 'naive-ui'
import { BranchesOutlined } from '@vicons/antd'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import {
  querySimpleList,
  queryProcessDefinitionByCode
} from '@/service/modules/process-definition'
import { querySubProcessInstanceByTaskCode } from '@/service/modules/process-instances'
import styles from '../index.module.scss'
import type { IJsonItem } from '../types'

export function useChildNode({
  model,
  projectCode,
  from,
  processName,
  code,
  isCreate
}: {
  model: { [field: string]: any }
  projectCode: number
  from?: number
  processName?: number
  code?: number
  isCreate: boolean
}): IJsonItem {
  const { t } = useI18n()
  const router = useRouter()
  const route = useRoute()

  const options = ref([] as { label: string; value: string }[])
  const loading = ref(false)

  const getProcessList = async () => {
    if (loading.value) return
    loading.value = true
    const res = await querySimpleList(projectCode)
    options.value = res
      .filter((option: { name: string; code: number }) => option.code !== code)
      .map((option: { name: string; code: number }) => ({
        label: option.name,
        value: option.code
      }))
    loading.value = false
  }
  const getProcessListByCode = async (processCode: number) => {
    if (!processCode) return
    const res = await queryProcessDefinitionByCode(processCode)
    model.definition = res
  }

  const handleSubProcessClick = () => {
    if (router.currentRoute.value.name === 'workflow-definition-detail') {
      router.push({
        name: 'workflow-definition-detail',
        params: { code: model.processDefinitionCode },
        query: { project: route.query.project, global: route.query.global }
      })
      return
    }
    if (model.instanceId) {
      querySubProcessInstanceByTaskCode(
        { taskId: model.instanceId },
        { projectCode: projectCode }
      ).then((res: any) => {
        router.push({
          name: 'workflow-instance-detail',
          params: { id: res.subProcessInstanceId },
          query: { code: model.processDefinitionCode }
        })
      })
    }
  }

  onMounted(() => {
    if (from === 1 && processName) {
      getProcessListByCode(processName)
    }
    getProcessList()
  })

  return {
    type: 'select',
    field: 'processDefinitionCode',
    span: 24,
    name: h('div', null, [
      t('project.node.child_node'),
      !isCreate
        ? h(
            NButton,
            {
              onClick: handleSubProcessClick,
              quaternary: true,
              circle: true,
              type: 'info',
              size: 'tiny',
              attrType: 'button'
            },
            { icon: () => h(NIcon, { size: 16 }, () => h(BranchesOutlined)) }
          )
        : null
    ]),
    props: {
      loading: loading
    },
    options: options,
    class: 'select-child-node',
    validate: {
      trigger: ['input', 'blur'],
      required: true,
      validator(unuse: any, value: number) {
        if (!value) {
          return Error(t('project.node.child_node_tips'))
        }
      }
    }
  }
}
