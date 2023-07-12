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

import { ref, onMounted, nextTick, Ref, toRef } from 'vue'
import { useI18n } from 'vue-i18n'
import type { IJsonItem } from '../types'
import { useSourceType } from '@/hooks/use-source-type'
import { find } from 'lodash'
import { getResourceList } from '@/service/modules/resources'
import { useRoute } from 'vue-router'

export function useDatasource({
  typeName,
  sourceName,
  model,
  typeField = 'type',
  sourceField = 'datasource',
  span = 12
}: {
  typeName?: string
  sourceName?: string
  model: { [field: string]: any }
  typeField?: string
  sourceField?: string
  span?: Ref<number> | number
}): IJsonItem[] {
  const { t } = useI18n()

  const datasourceOptions = ref([] as { label: string; value: number }[])

  const { state } = useSourceType()
  const route = useRoute()
  const projectCode = Number(route.params.projectCode)

  const options = toRef(state, 'types')

  const refreshOptions = async () => {
    const data = await getResourceList({
      projectCode,
      accessType: 'DATASOURCE',
      resourceType: model[typeField]
    })
    datasourceOptions.value = data.map((item: any) => ({
      label: item.datasourceName,
      value: item.id
    }))
    if (!data.length && model[sourceField]) model[sourceField] = null
    if (data.length && model[sourceField]) {
      const item = find(data, { id: model[sourceField] })
      if (!item) {
        model[sourceField] = null
      }
    }
  }

  const onChange = () => {
    refreshOptions()
  }

  onMounted(async () => {
    await nextTick()
    model[typeField] && refreshOptions()
  })

  return [
    {
      type: 'select',
      field: typeField,
      span,
      name: t(typeName || 'project.node.datasource_type'),
      props: {
        'on-update:value': onChange,
        loading: state.loading
      },
      options,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value) {
          if (!value && value !== 0) {
            return Error(t('project.node.datasource_type'))
          }
        }
      }
    },
    {
      type: 'select',
      field: sourceField,
      span,
      name: t(sourceName || 'project.node.datasource_instances'),
      options: datasourceOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value) {
          if (!value && value !== 0) {
            return Error(t('project.node.datasource_instances'))
          }
        }
      }
    }
  ]
}
