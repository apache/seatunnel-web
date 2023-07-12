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

import { computed, onMounted, ref, h, VNode, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { IJsonItem } from '../types'
import { getAllNotLockCards } from '@/service/modules/card'
import { NIcon, NPopover } from 'naive-ui'
import { QuestionCircleOutlined } from '@vicons/antd'
import styles from '../index.module.scss'
import {LocationQueryValue} from "vue-router";

export function useNextLoop(
    model: { [p: string]: any },
    projectCode?: number,
    processDefinitionCode?: number,
    updateValue?: (value: any, field: string) => void
): IJsonItem[] {
  const { t } = useI18n()
  const loading = ref(false)
  const cardOptions = ref([])
  const customSpan = computed(() => (model.customConfig ? 24 : 0))

  const getAllNotLockCardList = async () => {
    if (loading.value) return
    loading.value = true
    const params = {
      projectCode,
      processDefinitionCode: Number.isNaN(processDefinitionCode)? 0 : processDefinitionCode,
    }
    const res = await getAllNotLockCards(params)
    cardOptions.value = res.map(
      (item: { cardCode: string; cardName: string; cardValue: string }) => ({
        label: `${item.cardName} ${item.cardValue}`,
        value: item.cardCode
      })
    )
    loading.value = false
  }

  const renderStrategyLabelExtra = (): VNode => {
    return h(
      NPopover,
      { trigger: 'hover' },
      {
        trigger: () =>
          h(NIcon, { size: 20, class: styles['question-icon'] }, () =>
            h(QuestionCircleOutlined)
          ),
        default: () => [
          t('project.node.next_loop_date_tip'),
          h('br'),
          t('project.node.next_loop_timezone_tip')
        ]
      }
    )
  }

  onMounted(() => {
    getAllNotLockCardList()
  })

  const updateScriptAndLanguage = () => {
    const handlers = model.customConfig
      ? [{ key: 'script', name: t('project.node.script') }]
      : []
    if (updateValue) {
      updateValue(
        {
          ...model,
          handlers,
          language: model.programType === 'SHELL' ? 'shell' : 'python'
        },
        'batch'
      )
    }
  }

  watch(() => model.customConfig, updateScriptAndLanguage)
  watch(() => model.programType, updateScriptAndLanguage)

  const programTypeOptions = [
    {
      label: 'SHELL',
      value: 'SHELL'
    },
    {
      label: 'PYTHON',
      value: 'PYTHON',
      extra: renderStrategyLabelExtra()
    }
  ]
  return [
    {
      type: 'select',
      field: 'cardCode',
      span: 12,
      name: t('project.node.next_loop_card'),
      props: {
        loading: loading
      },
      options: cardOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value: number) {
          if (!value) {
            return Error(t('project.node.next_loop_card_tips'))
          }
        }
      }
    },
    {
      type: 'switch',
      field: 'customConfig',
      name: t('project.node.next_loop_custom_rule')
    },
    {
      type: 'radio',
      field: 'programType',
      span: customSpan,
      name: t('project.node.script_type'),
      options: programTypeOptions
    }
  ]
}
