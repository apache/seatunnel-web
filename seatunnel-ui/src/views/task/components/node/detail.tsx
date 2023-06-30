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

import {
  defineComponent,
  ref,
  watch,
  Ref,
  unref,
  PropType,
  onMounted
} from 'vue'
import Form from '@/components/form'
import { useTask } from './use-task'
import { useTaskNodeStore } from '@/store/project/task-node'
import { formatModel } from './format-data'
import type { ITaskData, EditWorkflowDefinition } from './types'

const props = {
  projectCode: {
    type: Number,
    default: 0
  },
  data: {
    type: Object as PropType<ITaskData>,
    default: { code: 0, taskType: 'SHELL', name: '' }
  },
  readonly: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  from: {
    type: Number as PropType<number>,
    default: 0
  },
  definitionRef: {
    type: Object as PropType<Ref<EditWorkflowDefinition>>
  },
  setting: {
    type: Object as PropType<any>
  }
}

const NodeDetail = defineComponent({
  name: 'NodeDetail',
  emits: ['taskTypeChange', 'valuesChange'],
  props,
  setup(props, { expose, emit }) {
    const taskStore = useTaskNodeStore()

    const formRef = ref()

    const handleUpdateValue = (value: any, field: string) => {
      emit('valuesChange', value, field)
    }
    const { elementsRef, rulesRef, model } = useTask({
      data: props.data,
      projectCode: props.projectCode,
      from: props.from,
      readonly: props.readonly,
      definitionRef: props.definitionRef,
      setting: props.setting,
      updateValue: handleUpdateValue
    })
    watch(
      () => model.taskType,
      async (taskType) => {
        taskStore.updateName(model.name || '')
        emit('taskTypeChange', taskType)
      }
    )

    expose(formRef)

    watch(
      () => props.data,
      () => {
        if (props.data) {
          formRef.value.setValues(
            props.from === 0 ? props.data : formatModel(props.data)
          )
        }
      }
    )

    onMounted(() => {
      formRef.value.setValues(
        props.from === 0 ? props.data : formatModel(props.data)
      )
    })

    return () => (
      <Form
        ref={formRef}
        meta={{
          model,
          rules: rulesRef.value,
          elements: elementsRef.value,
          disabled: unref(props.readonly)
        }}
        layout={{
          xGap: 10
        }}
      />
    )
  }
})

export default NodeDetail
