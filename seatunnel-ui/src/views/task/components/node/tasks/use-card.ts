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

import { reactive } from 'vue'
import * as Fields from '../fields/index'
import type { IJsonItem, INodeData, ITaskData } from '../types'
import { useRouter } from 'vue-router'

export function useCard({
  projectCode,
  from = 0,
  readonly,
  data,
  updateValue
}: {
  projectCode: number
  from?: number
  readonly?: boolean
  data?: ITaskData
  updateValue?: (value: any, field: string) => void
}) {
  const router = useRouter()
  const workflowCode =
    router.currentRoute.value.params.code ||
    router.currentRoute.value.query.code
  const model = reactive({
    taskType: 'NEXT_LOOP',
    name: '',
    flag: 'YES',
    description: '',
    timeoutFlag: false,
    timeoutNotifyStrategy: ['WARN'],
    timeout: 30,
    customConfig: false,
    programType: 'SHELL',
    localParams: [],
    environmentCode: null,
    workerGroup: 'default',
    delayTime: 0,
    rawScript: '',
    cardCode: null
  } as INodeData)

  let extra: IJsonItem[] = []
  if (from === 1) {
    extra = [
      Fields.useTaskType(model, readonly),
      Fields.useProcessName({
        model,
        projectCode,
        isCreate: !data?.id,
        from,
        processName: data?.processName
      })
    ]
  }

  return {
    json: [
      Fields.useName(from),
      ...extra,
      Fields.useRunFlag(),
      Fields.useDescription(),
      Fields.useTaskPriority(),
      Fields.useWorkerGroup(),
      Fields.useEnvironmentName(model, !data?.id),
      ...Fields.useTaskGroup(model, projectCode),
      Fields.useDelayTime(model),
      ...Fields.useTimeoutAlarm(model),
      ...Fields.useNextLoop(model, projectCode, Number(workflowCode), updateValue),
      Fields.usePreTasks(model)
    ] as IJsonItem[],
    model
  }
}
