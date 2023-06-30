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
import { computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useResources, useCustomParams } from '.'
import type { IJsonItem } from '../types'

export function useSeaTunnel(
  model: { [field: string]: any },
  updateValue?: (value: any, field: string) => void
): IJsonItem[] {
  const { t } = useI18n()

  const resourceEditorSpan = computed(() => (model.useCustom ? 0 : 24))

  watch(
    () => model.useCustom,
    () => {
      const handlers = model.useCustom
        ? [{ key: 'script', name: t('project.node.script') }]
        : []
      if (updateValue) {
        updateValue(
          {
            ...model,
            handlers,
            language: 'script'
          },
          'batch'
        )
      }
    }
  )

  return [
    {
      type: 'select',
      field: 'engine',
      span: 12,
      name: t('project.node.engine'),
      options: ENGINE,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        message: t('project.node.engine_tips')
      }
    },
    {
      type: 'input',
      field: 'others',
      name: t('project.node.option_parameters'),
      span: 24,
      props: {
        type: 'textarea',
        placeholder: t('project.node.option_parameters_tips')
      }
    },

    // SeaTunnel config parameter
    {
      type: 'switch',
      field: 'useCustom',
      name: t('project.node.custom_config')
    },

    useResources(resourceEditorSpan, true, 1),
    ...useCustomParams({ model, field: 'localParams', isSimple: true })
  ]
}

export const ENGINE = [
  {
    label: 'SEATUNNEL',
    value: 'SEATUNNEL'
  }
]
