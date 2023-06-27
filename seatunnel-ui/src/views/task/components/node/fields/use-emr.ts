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
import { useCustomParams } from '.'
import type { IJsonItem } from '../types'
import {useI18n} from "vue-i18n";

export function useEmr(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  return [
    {
      type: 'select',
      field: 'programType',
      span: 24,
      name: t('project.node.program_type'),
      options: PROGRAM_TYPES,
      validate: {
        required: true
      }
    },
    ...useCustomParams({ model, field: 'localParams', isSimple: false })
  ]
}

export const PROGRAM_TYPES = [
  {
    label: 'RUN_JOB_FLOW',
    value: 'RUN_JOB_FLOW'
  },
  {
    label: 'ADD_JOB_FLOW_STEPS',
    value: 'ADD_JOB_FLOW_STEPS'
  }
]
