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

import { defineComponent, PropType, watch, onMounted, computed } from 'vue'
import Modal from '@/components/modal'
import {
  NSpace,
  NSteps,
  NStep,
  NUpload,
  NButton,
  NDataTable,
  NIcon,
  NForm,
  NFormItem,
  NUploadDragger,
  NText,
  NCheckbox,
  NCheckboxGroup,
  NCollapse,
  NCollapseItem,
  NGrid,
  NGi
} from 'naive-ui'
import { useImport } from './use-import'
import { useI18n } from 'vue-i18n'
import { RightOutlined, UploadOutlined } from '@vicons/antd'
import { tasksState } from '@/common/common'
import { ITaskState } from '../../instance/types'
import styles from '../index.module.scss'

const ImportModal = defineComponent({
  name: 'coronation-import-modal',
  props: {
    showModalRef: {
      type: Boolean as PropType<boolean>,
      default: false
    }
  },
  emits: ['cancelModal', 'confirmModal'],
  setup(props, ctx) {
    const {
      rules,
      variables,
      formRef,
      createColumns,
      nextStep,
      prevStep,
      onFileChange
    } = useImport(ctx)
    const { t } = useI18n()

    const tableData = computed(() =>
      !variables.coronationMore && variables.tableData.length > 5
        ? variables.tableData.slice(0, 5)
        : variables.tableData
    )

    const treeData = computed(() =>
      !variables.upstreamMore && variables.treeData.length > 5
        ? variables.treeData.slice(0, 5)
        : variables.treeData
    )

    const getAllTaskKeys = () => {
      const taskKeys = new Set()
      variables.treeData
        .filter(
          (item: any) => item.upstreamTasks && item.upstreamTasks.length > 0
        )
        .map((item: any) => taskKeys.add(item.key))
      return [...taskKeys]
    }

    const cancelModal = () => {
      ctx.emit('cancelModal', props.showModalRef)
    }

    const getConfirmText = () => {
      if (variables.currentStep === 3) return t('project.task.confirm')
      else return t('project.task.next_step')
    }

    onMounted(() => {
      createColumns(variables)
    })

    watch(useI18n().locale, () => {
      createColumns(variables)
    })

    watch(
      () => props.showModalRef,
      () => {
        if (props.showModalRef) {
          variables.currentStep = 1
          variables.file = undefined
          variables.coronationMore = false
          variables.upstreamMore = false
        }
      }
    )

    const expandedNames = computed(() => getAllTaskKeys() as Array<number>)

    const renderCheckBoxLabel = (task: any) => {
      const stateOption = tasksState(t)[task.taskStatus as ITaskState] || {}

      return task.taskStatus && stateOption.desc
        ? `${task.taskNode} (${stateOption.desc})`
        : `${task.taskNode}`
    }

    const checkBoxRender = (key: string, upstreamTasks: any) => {
      return (
        <NCheckboxGroup
          defaultValue={upstreamTasks.map((item: any) => item.key)}
          onUpdateValue={(value) => handleUpstreamValue(key, value)}
          style={{ marginLeft: '20px' }}
        >
          <NGrid cols={1}>
            {upstreamTasks.map((item: any) => (
              <NGi>
                <NCheckbox
                  value={item.key}
                  label={renderCheckBoxLabel(item)}
                ></NCheckbox>
              </NGi>
            ))}
          </NGrid>
        </NCheckboxGroup>
      )
    }

    const handleUpstreamValue = (key: string, value: any) => {
      variables.coronationTask[key] = value
    }

    return () => (
      <Modal
        title={t('project.task.task_coronation')}
        show={props.showModalRef}
        onCancel={cancelModal}
        onConfirm={nextStep}
        confirmText={getConfirmText()}
        cancelShow={variables.currentStep !== 2}
      >
        {{
          default: () => (
            <NSpace vertical>
              <NSteps current={variables.currentStep}>
                <NStep title={t('project.task.local_import')} />
                <NStep title={t('project.task.task_coronation_list')} />
                <NStep title={t('project.task.associate_upstream')} />
              </NSteps>
              {variables.currentStep === 1 && (
                <NForm rules={rules} ref={formRef}>
                  <NFormItem path='file' showLabel={false}>
                    <NUpload
                      accept='.xlsx,.xls'
                      on-change={onFileChange}
                      class={styles['uploader']}
                      fileList={variables.file ? [variables.file] : []}
                    >
                      <NUploadDragger>
                        <NSpace vertical>
                          <NIcon size='48' depth='3'>
                            <UploadOutlined />
                          </NIcon>
                          <NText>{t('project.isolation.upload_tips')}</NText>
                        </NSpace>
                      </NUploadDragger>
                    </NUpload>
                  </NFormItem>
                </NForm>
              )}
              {variables.currentStep === 2 && (
                <NSpace justify='center'>
                  <NDataTable
                    columns={variables.columns}
                    data={tableData.value}
                  />
                  {!variables.coronationMore && variables.tableData.length > 5 && (
                    <NButton
                      text
                      type='info'
                      onClick={() => (variables.coronationMore = true)}
                    >
                      {t('project.task.more')}
                    </NButton>
                  )}
                </NSpace>
              )}
              {variables.currentStep === 3 && (
                <NSpace vertical>
                  <NCollapse defaultExpandedNames={expandedNames.value}>
                    {treeData.value.map((item: any) => (
                      <NCollapseItem
                        title={
                          item.workflowInstanceName
                            ? `${item.taskNode} (${item.workflowInstanceName})`
                            : `${item.taskNode}`
                        }
                        name={item.key}
                        style={{
                          marginLeft:
                            item.upstreamTasks && item.upstreamTasks.length > 0
                              ? 0
                              : '20px'
                        }}
                      >
                        {{
                          default: () =>
                            checkBoxRender(item.key, item.upstreamTasks),
                          arrow: () =>
                            item.upstreamTasks &&
                            item.upstreamTasks.length > 0 ? (
                              <NIcon>
                                <RightOutlined />
                              </NIcon>
                            ) : (
                              ''
                            )
                        }}
                      </NCollapseItem>
                    ))}
                  </NCollapse>
                  {!variables.upstreamMore && variables.treeData.length > 5 && (
                    <NButton
                      text
                      type='info'
                      onClick={() => (variables.upstreamMore = true)}
                      style={{ marginLeft: '20px' }}
                    >
                      {t('project.task.more')}
                    </NButton>
                  )}
                </NSpace>
              )}
            </NSpace>
          ),
          'btn-middle': () =>
            variables.currentStep !== 1 && (
              <NButton type='primary' size='small' onClick={prevStep}>
                {t('project.task.prev_step')}
              </NButton>
            )
        }}
      </Modal>
    )
  }
})

export default ImportModal
