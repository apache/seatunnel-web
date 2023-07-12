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

import { ref, onMounted, computed, h } from 'vue'
import { useI18n } from 'vue-i18n'
import styles from '../index.module.scss'
import type { IJsonItem } from '../types'
import { getResourceList } from '@/service/modules/resources'
import { useRoute } from 'vue-router'

export function useSqlUtils(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const emailSpan = computed(() => (model.sendEmail ? 24 : 0))
  const groups = ref([])
  const groupsLoading = ref(false)

  const route = useRoute()
  const projectCode = Number(route.params.projectCode)

  const getGroups = async () => {
    if (groupsLoading.value) return
    groupsLoading.value = true
    const res = await getResourceList({
      accessType: 'ALERT_GROUP',
      projectCode
    })
    groups.value = res.map((item: { id: number; groupName: string }) => ({
      label: item.groupName,
      value: item.id
    }))
    groupsLoading.value = false
  }

  onMounted(() => {
    getGroups()
  })

  return [
    {
      type: 'select',
      field: 'displayRows',
      span: 6,
      name: t('project.node.log_display'),
      options: DISPLAY_ROWS,
      props: {
        filterable: true,
        tag: true
      },
      validate: {
        trigger: ['input', 'blur'],
        validator(unuse, value) {
          if (!/^\+?[1-9][0-9]*$/.test(value)) {
            return new Error(t('project.node.integer_tips'))
          }
        }
      }
    },
    {
      type: 'custom',
      field: 'displayRowsTips',
      span: 6,
      widget: h(
        'div',
        { class: styles['display-rows-tips'] },
        t('project.node.rows_of_result')
      )
    },
    {
      type: 'switch',
      field: 'sendEmail',
      span: 6,
      name: t('project.node.send_email')
    },
    {
      type: 'input',
      field: 'title',
      name: t('project.node.title'),
      props: {
        placeholder: t('project.node.title_tips')
      },
      span: emailSpan,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse, value) {
          if (model.sendEmail && !value)
            return new Error(t('project.node.title_tips'))
        }
      }
    },
    {
      type: 'select',
      field: 'groupId',
      name: t('project.node.alarm_group'),
      options: groups,
      span: emailSpan,
      props: {
        loading: groupsLoading,
        placeholder: t('project.node.alarm_group_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse, value) {
          if (model.sendEmail && !value)
            return new Error(t('project.node.alarm_group_tips'))
        }
      }
    }
  ]
}

const DISPLAY_ROWS = [
  {
    label: '1',
    value: 1
  },
  {
    label: '10',
    value: 10
  },
  {
    label: '25',
    value: 25
  },
  {
    label: '50',
    value: 50
  },
  {
    label: '100',
    value: 100
  }
]
