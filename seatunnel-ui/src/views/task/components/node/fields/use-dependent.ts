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

import { ref, onMounted, watch, h, VNode, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Router, useRouter } from 'vue-router'
import { useRelationCustomParams, useDependentTimeout } from '.'
import { useTaskNodeStore } from '@/store/project/task-node'
import { NIcon, NPopover } from 'naive-ui'
import { QuestionCircleOutlined } from '@vicons/antd'
import { queryProjectCreatedAndAuthorizedByUser } from '@/service/modules/projects'
import {
  queryAllByProjectCode,
  getTasksByDefinitionCode, queryProcessDefinitionListByProjectCode
} from '@/service/modules/process-definition'
import { tasksState } from '@/common/common'
import styles from '../index.module.scss'
import type {
  IJsonItem,
  IDependpendItem,
  IDependTask,
  ITaskState,
  IDateType
} from '../types'
import { getResourceList } from '@/service/modules/resources'
import { useRoute } from 'vue-router'

export function useDependent(
  model: { [field: string]: any },
  setting: any
): IJsonItem[] {
  const { t } = useI18n()

  const router: Router = useRouter()
  const nodeStore = useTaskNodeStore()

  const cardRef = ref(t('project.node.cart_null_tips'))
  const route = useRoute()
  const projectCode = Number(route.params.projectCode)

  const dependentResult = nodeStore.getDependentResult
  const TasksStateConfig = tasksState(t)

  const projectList = ref([] as { label: string; value: number }[])
  const processCache = {} as {
    [key: number]: { label: string; value: number }[]
  }
  const taskCache = {} as {
    [key: number]: { label: string; value: number }[]
  }

  const CYCLE_TYPE = [
    {
      value: 'NATURAL',
      label: t('project.node.natural')
    },
    {
      value: 'BUSINESS',
      label: t('project.node.business')
    }
  ]

  const CYCLE_LIST = [
    {
      value: 'month',
      label: t('project.node.month')
    },
    {
      value: 'week',
      label: t('project.node.week')
    },
    {
      value: 'day',
      label: t('project.node.day')
    },
    {
      value: 'hour',
      label: t('project.node.hour')
    }
  ]
  const DATE_LSIT = {
    hour: [
      {
        value: 'currentHour',
        label: t('project.node.current_hour')
      },
      {
        value: 'last1Hour',
        label: t('project.node.last_1_hour')
      },
      {
        value: 'last2Hours',
        label: t('project.node.last_2_hour')
      },
      {
        value: 'last3Hours',
        label: t('project.node.last_3_hour')
      },
      {
        value: 'last24Hours',
        label: t('project.node.last_24_hour')
      }
    ],
    day: [
      {
        value: 'today',
        label: t('project.node.today')
      },
      {
        value: 'last1Days',
        label: t('project.node.last_1_days')
      },
      {
        value: 'last2Days',
        label: t('project.node.last_2_days')
      },
      {
        value: 'last3Days',
        label: t('project.node.last_3_days')
      },
      {
        value: 'last7Days',
        label: t('project.node.last_7_days')
      }
    ],
    week: [
      {
        value: 'thisWeek',
        label: t('project.node.this_week')
      },
      {
        value: 'lastWeek',
        label: t('project.node.last_week')
      },
      {
        value: 'lastMonday',
        label: t('project.node.last_monday')
      },
      {
        value: 'lastTuesday',
        label: t('project.node.last_tuesday')
      },
      {
        value: 'lastWednesday',
        label: t('project.node.last_wednesday')
      },
      {
        value: 'lastThursday',
        label: t('project.node.last_thursday')
      },
      {
        value: 'lastFriday',
        label: t('project.node.last_friday')
      },
      {
        value: 'lastSaturday',
        label: t('project.node.last_saturday')
      },
      {
        value: 'lastSunday',
        label: t('project.node.last_sunday')
      }
    ],
    month: [
      {
        value: 'thisMonth',
        label: t('project.node.this_month')
      },
      {
        value: 'thisMonthBegin',
        label: t('project.node.this_month_begin')
      },
      {
        value: 'lastMonth',
        label: t('project.node.last_month')
      },
      {
        value: 'lastMonthBegin',
        label: t('project.node.last_month_begin')
      },
      {
        value: 'lastMonthEnd',
        label: t('project.node.last_month_end')
      }
    ]
  } as { [key in IDateType]: { value: string; label: string }[] }

  const getProjectList = async () => {
    const result = await queryProjectCreatedAndAuthorizedByUser()
    projectList.value = result.map((item: { code: number; name: string }) => ({
      value: item.code,
      label: item.name
    }))
    return projectList
  }
  const getProcessList = async (code: number) => {
    if (processCache[code]) {
      return processCache[code]
    }
    const result = await queryProcessDefinitionListByProjectCode(code)
    const processList = result.map(
      (item: { code: number; name: string }) => ({
        value: item.code,
        label: item.name
      })
    )
    processCache[code] = processList

    return processList
  }

  const getTaskList = async (code: number, processCode: number) => {
    if (taskCache[processCode]) {
      return taskCache[processCode]
    }
    const result = await getTasksByDefinitionCode(code, processCode)
    const taskList = result.map((item: { code: number; name: string }) => ({
      value: item.code,
      label: item.name
    }))
    taskList.unshift({
      value: 0,
      label: 'ALL'
    })
    taskCache[processCode] = taskList
    return taskList
  }

  const renderState = (item: {
    definitionCode: number
    depTaskCode: number
    cycle: string
    dateValue: string
  }) => {
    if (!item || router.currentRoute.value.name !== 'workflow-instance-detail')
      return null
    const key = `${item.definitionCode}-${item.depTaskCode}-${item.cycle}-${item.dateValue}`
    const state: ITaskState = dependentResult[key] || 'WAITING_THREAD'
    return h(NIcon, { size: 24, color: TasksStateConfig[state].color }, () =>
      h(TasksStateConfig[state].icon)
    )
  }

  const renderStrategyLabelExtra = (value: 0 | 1): VNode => {
    return h(
      NPopover,
      { trigger: 'hover' },
      {
        trigger: () =>
          h(NIcon, { size: 20, class: styles['question-icon'] }, () =>
            h(QuestionCircleOutlined)
          ),
        default: () =>
          value === 0
            ? t('project.node.failure_continue_tips')
            : t('project.node.failure_wait_tips')
      }
    )
  }

  const initDependTaskList = (dependTaskList: IDependTask[]) => {
    dependTaskList.forEach((item: IDependTask) => {
      if (!item.dependItemList?.length) return
      item.dependItemList?.forEach(async (dependItem: IDependpendItem) => {
        if (dependItem.projectCode) {
          dependItem.definitionCodeOptions = await getProcessList(
            dependItem.projectCode
          )
        }
        if (dependItem.projectCode && dependItem.definitionCode) {
          dependItem.depTaskCodeOptions = await getTaskList(
            dependItem.projectCode,
            dependItem.definitionCode
          )
        }
        if (dependItem.cycle) {
          dependItem.dateOptions = DATE_LSIT[dependItem.cycle]
        }
      })
    })
  }

  const initProjectCard = async () => {
    if (!setting || !setting.cardCode) {
      cardRef.value = t('project.node.cart_null_tips')
      return
    }

    const cards =
      (await getResourceList({
        projectCode,
        accessType: 'DATA_CARD'
      })) || []
    const card = cards.filter(
      (item: { cardCode: number }) => item.cardCode === setting.cardCode
    )
    cardRef.value = card.length > 0 ? card[0].cardValue : ' '
  }

  onMounted(() => {
    getProjectList()
    initDependTaskList(model.dependTaskList)
    initProjectCard()
  })

  watch(
    () => setting.cardCode,
    () => initProjectCard()
  )

  watch(
    () => model.dependTaskList,
    (value) => initDependTaskList(value)
  )

  return [
    ...useDependentTimeout(model),
    ...useRelationCustomParams({
      model,
      children: (i = 0) => ({
        type: 'custom-parameters',
        field: 'dependItemList',
        span: 18,
        children: [
          (j = 0) => ({
            type: 'select',
            field: 'projectCode',
            name: t('project.node.project_name'),
            span: 24,
            props: {
              filterable: true,
              onUpdateValue: async (projectCode: number) => {
                const item = model.dependTaskList[i].dependItemList[j]
                item.definitionCodeOptions = await getProcessList(projectCode)
                item.depTaskCode = null
                item.definitionCode = null
              }
            },
            options: projectList,
            path: `dependTaskList.${i}.dependItemList.${j}.projectCode`,
            rule: {
              required: true,
              trigger: ['input', 'blur'],
              validator(validate: any, value: string) {
                if (!value) {
                  return Error(t('project.node.project_name_tips'))
                }
              }
            }
          }),
          (j = 0) => ({
            type: 'select',
            field: 'definitionCode',
            span: 24,
            name: t('project.node.process_name'),
            props: {
              filterable: true,
              onUpdateValue: async (processCode: number) => {
                const item = model.dependTaskList[i].dependItemList[j]
                item.depTaskCodeOptions = await getTaskList(
                  item.projectCode,
                  processCode
                )
                item.depTaskCode = 0
              }
            },
            options:
              model.dependTaskList[i]?.dependItemList[j]
                ?.definitionCodeOptions || [],
            path: `dependTaskList.${i}.dependItemList.${j}.definitionCode`,
            rule: {
              required: true,
              trigger: ['input', 'blur'],
              validator(validate: any, value: string) {
                if (!value) {
                  return Error(t('project.node.process_name_tips'))
                }
              }
            }
          }),
          (j = 0) => ({
            type: 'select',
            field: 'depTaskCode',
            span: 24,
            name: t('project.node.task_name'),
            props: {
              filterable: true
            },
            options:
              model.dependTaskList[i]?.dependItemList[j]?.depTaskCodeOptions ||
              [],
            path: `dependTaskList.${i}.dependItemList.${j}.depTaskCode`,
            rule: {
              required: true,
              trigger: ['input', 'blur'],
              validator(validate: any, value: number) {
                if (!value && value !== 0) {
                  return Error(t('project.node.task_name_tips'))
                }
              }
            }
          }),
          (j = 0) => ({
            type: 'radio',
            field: 'timeType',
            span: 24,
            value: 'NATURAL',
            props: {
              onUpdateValue: (value: 'NATURAL' | 'BUSINESS') => {
                if (value === 'BUSINESS') {
                  model.dependTaskList[i].dependItemList[j].cycle = 'day'
                  model.dependTaskList[i].dependItemList[j].dateValue = null
                  model.dependTaskList[i].dependItemList[j].dateOptions =
                    DATE_LSIT['day']
                } else {
                  model.dependTaskList[i].dependItemList[j].cycle = null
                  model.dependTaskList[i].dependItemList[j].dateValue = null
                  model.dependTaskList[i].dependItemList[j].dateOptions = []
                }
              }
            },
            options: CYCLE_TYPE,
            path: `dependTaskList.${i}.dependItemList.${j}.timeType`,
            rule: {
              required: true,
              trigger: ['input', 'blur'],
              validator(validate: any, value: string) {
                if (!value) {
                  return Error(t('project.node.cycle_time_tips'))
                }
              }
            }
          }),
          (j = 0) => ({
            type: 'input',
            field: 'cardValue',
            props: {
              disabled: true,
              defaultValue: cardRef
            },
            span: computed(() =>
              model.dependTaskList[i].dependItemList[j].timeType === 'BUSINESS'
                ? 24
                : 0
            ),
            name: t('project.node.card'),
            value: cardRef
          }),
          (j = 0) => ({
            type: 'select',
            field: 'cycle',
            span: 10,
            name: t('project.node.cycle_time'),
            props: {
              disabled: computed(
                () =>
                  model.dependTaskList[i] &&
                  model.dependTaskList[i].dependItemList[j] &&
                  model.dependTaskList[i].dependItemList[j].timeType ===
                    'BUSINESS'
              ).value,
              onUpdateValue: (value: IDateType) => {
                model.dependTaskList[i].dependItemList[j].dateOptions =
                  DATE_LSIT[value]
                model.dependTaskList[i].dependItemList[j].dateValue = null
              }
            },
            options: CYCLE_LIST,
            path: `dependTaskList.${i}.dependItemList.${j}.cycle`,
            rule: {
              required: false,
              trigger: ['input', 'blur'],
              validator(validate: any, value: string) {
                if (!value) {
                  return Error(t('project.node.cycle_time_tips'))
                }
              }
            }
          }),
          (j = 0) => ({
            type: 'select',
            field: 'dateValue',
            span: 10,
            name: ' ',
            options:
              model.dependTaskList[i]?.dependItemList[j]?.dateOptions || [],
            path: `dependTaskList.${i}.dependItemList.${j}.dateValue`,
            rule: {
              trigger: ['input', 'blur'],
              validator(validate: any, value: string) {
                if (!value) {
                  return Error(t('project.node.date_tips'))
                }
              }
            }
          }),
          (j = 0) => ({
            type: 'custom',
            field: 'state',
            span: 2,
            name: ' ',
            widget: renderState(model.dependTaskList[i]?.dependItemList[j])
          })
        ]
      }),
      childrenField: 'dependItemList',
      name: 'add_dependency'
    }),
    {
      type: 'radio',
      field: 'dependStrategy',
      name: t('project.node.dependency_strategy'),
      props: {},
      options: [
        {
          label:
            t('project.node.failure') + ' -> ' + t('project.node.continue'),
          value: 'failure-continue',
          extra: renderStrategyLabelExtra(0)
        },
        {
          label: t('project.node.failure') + ' -> ' + t('project.node.wait'),
          value: 'failure-waiting',
          extra: renderStrategyLabelExtra(1)
        }
      ]
    }
  ]
}
