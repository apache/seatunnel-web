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

import { defineComponent, reactive, toRefs } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import {
  NBreadcrumb,
  NBreadcrumbItem,
  NButton,
  NCard,
  NIcon,
  NInput,
  NSpace,
  NTooltip
} from 'naive-ui'
import { BulbOutlined } from '@vicons/antd'
import { scriptAdd } from '@/service/script'
import MonacoEditor from '@/components/monaco-editor'
import type { Router } from 'vue-router'

const DataPipesCreate = defineComponent({
  setup() {
    const { t } = useI18n()
    const router: Router = useRouter()
    const variables = reactive({
      name: '',
      type: 0,
      content: ''
    })

    const handleClickDataPipes = () => {
      router.push({ path: '/data-pipes/list' })
    }

    const handleAdd = () => {
      scriptAdd(variables).then(() => {
        handleClickDataPipes()
      })
    }

    return {
      t,
      ...toRefs(variables),
      handleClickDataPipes,
      handleAdd
    }
  },
  render() {
    return (
      <NSpace vertical>
        <NCard>
          {{
            header: () => (
              <NSpace align='center'>
                <NBreadcrumb>
                  <NBreadcrumbItem onClick={this.handleClickDataPipes}>
                    {this.t('data_pipes.data_pipes')}
                  </NBreadcrumbItem>
                  <NBreadcrumbItem>
                    {this.t('data_pipes.create')}
                  </NBreadcrumbItem>
                </NBreadcrumb>
              </NSpace>
            ),
            'header-extra': () => (
              <NSpace>
                <NButton secondary onClick={this.handleClickDataPipes}>
                  {this.t('data_pipes.cancel')}
                </NButton>
                <NButton secondary type='success' onClick={this.handleAdd}>
                  {this.t('data_pipes.save')}
                </NButton>
              </NSpace>
            )
          }}
        </NCard>
        <NCard>
          <NSpace align='center'>
            <span>{this.t('data_pipes.name')}</span>
            <NSpace align='center'>
              <NInput
                clearable
                maxlength='100'
                showCount
                style={{ width: '600px' }}
                v-model={[this.name, 'value']}
              />
              <NTooltip placement='right' trigger='hover'>
                {{
                  default: () => <span>{this.t('data_pipes.name_tips')}</span>,
                  trigger: () => (
                    <NIcon size='20' style={{ cursor: 'pointer' }}>
                      <BulbOutlined />
                    </NIcon>
                  )
                }}
              </NTooltip>
            </NSpace>
          </NSpace>
        </NCard>
        <NCard>
          <MonacoEditor v-model={[this.content, 'value']} />
        </NCard>
      </NSpace>
    )
  }
})

export default DataPipesCreate
