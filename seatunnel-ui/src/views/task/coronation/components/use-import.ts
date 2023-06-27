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
import { reactive, SetupContext, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { UploadCustomRequestOptions, useMessage } from 'naive-ui'
import { useRouter } from 'vue-router'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import type { Router } from 'vue-router'
import {
  importTaskCoronation,
  parseTaskCoronation,
  submitTaskCoronation
} from '@/service/modules/task-coronation'
import _ from 'lodash'
import { uuid } from '@/common/common'

export function useImport(
  ctx: SetupContext<('cancelModal' | 'confirmModal')[]>
) {
  const { t } = useI18n()
  const message = useMessage()
  const router: Router = useRouter()

  const formRef = ref()
  const rules = {
    file: {
      trigger: ['blur'],
      required: true,
      validator: () => {
        if (variables.file) return
        return Error(t('project.isolation.file_tips'))
      }
    }
  }

  const variables = reactive({
    columns: [],
    tableWidth: DefaultTableWidth,
    tableData: [] as any,
    treeData: [] as any,
    currentStep: 1,
    file: null as any,
    saving: false,
    projectCode: Number(router.currentRoute.value.params.projectCode),
    coronationTask: {} as { [key: string]: Array<string> },
    coronationMore: false,
    upstreamMore: false
  })

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: t('project.task.workflow_instance_name'),
        key: 'workflowInstanceName',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.task.task_name'),
        key: 'taskName',
        ...COLUMN_WIDTH_CONFIG['note']
      }
    ]

    if (variables.tableWidth) {
      variables.tableWidth = calculateTableWidth(variables.columns)
    }
  }

  const nextStep = async () => {
    if (variables.currentStep === 1) await handleImportCoronation()
    if (variables.currentStep === 2) await handleCoronationList()
    if (variables.currentStep === 3) await handleSubmitCoronation()
    if (variables.currentStep < 3) variables.currentStep++
  }

  const prevStep = () => {
    if (variables.currentStep > 1) {
      variables.coronationMore = false
      variables.upstreamMore = false
      variables.currentStep--
    }
  }

  const handleImportCoronation = async () => {
    if (variables.saving) return
    variables.saving = true
    try {
      await formRef.value.validate()
      const formData = new FormData()
      formData.append('file', variables.file.file)
      variables.tableData = await importTaskCoronation(
        formData,
        variables.projectCode
      )
    } finally {
      variables.saving = false
    }
  }

  const formatTreeData = (data: any) => {
    data.map((item: any) => {
      item.key = uuid(item.taskCode + '_')
      if (item.upstreamTasks) {
        item.upstreamTasks.map(
          (item: any) => (item.key = uuid(item.taskCode + '_'))
        )
      }
    })
  }

  const initDefaultCheckedKeys = () => {
    for (let i = 0; i < variables.treeData.length; i++) {
      if (variables.treeData[i].upstreamTasks) {
        const keys = []
        for (let j = 0; j < variables.treeData[i].upstreamTasks.length; j++) {
          keys.push(variables.treeData[i].upstreamTasks[j].key)
        }
        variables.coronationTask[variables.treeData[i].key] = keys
      }
    }
  }

  const handleCoronationList = async () => {
    variables.treeData = await parseTaskCoronation(variables.projectCode, {
      coronationTasks: variables.tableData
    })
    formatTreeData(variables.treeData)
    initDefaultCheckedKeys()
  }

  const handleSubmitCoronation = async () => {
    const coronationReq = [] as any
    for (let i = 0; i < variables.treeData.length; i++) {
      const task = variables.treeData[i]
      const coronationTask = _.omit(task, ['upstreamTasks', 'key'])
      coronationTask.upstreamTasks = []
      for (let j = 0; j < task.upstreamTasks.length; j++) {
        if (
          variables.coronationTask[task.key].includes(task.upstreamTasks[j].key)
        ) {
          coronationTask.upstreamTasks.push({
            ..._.omit(task.upstreamTasks[j], ['key'])
          })
        }
      }
      coronationReq.push(coronationTask)
    }
    await submitTaskCoronation(variables.projectCode, {
      coronationTasks: coronationReq
    })
    message.success(t('project.task.success'))
    ctx.emit('confirmModal')
  }

  const onFileChange = (options: UploadCustomRequestOptions) => {
    if (options.file.status !== 'pending') {
      variables.file = undefined
      formRef.value.validate()
      return
    }
    variables.file = options.file
    formRef.value.validate()
  }

  return {
    rules,
    variables,
    formRef,
    createColumns,
    nextStep,
    prevStep,
    handleImportCoronation,
    onFileChange
  }
}
