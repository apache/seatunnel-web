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
import { defineStore } from 'pinia'
import type { TaskTypeState, ITaskType, TaskType, ITaskTypeItem } from './types'

export const TASK_TYPES_MAP = {
  SHELL: {
    alias: 'SHELL',
    helperLinkDisable: true
  },
  INFORMATICA: {
    alias: 'INFORMATICA',
    helperLinkDisable: true
  },
  SUB_PROCESS: {
    alias: 'SUB_PROCESS',
    helperLinkDisable: true
  },
  PROCEDURE: {
    alias: 'PROCEDURE',
    helperLinkDisable: true
  },
  SQL: {
    alias: 'SQL',
    helperLinkDisable: true
  },
  SPARK: {
    alias: 'SPARK',
    helperLinkDisable: true
  },
  FLINK: {
    alias: 'FLINK',
    helperLinkDisable: true
  },
  MR: {
    alias: 'MapReduce',
    helperLinkDisable: true
  },
  PYTHON: {
    alias: 'PYTHON',
    helperLinkDisable: true
  },
  DEPENDENT: {
    alias: 'DEPENDENT',
    helperLinkDisable: true
  },
  HTTP: {
    alias: 'HTTP',
    helperLinkDisable: true
  },
  DATAX: {
    alias: 'DataX',
    helperLinkDisable: true
  },
  PIGEON: {
    alias: 'PIGEON',
    helperLinkDisable: true
  },
  SQOOP: {
    alias: 'SQOOP',
    helperLinkDisable: true
  },
  CONDITIONS: {
    alias: 'CONDITIONS',
    taskDefinitionDisable: true,
    helperLinkDisable: true
  },
  DATA_QUALITY: {
    alias: 'DATA_QUALITY',
    helperLinkDisable: true
  },
  SWITCH: {
    alias: 'SWITCH',
    taskDefinitionDisable: true,
    helperLinkDisable: true
  },
  SEATUNNEL: {
    alias: 'SeaTunnel',
    helperLinkDisable: true
  },
  WHALE_SEATUNNEL: {
    alias: 'Whale_SeaTunnel'
  },
  EMR: {
    alias: 'AmazonEMR',
    helperLinkDisable: true
  },
  SURVEIL: {
    alias: 'TRIGGER',
    helperLinkDisable: true
  },
  NEXT_LOOP: {
    alias: 'NEXT_LOOP',
    helperLinkDisable: true
  },
  HIVECLI: {
    alias: 'HIVECLI',
    helperLinkDisable: true
  },
  DINKY: {
    alias: 'DINKY',
    helperLinkDisable: true
  },
  CHUNJUN: {
    alias: 'CHUNJUN',
    helperLinkDisable: true
  },
  ZEPPELIN: {
    alias: 'ZEPPELIN',
    helperLinkDisable: true
  },
  JUPYTER: {
    alias: 'JUPYTER',
    helperLinkDisable: true
  },
  MLFLOW: {
    alias: 'MLFLOW',
    helperLinkDisable: true
  },
  OPENMLDB: {
    alias: 'OPENMLDB',
    helperLinkDisable: true
  },
  DVC: {
    alias: 'DVC',
    helperLinkDisable: true
  },
  SAGEMAKER: {
    alias: 'SageMaker',
    helperLinkDisable: true
  },
  PYTORCH: {
    alias: 'PYTORCH',
    helperLinkDisable: true
  },
  JAR: {
    alias: 'JAR',
    helperLinkDisable: true
  }
} as { [key in TaskType]: ITaskType }

export const useTaskTypeStore = defineStore({
  id: 'project-task-type',
  state: (): TaskTypeState => ({
    types: []
  }),
  persist: true,
  getters: {
    getTaskType(): ITaskTypeItem[] {
      return this.types
    }
  },
  actions: {
    setTaskTypes(types: TaskType[]): void {
      try {
        this.types = types
          .filter((type) => !!TASK_TYPES_MAP[type])
          .map((type) => ({ ...TASK_TYPES_MAP[type], type }))
      } catch (err) {
        this.types = []
      }
    }
  }
})
