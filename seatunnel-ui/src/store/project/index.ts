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

export * from './task-node'
export * from './task-type'

import { defineStore } from 'pinia'
import type { ProjectStore } from './types'

export const useProjectStore = defineStore({
  id: 'project',
  state: (): ProjectStore => ({
    currentProject: null,
    projectList: [],
    golbalProject: [],
    globalFlag: false,
    changeProject: false
  }),
  persist: true,
  getters: {
    getCurrentProject(): any {
      if (this.globalFlag) {
        return this.golbalProject
      } else {
        return [this.currentProject]
      }
    },
    getProjects(): any {
      return this.projectList
    },
    getGolbalProject(): Array<number | string> {
      return this.golbalProject
    },
    getGlobalFlag(): boolean {
      return this.globalFlag
    },
    getChangeProject(): boolean {
      return this.changeProject
    }
  },
  actions: {
    setCurrentProject(projectCode: number | string): void {
      this.currentProject = projectCode
    },
    setProjectList(projectList: Array<any>): void {
      this.golbalProject = []
      projectList.forEach((item: any) => {
        this.golbalProject.push(item.code)
      })
      this.projectList = projectList
    },
    setGolbalCode(codeList: Array<any>) {
      this.golbalProject = codeList
    },
    setGlobalFlag(flag: boolean) {
      this.globalFlag = flag
    },
    setChangeProject(flag: boolean) {
      this.changeProject = flag
    }
  }
})
