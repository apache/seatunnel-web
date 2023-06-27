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
import { getFolderList, getMappingList } from '@/service/modules/informatica'
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import type { IJsonItem } from '../types'

//use-informatica

export function useInformatica(): IJsonItem[] {
  const { t } = useI18n()
  const folderLoading = ref(false)
  const folderOptions = ref([] as any)
  const mappingLoading = ref(false)
  const mappingOptions = ref([] as any)

  const handleFolderList = async () => {
    if (folderLoading.value) return
    folderLoading.value = true
    const res = await getFolderList()
    folderOptions.value = res.map((item: any) => ({
      label: item,
      value: item
    }))
    folderLoading.value = false
  }

  const handleMappingList = async (value: string) => {
    if (mappingLoading.value) return
    mappingLoading.value = true
    const res = await getMappingList(value)
    mappingOptions.value = res.map((item: any) => ({
      label: item,
      value: item
    }))
    mappingLoading.value = false
  }

  onMounted(() => {
    handleFolderList()
  })

  return [
    {
      type: 'select',
      field: 'informaticaFolderName',
      name: t('project.node.folder_name'),
      props: {
        loading: folderLoading,
        'on-update:value': (value: any) => {
          handleMappingList(value)
        }
      },
      options: folderOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        message: t('project.node.folder_name_tips')
      }
    },
    {
      type: 'select',
      field: 'informaticaWorkflowName',
      name: t('project.node.mapping_name'),
      props: {
        loading: mappingLoading
      },
      options: mappingOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        message: t('project.node.mapping_name_tips')
      }
    }
  ]
}
