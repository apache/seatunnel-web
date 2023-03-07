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

import { defineComponent, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { NLog } from 'naive-ui'
import { taskInstanceLog } from '@/service/task'
import Modal from '@/components/modal'
import type { PropType, Ref } from 'vue'
import type { ResponseBasic } from '@/service/types'

const props = {
  showModal: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  row: {
    type: Object as PropType<any>,
    default: {}
  }
}

const LogModal = defineComponent({
  name: 'LogModal',
  props,
  emits: ['confirmModal'],
  setup(props, ctx) {
    const { t } = useI18n()
    const log: Ref<string> = ref('')

    const handleConfirm = () => {
      ctx.emit('confirmModal')
    }

    onMounted(() => {
      props.row.instanceId &&
        taskInstanceLog(props.row.instanceId).then(
          (res: ResponseBasic<{ instanceId: number; logContent: string }>) => {
            log.value = res.data.logContent
          }
        )
    })

    return {
      t,
      log,
      handleConfirm
    }
  },
  render() {
    return (
      <Modal
        title={this.t('tasks.log')}
        show={this.showModal}
        cancelShow={false}
        onConfirm={this.handleConfirm}
        style={{ width: '80%' }}
      >
        <NLog log={this.log} rows={35} />
      </Modal>
    )
  }
})

export default LogModal
