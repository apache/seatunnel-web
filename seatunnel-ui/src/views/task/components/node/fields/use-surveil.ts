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
import { computed, h, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomParams, useRemoteConnection } from '.'
import { useDatasource } from './use-datasource'
import { NIcon } from 'naive-ui'
import { QuestionCircleOutlined } from '@vicons/antd'
import styles from '../index.module.scss'
import type { IJsonItem } from '../types'

export function useSurveil(
  model: { [field: string]: any },
  updateValue?: (value: any, field: string) => void
): IJsonItem[] {
  const { t, locale } = useI18n()
  const filePathSpan = computed(() => (model.scanType === 1 ? 24 : 0))
  const dataSourceSpan = computed(() => (model.scanType === 2 ? 12 : 0))
  const kafkaSpan = computed(() => (model.scanType === 3 ? 24 : 0))
  const hdfsPathSpan = computed(() => (model.scanType === 4 ? 24 : 0))
  const remoteDisplay = computed(() => model['scanType'] === 1)

  watch(
    () => model.scanType,
    () => {
      const handlers =
        model.scanType === 2
          ? [{ key: 'script', name: 'project.node.sql_statement' }]
          : []

      if (updateValue) {
        updateValue(
          {
            script: '',
            ...model,
            handlers,
            language: 'sql'
          },
          'batch'
        )
      }
    }
  )

  return [
    {
      type: 'select',
      field: 'scanType',
      name: t('project.node.scan_type'),
      options: SCAN_TYPES,
      span: 12
    },
    {
      type: 'input-number',
      field: 'interval',
      name: t('project.node.scan_interval'),
      span: 12,
      slots: {
        suffix: () => t('project.node.second')
      },
      validate: {
        trigger: ['input', 'trigger'],
        validator(unuse, value) {
          if (value < 1 || !Number.isInteger(value)) {
            return Error(t('project.node.positive_integer_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'filePath',
      name: t('project.node.file_path'),
      span: filePathSpan,
      props: {
        placeholder: t('project.node.file_path_tips')
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        validator(unuse, value) {
          if (filePathSpan.value && !value) {
            return Error(t('project.node.file_path_tips'))
          }
        }
      }
    },
    ...useRemoteConnection(model, remoteDisplay),
    {
      type: 'input',
      field: 'filePath',
      name: t('project.node.file_path'),
      span: hdfsPathSpan,
      props: {
        placeholder: t('project.node.file_path_tips')
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        validator(unuse, value) {
          if (hdfsPathSpan.value && !value) {
            return Error(t('project.node.file_path_tips'))
          }
        }
      }
    },
    ...useDatasource({
      model,
      span: dataSourceSpan
    }),
    {
      type: 'input',
      field: 'topic',
      name: t('project.node.topic_name'),
      props: {
        placeholder: t('project.node.topic_name_tips')
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        validator(unuse, value) {
          if (kafkaSpan.value && !value) {
            return Error(t('project.node.topic_name_tips'))
          }
        }
      },
      span: kafkaSpan
    },
    {
      type: 'input',
      field: 'offsetTime',
      name: h('div', null, [
        t('project.node.offset_time'),
        h(
          NIcon,
          {
            size: 20,
            class: styles['question-icon'],
            onClick: () => {
              window.open(
                `https://dolphinscheduler.apache.org/${
                  locale.value === 'en_US' ? 'en-us' : 'zh-cn'
                }/docs/latest/user_doc/guide/parameter/built-in.html`
              )
            }
          },
          () => h(QuestionCircleOutlined)
        )
      ]),
      props: {
        placeholder: t('project.node.offset_time_required_tips')
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        validator(unuse, value) {
          if (kafkaSpan.value && !value) {
            return Error(t('project.node.offset_time_required_tips'))
          }
          if (
            !/^\${.+?}$/.test(value) &&
            !/^\$\[.+?\]$/.test(value) &&
            !/^((19|20)[0-9]{2})((0[1-9])|(1[0-2]))((0[1-9])|((1|2)[0-9])|(3[0-1]))$/.test(
              value
            )
          ) {
            return Error(t('project.node.offset_time_incorrect_tips'))
          }
        }
      },
      span: kafkaSpan
    },
    {
      type: 'input',
      field: 'bootstrapServers',
      name: t('project.node.bootstrap_servers'),
      props: {
        placeholder: t('project.node.bootstrap_servers_tips')
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        validator(unuse, value) {
          if (kafkaSpan.value && !value) {
            return Error(t('project.node.bootstrap_servers_tips'))
          }
        }
      },
      span: kafkaSpan
    },
    {
      type: 'input',
      field: 'groupId',
      name: t('project.node.group_id'),
      props: {
        placeholder: t('project.node.group_id_tips')
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        validator(unuse, value) {
          if (kafkaSpan.value && !value) {
            return Error(t('project.node.group_id_tips'))
          }
        }
      },
      span: kafkaSpan
    },
    {
      type: 'input',
      field: 'keyDeserializer',
      name: t('project.node.key_deserializer'),
      props: {
        placeholder: t('project.node.key_deserializer_tips')
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        validator(unuse, value) {
          if (kafkaSpan.value && !value) {
            return Error(t('project.node.key_deserializer_tips'))
          }
        }
      },
      span: kafkaSpan
    },
    {
      type: 'input',
      field: 'valueDeserializer',
      name: t('project.node.value_deserializer'),
      props: {
        placeholder: t('project.node.value_deserializer_tips')
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        validator(unuse, value) {
          if (kafkaSpan.value && !value) {
            return Error(t('project.node.value_deserializer_tips'))
          }
        }
      },
      span: kafkaSpan
    },
    ...useCustomParams({ model, field: 'localParams', isSimple: false })
  ]
}

export const SCAN_TYPES = [
  {
    value: 1,
    label: 'FILE'
  },
  {
    value: 2,
    label: 'SQL'
  },
  {
    value: 3,
    label: 'KAFKA'
  },
  {
    value: 4,
    label: 'HDFS'
  }
]

export const STATUS_TYPES = [
  {
    value: 0,
    label: 'SUBMITTED_SUCCESS'
  },
  {
    value: 1,
    label: 'RUNNING_EXECUTION'
  },
  {
    value: 2,
    label: 'READY_PAUSE'
  },
  {
    value: 3,
    label: 'PAUSE'
  },
  {
    value: 4,
    label: 'READY_STOP'
  },
  {
    value: 5,
    label: 'STOP'
  },
  {
    value: 6,
    label: 'FAILURE'
  },
  {
    value: 7,
    label: 'SUCCESS'
  },
  {
    value: 8,
    label: 'NEED_FAULT_TOLERANC'
  },
  {
    value: 9,
    label: 'KILL'
  },
  {
    value: 10,
    label: 'WAITING_THREAD'
  },
  {
    value: 11,
    label: 'WAITING_DEPEND'
  },
  {
    value: 12,
    label: 'DELAY_EXECUTION'
  },
  {
    value: 13,
    label: 'FORCED_SUCCESS'
  },
  {
    value: 14,
    label: 'SERIAL_WAIT'
  },
  {
    value: 15,
    label: 'READY_BLOCK'
  },
  {
    value: 16,
    label: 'BLOCK'
  },
  {
    value: 17,
    label: 'DISPATCH'
  },
  {
    value: 18,
    label: 'PAUSE_BY_ISOLATION'
  },
  {
    value: 19,
    label: 'KILL_BY_ISOLATION'
  },
  {
    value: 20,
    label: 'PAUSE_BY_CORONATION'
  },
  {
    value: 21,
    label: 'FORBIDDEN_BY_CORONATION'
  }
]
