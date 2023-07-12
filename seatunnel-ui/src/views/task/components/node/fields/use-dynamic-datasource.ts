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
import { useRoute } from 'vue-router'
import { find } from 'lodash'
import type { IJsonItem } from '../types'
import { useSourceType } from '@/hooks/use-source-type'
import { getResourceList } from '@/service/modules/resources'

export function useDynamicDatasource({
  typeName,
  sourceName,
  model,
  typeField = 'type',
  sourceField = 'datasource',
  span = 10
}: {
  typeName?: string
  sourceName?: string
  model: { [field: string]: any }
  supportedDatasourceType?: string[]
  typeField?: string
  sourceField?: string
  span?: Ref<number> | number
}): IJsonItem[] {
  const { t } = useI18n()

  const route = useRoute()
  const projectCode = Number(route.params.projectCode)

  const { state } = useSourceType()
  const options = toRef(state, 'types')

  const datasourceOptions = ref([[]] as Array<
    { label: string; value: number }[]
  >)

  const refreshOptions = async (dataSourceType: string, index = 0) => {
    const data = await getResourceList({
      projectCode,
      accessType: 'DATASOURCE',
      resourceType: dataSourceType
    })
    datasourceOptions.value[index] = data.map((item: any) => ({
      label: item.datasourceName,
      value: item.id
    }))
    if (!data.length && model.dataSourceList[index]) {
      model.dataSourceList[index][sourceField] = null
    }

    if (data.length && model.dataSourceList[index]) {
      const item = find(data, { id: model.dataSourceList[index][sourceField] })
      if (!item) {
        model.dataSourceList[index][sourceField] = null
      }
    }
  }

  const onChange = (dataSourceType: string, index: number) => {
    refreshOptions(dataSourceType, index)
  }

  onMounted(async () => {
    await nextTick()
    model.dataSourceList.map(
      (item: { type: string; datasource: number }, index: number) => {
        refreshOptions(item.type, index)
      }
    )
  })
  return [
    {
      type: 'custom-parameters',
      field: 'dataSourceList',
      name: t('project.node.datasource'),
      children: [
        (i) => {
          return {
            type: 'select',
            field: typeField,
            span,
            props: {
              'on-update:value': (dataSourceType: string) =>
                onChange(dataSourceType, i as number),
              placeholder: t(typeName || 'project.node.datasource_type')
            },
            options
          }
        },
        (i) => {
          return {
            type: 'select',
            field: sourceField,
            span,
            options: datasourceOptions.value[i as number],
            props: {
              placeholder: t(sourceName || 'project.node.datasource_instances')
            },
            validate: {
              trigger: ['input', 'blur'],
              required: true,
              validator() {
                for (const item of model.dataSourceList) {
                  if (!item.type || !item.datasource) {
                    return Error(t('project.node.datasource_require_tips'))
                  }
                }
              }
            }
          }
        }
      ]
    }
  ]
}
