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

import { defineComponent, PropType, toRefs, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { NForm, NFormItem, NInput, NRadioGroup, NRadio, NSpace } from 'naive-ui'
import { useTaskModal } from './use-task-modal'
import Modal from '@/components/modal'
import { useProjectStore } from '@/store/project'
import ProjectSelector from '@/views/projects/components/projectSelector'

const props = {
  showModalRef: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  row: {
    type: Object as PropType<any>,
    default: {}
  }
}

const TaskModal = defineComponent({
  name: 'TaskModal',
  props,
  emits: ['cancelModal', 'confirmModal'],
  setup(props, ctx) {
    const { t } = useI18n()
    const { variables, handleValidate } = useTaskModal(props, ctx)
    const projectStore = useProjectStore()
    const projectCode = ref([])
    const globalFlag = projectStore.getGlobalFlag
    projectCode.value = globalFlag ? [] : projectStore.getCurrentProject[0]
    const showPre = ref(globalFlag ? true : false)
    const synchronizationForm: any = ref(null)

    const cancelModal = () => {
      variables.model.name = ''
      variables.model.description = ''
      ctx.emit('cancelModal', props.showModalRef)
    }

    const projectRule = {
      projectCode: {
        required: true,
        validator() {
          if (projectCode.value.length == 0) {
            return new Error(t('project.dag.project_empty'))
          }
        },
        trigger: 'blur'
      }
    }

    const preCancle = () => {
      ctx.emit('cancelModal')
      projectCode.value = []
      variables.model.name = ''
      variables.model.description = ''
    }

    const confirmModal = () => {
      handleValidate()
      projectCode.value = []
    }

    const getNextStep = () => {
      if (synchronizationForm.value) {
        synchronizationForm.value.validate(async (valid: any) => {
          if (!valid) {
            showPre.value = false
            variables.model.name = ''
            variables.model.description = ''
          }
        })
      }
    }

    const getProjectList = (projectListItem: any) => {
      if (projectListItem) {
        projectCode.value = [projectListItem] as any
      } else {
        projectCode.value = []
      }
    }
    return {
      t,
      ...toRefs(variables),
      cancelModal,
      confirmModal,
      preCancle,
      getNextStep,
      projectCode,
      projectRule,
      showPre,
      synchronizationForm,
      getProjectList,
      globalFlag
    }
  },
  render() {
    const {
      projectCode,
      projectRule,
      getNextStep,
      preCancle,
      showPre,
      globalFlag,
      t,
      getProjectList,
      showModalRef
    } = this
    return (
      <template>
        {globalFlag && (
          <Modal
            show={showModalRef && showPre && globalFlag}
            title={t('project.workflow.select_project')}
            onCancel={preCancle}
            onConfirm={getNextStep}
            confirmText={t('project.next_step')}
          >
            <NForm
              model={projectCode}
              rules={projectRule}
              ref='synchronizationForm'
            >
              <NFormItem
                label={t('project.workflow.choose_project')}
                path='projectCode'
              >
                <ProjectSelector
                  initCode={projectCode.length == 1 ? projectCode[0] : null}
                  style={{ width: '100%' }}
                  onGetprojectList={getProjectList}
                ></ProjectSelector>
              </NFormItem>
            </NForm>
          </Modal>
        )}
        <Modal
          title={this.t(
            'project.synchronization_definition.create_synchronization_task'
          )}
          show={showModalRef && !showPre}
          onCancel={this.cancelModal}
          onConfirm={this.confirmModal}
          confirmLoading={this.saving}
        >
          <NForm model={this.model} rules={this.rules} ref='taskModalFormRef'>
            <NFormItem
              label={this.t(
                'project.synchronization_definition.synchronization_task_name'
              )}
              path='name'
            >
              <NInput
                clearable
                v-model={[this.model.name, 'value']}
                placeholder={this.t(
                  'project.synchronization_definition.task_name_tips'
                )}
              />
            </NFormItem>
            <NFormItem
              label={this.t('project.synchronization_definition.task_describe')}
              path='description'
            >
              <NInput
                clearable
                v-model={[this.model.description, 'value']}
                placeholder={this.t(
                  'project.synchronization_definition.task_describe_tips'
                )}
                maxlength={50}
              />
            </NFormItem>
            <NFormItem
              label={this.t(
                'project.synchronization_definition.business_model'
              )}
              path='businessModel'
            >
              <NRadioGroup v-model={[this.model.jobType, 'value']}>
                <NSpace>
                  <NRadio key='wholeLibrarySync' value='DATA_REPLICA'>
                    {this.t(
                      'project.synchronization_definition.whole_library_sync'
                    )}
                  </NRadio>
                  <NRadio key='dataIntegration' value='DATA_INTEGRATION'>
                    {this.t(
                      'project.synchronization_definition.data_integration'
                    )}
                  </NRadio>
                </NSpace>
              </NRadioGroup>
            </NFormItem>
          </NForm>
        </Modal>
      </template>
    )
  }
})

export { TaskModal }
