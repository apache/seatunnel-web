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
import utils from '@/utils'
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomParams, useResources } from '.'
import { useTaskNodeStore } from '@/store/project/task-node'
import { queryResourceByProgramType } from '@/service/modules/resources'
import type { IJsonItem, IMainJar } from '../types'

export function useJar(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const mainClassSpan = computed(() => (model.programType === 'CLASS' ? 24 : 0))
  const mainJarSpan = computed(() => (model.programType === 'CLASS' ? 0 : 24))

  const dependentPathSpan = computed(() =>
    model.programType === 'CLASS' ? 24 : 0
  )
  const mainJarOptions = ref([] as IMainJar[])
  const taskStore = useTaskNodeStore()

  const getMainJars = async () => {
    const programType = 'JAVA'
    const res = await queryResourceByProgramType({
      type: 'FILE',
      programType
    })
    utils.removeUselessChildren(res)
    mainJarOptions.value = res || []
    taskStore.updateMainJar(programType, res)
  }

  onMounted(() => {
    getMainJars()
  })

  return [
    {
      type: 'radio',
      field: 'programType',
      span: 24,
      name: t('project.node.execute_type'),
      options: programTypeOptions
    },
    {
      type: 'input',
      field: 'options',
      span: 24,
      name: 'Java Option',
      props: {
        placeholder: t('project.node.java_option_tips')
      }
    },
    {
      type: 'tree-select',
      field: 'cpResIds',
      span: dependentPathSpan,
      name: t('project.node.dependent_path'),
      props: {
        multiple: true,
        checkable: true,
        cascade: true,
        showPath: true,
        checkStrategy: 'child',
        placeholder: t('project.node.dependent_path_tips'),
        keyField: 'id',
        labelField: 'fullName'
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (model.programType === 'CLASS' && !value) {
            return new Error(t('project.node.dependent_path_tips'))
          }
        }
      },
      options: mainJarOptions
    },
    {
      type: 'input',
      field: 'mainClass',
      span: mainClassSpan,
      name: t('project.node.main_class'),
      props: {
        placeholder: t('project.node.main_class_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (model.programType === 'CLASS' && !value) {
            return new Error(t('project.node.main_class_tips'))
          }
        }
      }
    },
    {
      type: 'tree-select',
      field: 'mainJar',
      span: mainJarSpan,
      name: t('project.node.main_package'),
      props: {
        cascade: true,
        showPath: true,
        checkStrategy: 'child',
        placeholder: t('project.node.main_package_tips'),
        keyField: 'id',
        labelField: 'fullName'
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.main_package_tips'))
          }
        }
      },
      options: mainJarOptions
    },
    {
      type: 'input',
      field: 'mainArgs',
      name: t('project.node.main_arguments'),
      props: {
        type: 'textarea',
        placeholder: t('project.node.main_arguments_tips')
      }
    },
    useResources(),
    ...useCustomParams({ model, field: 'localParams', isSimple: true })
  ]
}

const programTypeOptions = [
  {
    label: '执行类',
    value: 'CLASS'
  },
  {
    label: '执行Jar',
    value: 'JAR'
  }
]
