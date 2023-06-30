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
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomParams } from '.'
import type { IJsonItem } from '../types'
import { querySeaTunnelList } from '@/service/modules/seatunnel'

export function useWhaleTunnel(
  model: { [field: string]: any },
  projectCode: number
): IJsonItem[] {
  const { t } = useI18n()
  const loading = ref(false)
  const options = ref(
    [] as {
      label: string
      value: string
    }[]
  )

  const getWhaleTunnelList = async () => {
    if (loading.value) return
    loading.value = true
    const res = await querySeaTunnelList({
      projectCode
    })
    options.value = []
    res.map((item: any) => {
      options.value.push({
        label: `${item.syncTaskType}-${item.taskName}`,
        value: item.taskId
      })
    })

    loading.value = false
  }

  onMounted(() => getWhaleTunnelList())

  return [
    {
      type: 'select',
      field: 'taskId',
      name: t('project.node.task_name'),
      span: 22,
      props: {
        loading: loading,
        filterable: true
      },
      options,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value: number) {
          if (!value) {
            return Error(t('project.node.whale_seatunnel_task_tips'))
          }
        }
      }
    },
    {
      type: 'switch',
      field: 'breakContinue',
      name: t('project.node.break_continue')
    },
    ...useCustomParams({ model, field: 'localParams', isSimple: false })
  ]
}
