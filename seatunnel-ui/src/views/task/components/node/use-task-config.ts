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
import type { ITaskType } from './types'
import type { ITabPanelType, IHandler } from '@/components/studio/types/tab'

export interface IDefaultTaskType {
  language: string
  paneType?: ITabPanelType
  handlers: IHandler[]
}

export const useTaskConfig = () => {
  const TASK_CONFIG = {
    SHELL: {
      language: 'shell',
      paneType: 'setting',
      handlers: [
        {
          key: 'script',
          name: 'project.node.script'
        }
      ]
    },
    SQL: {
      language: 'sql',
      paneType: 'setting',
      handlers: [
        {
          key: 'script',
          name: 'project.node.script'
        }
      ]
    },
    PYTHON: {
      language: 'python',
      paneType: 'setting',
      handlers: [
        {
          key: 'script',
          name: 'project.node.script'
        }
      ]
    },
    PROCEDURE: {
      language: 'sql',
      paneType: 'setting',
      handlers: [
        {
          key: 'script',
          name: 'project.node.procedure_method'
        }
      ]
    },
    DATAX: {
      language: 'sql',
      paneType: 'setting',
      handlers: [
        {
          key: 'script',
          name: 'project.node.sql_statement'
        }
      ]
    },
    SQOOP: {
      language: 'sql',
      paneType: 'setting',
      handlers: [
        {
          key: 'script',
          name: 'project.node.sql_statement'
        }
      ]
    },
    EMR: {
      language: 'json',
      paneType: 'setting',
      handlers: [
        {
          key: 'script',
          name: 'project.node.emr_flow_define_json'
        }
      ]
    },
    HIVECLI: {
      language: 'sql',
      paneType: 'setting',
      handlers: [
        {
          key: 'script',
          name: 'project.node.hive_sql_script'
        }
      ]
    },
    SEATUNNEL: {
      language: 'shell',
      paneType: 'setting',
      handlers: [
        {
          key: 'script',
          name: 'project.node.script'
        }
      ]
    },
    OPENMLDB: {
      language: 'sql',
      paneType: 'setting',
      handlers: [
        {
          key: 'script',
          name: 'project.node.sql_statement'
        }
      ]
    },
    SAGEMAKER: {
      language: 'json',
      paneType: 'setting',
      handlers: [
        {
          key: 'script',
          name: 'project.node.sagemaker_request_json'
        }
      ]
    },
    CHUNJUN: {
      language: 'json',
      paneType: 'setting',
      handlers: [
        {
          key: 'script',
          name: 'project.node.chunjun_json'
        }
      ]
    }
  } as {
    [key in ITaskType]: {
      language: string
      paneType: ITabPanelType
      handlers: IHandler[]
    }
  }

  const getTaskDefaultConfig = (taskType: ITaskType): IDefaultTaskType => {
    return TASK_CONFIG[taskType] || { paneType: 'setting' }
  }
  return { getTaskDefaultConfig }
}

export const fieldMap = {
  rawScript: 'rawScript',
  SQOOP: 'customShell',
  SQL: 'sql',
  PROCEDURE: 'method',
  DATAX_IF: 'sql',
  DATAX_ELSE: 'json',
  RUN_JOB_FLOW_IF: 'jobFlowDefineJson',
  RUN_JOB_FLOW_ELSE: 'stepsDefineJson',
  OPENMLDB: 'sql',
  SAGEMAKER: 'sagemakerRequestJson',
  PYTORCH: 'script',
  SURVEIL_TYPE2: 'sql',
  HIVECLI: 'hiveSqlScript',
  CHUNJUN: 'json'
}
