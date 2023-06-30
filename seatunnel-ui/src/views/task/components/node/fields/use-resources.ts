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

import { ref, onMounted, Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import utils from '@/utils'
import { getResourceList } from '@/service/modules/resources'
import { useRoute } from 'vue-router'
import type { IJsonItem, IResource } from '../types'

export function useResources(
  span: number | Ref<number> = 24,
  required = false,
  limit = -1
): IJsonItem {
  const { t } = useI18n()
  const route = useRoute()
  const projectCode = Number(route.params.projectCode)
  const resourcesOptions = ref([] as IResource[])
  const resourcesLoading = ref(false)

  const getResources = async () => {
    if (resourcesLoading.value) return
    resourcesLoading.value = true
    // const res = await queryFileAndGitList({ types: 'FILE,GIT' })
    const file = await getResourceList({
      projectCode,
      accessType: 'FILE',
      resourceType: 'FILE'
    })

    const git = await getResourceList({
      projectCode,
      accessType: 'GIT_RESOURCE',
      resourceType: 'GIT'
    })

    const options = [
      {
        id: -1,
        name: t('project.node.local_file'),
        dirctory: true,
        disabled: true,
        children: file
      },
      {
        id: -2,
        name: t('project.node.git_file'),
        dirctory: true,
        disabled: true,
        children: git
      }
    ]
    utils.removeUselessChildren(options)
    resourcesOptions.value = options || []
    resourcesLoading.value = false
  }

  onMounted(() => {
    getResources()
  })

  return {
    type: 'tree-select',
    field: 'resourceList',
    name: t('project.node.resources'),
    span,
    options: resourcesOptions,
    props: {
      filterable: true,
      multiple: true,
      checkable: true,
      cascade: true,
      showPath: true,
      checkStrategy: 'parent',
      placeholder: t('project.node.resources_tips'),
      keyField: 'id',
      labelField: 'name',
      loading: resourcesLoading
    },
    validate: {
      trigger: ['input', 'blur'],
      required,
      validator(validate: any, value: IResource[]) {
        if (required) {
          if (!value) {
            return new Error(t('project.node.resources_tips'))
          }

          if (limit > 0 && value.length > limit) {
            return new Error(t('project.node.resources_limit_tips') + limit)
          }
        }
      }
    }
  }
}
