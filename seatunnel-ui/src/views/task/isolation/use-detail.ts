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
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  parseIsolationFile,
  submitIsolationTasks,
  IIsolationTaskRecord
} from '@/service/modules/isolation-task'
import type { UploadCustomRequestOptions, FileInfo } from './types'

interface DetailState {
  status: 'process' | 'wait' | 'error' | 'finish'
  current: 1 | 2
  saving: boolean
  file?: FileInfo
  tasks: IIsolationTaskRecord[]
}

export const useDetail = (projectCode: number) => {
  const { t } = useI18n()

  const formRef = ref()
  const rules = {
    file: {
      trigger: ['blur'],
      required: true,
      validator: () => {
        if (state.file) return
        return Error(t('project.isolation.file_tips'))
      }
    }
  }
  const state = reactive({
    current: 1,
    status: 'process',
    saving: false,
    tasks: []
  } as DetailState)

  const parseFile = async () => {
    try {
      await formRef.value.validate()
      if (!state.file?.file) return
      const formData = new FormData()
      formData.append('file', state.file.file)
      const result = await parseIsolationFile(formData, projectCode)
      state.tasks = result
      state.current = 2
      return true
    } catch (err) {}
  }

  const submitTasks = async () => {
    await submitIsolationTasks(state.tasks, projectCode)
    return true
  }

  const onFileChange = (options: UploadCustomRequestOptions) => {
    if (options.file.status !== 'pending') {
      state.file = undefined
      formRef.value.validate()
      return
    }
    state.file = options.file
    formRef.value.validate()
  }

  const onReset = () => {
    state.current = 1
    state.status = 'process'
    state.saving = false
    state.tasks = []
  }

  const onConfirm = async (step?: number) => {
    if (step === -1) {
      state.current = 1
      return false
    }
    if (state.current === 1) {
      parseFile()
      return false
    }
    if (state.current === 2) {
      return await submitTasks()
    }
  }

  return {
    state,
    formRef,
    rules,
    onReset,
    onConfirm,
    onFileChange
  }
}
