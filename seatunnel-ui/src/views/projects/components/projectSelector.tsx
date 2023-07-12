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

import { useProjectStore } from '@/store/project'
import { NSelect } from 'naive-ui'
import { defineComponent, ref, VNode, h } from 'vue'
import { NTooltip, SelectOption } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import './index.scss'
const props = {
  key: {
    type: String,
    default: ''
  },
  size: {
    default: 'medium'
  },
  initCode: {
    default: null
  }
}

export default defineComponent({
  name: 'projectSelector',
  props,
  emits: ['getprojectList'],
  setup(props, ctx) {
    const { t } = useI18n()
    const projectItem = Number(props.initCode) || null
    const projectOption = ref([] as any)
    const projectStore = useProjectStore()
    projectOption.value.push(...projectStore.getProjects)
    const rendedrOption = ({
      node,
      option
    }: {
      node: VNode
      option: SelectOption
    }) => {
      return h(
        NTooltip,
        {
          width: 100,
          placement: 'bottom-end',
          showArrow: false
        },
        {
          trigger: () => node,
          default: () => option.label
        }
      )
    }
    const handleSelectproject = (option: any) => {
      ctx.emit('getprojectList', option)
    }
    return {
      handleSelectproject,
      projectOption,
      projectItem,
      rendedrOption,
      t
    }
  },
  render() {
    return (
      <div style={{ width: '180px' }}>
        {this.projectOption && (
          <NSelect
            size={this.$props.size as any}
            renderOption={this.rendedrOption}
            v-model:value={this.projectItem}
            placeholder={this.t('project.all_project')}
            options={this.projectOption}
            clearable
            filterable
            onUpdateValue={this.handleSelectproject}
          />
        )}
      </div>
    )
  }
})
