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

import { defineComponent, watch } from 'vue'
import {
  NForm,
  NFormItem,
  NText,
  NIcon,
  NButton,
  NUpload,
  NUploadDragger,
  NSpace,
  NSteps,
  NStep,
  NDataTable,
  NEllipsis
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useDetail } from './use-detail'
import { UploadOutlined } from '@vicons/antd'
import Modal from '@/components/modal'
import styles from './index.module.scss'

const props = {
  show: {
    type: Boolean,
    default: false
  },
  projectCode: {
    type: String,
    default: ''
  }
}
const DetailModal = defineComponent({
  name: 'DetailModal',
  props,
  emits: ['cancel', 'update'],
  setup(props, ctx) {
    const { t } = useI18n()

    const { state, formRef, rules, onReset, onConfirm, onFileChange } =
      useDetail(Number(props.projectCode))

    const onCancel = () => {
      ctx.emit('cancel')
    }

    const onSubmit = async () => {
      const res = await onConfirm()
      if (res) {
        onCancel()
        ctx.emit('update')
      }
    }

    watch(
      () => props.show,
      (value) => {
        if (!value) onReset()
      }
    )

    return () => (
      <Modal
        show={props.show}
        title={t('project.isolation.task_isolation')}
        onConfirm={onSubmit}
        confirmLoading={state.saving}
        onCancel={onCancel}
        confirmText={t(
          state.current === 1
            ? 'project.isolation.next'
            : 'project.isolation.sure'
        )}
      >
        {{
          default: () => (
            <NSpace class={styles.detail} vertical>
              <NSteps current={state.current} status={state.status}>
                <NStep title={t('project.isolation.local_upload')} />
                <NStep title={t('project.isolation.isolation_tasks')} />
              </NSteps>
              {state.current === 1 && (
                <NForm rules={rules} ref={formRef}>
                  <NFormItem path='file' showLabel={false}>
                    <NUpload
                      accept='.xlsx,.xls'
                      on-change={onFileChange}
                      class={styles['uploader']}
                      fileList={state.file ? [state.file] : []}
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
              {state.current === 2 && (
                <NSpace vertical>
                  <div>
                    {t('project.isolation.total_items', {
                      total: state.tasks.length
                    })}
                  </div>
                  <NDataTable
                    columns={[
                      {
                        key: 'taskName',
                        title: t('project.isolation.task_name'),
                        width: 150,
                        render: (rowData) => (
                          <NEllipsis style='max-width: 100px'>
                            {rowData.taskName}
                          </NEllipsis>
                        )
                      },
                      {
                        key: 'workflowInstanceName',
                        title: t('project.isolation.workflow_instance_name'),
                        render: (rowData) => (
                          <NEllipsis style='max-width: 100%'>
                            {rowData.workflowInstanceName}
                          </NEllipsis>
                        )
                      }
                    ]}
                    data={state.tasks}
                    max-height={300}
                  />
                </NSpace>
              )}
            </NSpace>
          ),
          'btn-middle': () =>
            state.current === 2 && (
              <NButton
                type='info'
                size='small'
                onClick={() => void onConfirm(-1)}
              >
                {t('project.isolation.previous')}
              </NButton>
            )
        }}
      </Modal>
    )
  }
})

export default DetailModal
