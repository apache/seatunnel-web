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

import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getResourceList } from '@/service/modules/resources'
import { useRoute } from 'vue-router'
import type { IJsonItem } from '../types'

export function useRemoteConnection(
  model: {
    [field: string]: any
  },
  display = ref(true)
): IJsonItem[] {
  const { t } = useI18n()
  const route = useRoute()
  const projectCode = Number(route.params.projectCode)

  const remoteSpan = computed(() => (display.value ? 24 : 0))
  const remoteConnectionSpan = computed(() =>
    model.open && display.value ? 10 : 0
  )
  const sourceSpan = computed(() => (model.open && display.value ? 14 : 0))

  const remoteConnectionOptions = [
    { label: t('project.node.ssh'), value: 'SSH' }
  ]

  const sourceOptions = ref([])

  const getSourceOptions = async () => {
    const data = await getResourceList({
      projectCode,
      accessType: 'DATASOURCE',
      resourceType: 'SSH'
    })
    sourceOptions.value = data.map((item: any) => ({
      label: item.datasourceName,
      value: item.id
    }))
  }

  onMounted(() => getSourceOptions())

  return [
    {
      type: 'switch',
      field: 'open',
      span: remoteSpan,
      name: t('project.node.remote_connection')
    },
    {
      type: 'select',
      field: 'datasourceType',
      name: t('project.node.connection_type'),
      span: remoteConnectionSpan,
      options: remoteConnectionOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (model['open'] && !value) {
            return new Error(t('project.node.connection_type_tips'))
          }
        }
      }
    },
    {
      type: 'select',
      field: 'datasourceId',
      name: t('project.node.source_name'),
      span: sourceSpan,
      options: sourceOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (model['open'] && !value) {
            return new Error(t('project.node.source_name_tips'))
          }
        }
      }
    }
  ]
}
