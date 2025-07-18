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

import { defineComponent, inject, ref } from 'vue'
import { NTooltip } from 'naive-ui'
import styles from './index.module.scss'
import SourceImg from '../images/source.png'
import SinkImg from '../images/sink.png'
import FieldMapperImg from '../images/field-mapper.png'
import FilterEventTypeImg from '../images/filter-event-type.png'
import ReplaceImg from '../images/replace.png'
import JsonPathImg from '../images/json-path.png'
import SplitImg from '../images/spilt.png'
import CopyImg from '../images/copy.png'
import SqlImg from '../images/sql.png'

const Node = defineComponent({
  name: 'Node',
  setup() {
    const getNode = inject('getNode') as any
    const node = getNode()
    const { name, unsaved, type, connectorType, isError } = node.getData()

    const icon = ref('')

    if (type === 'source') {
      icon.value = SourceImg
    } else if (type === 'sink') {
      icon.value = SinkImg
    } else if (type === 'transform' && connectorType === 'FieldMapper') {
      icon.value = FieldMapperImg
    } else if (type === 'transform' && connectorType === 'FilterRowKind') {
      icon.value = FilterEventTypeImg
    } else if (type === 'transform' && connectorType === 'Replace') {
      icon.value = ReplaceImg
    } else if (type === 'transform' && connectorType === 'MultiFieldSplit') {
      icon.value = SplitImg
    } else if (type === 'transform' && connectorType === 'Copy') {
      icon.value = CopyImg
    } else if (type === 'transform' && connectorType === 'Sql') {
      icon.value = SqlImg
    } else if (type === 'transform' && connectorType === 'JsonPath') {
      icon.value = JsonPathImg
    }


    const getBorderStyle = () => {
      if (isError) {
        return '4px solid #F87171'
      } else if (unsaved) {
        return '4px solid #FBBF24'
      } else if (type === 'source') {
        return '4px solid #34D399'
      } else if (type === 'sink') {
        return '4px solid #60A5FA'
      } else if (type === 'transform') {
        return '4px solid #A78BFA'
      } else {
        return '4px solid #60A5FA'
      }
    }

    const getBackgroundStyle = () => {
      if (isError) {
        return 'linear-gradient(135deg, #FEF2F2 0%, #FEE2E2 100%)'
      } else if (unsaved) {
        return 'linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%)'
      } else if (type === 'source') {
        return 'linear-gradient(135deg, #ECFDF5 0%, #D1FAE5 100%)'
      } else if (type === 'sink') {
        return 'linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%)'
      } else if (type === 'transform') {
        return 'linear-gradient(135deg, #F5F3FF 0%, #EDE9FE 100%)'
      } else {
        return 'linear-gradient(135deg, #F8FAFC 0%, #F1F5F9 100%)'
      }
    }

    return () => (
      <div
        class={styles['dag-node']}
        style={{
          borderLeft: getBorderStyle(),
          background: getBackgroundStyle()
        }}
      >
        <img src={icon.value} class={styles['dag-node-icon']} />
        <NTooltip trigger='hover'>
          {{
            trigger: () => <div class={styles['dag-node-label']}>{name}</div>,
            default: () => name
          }}
        </NTooltip>
        
        {/* 状态指示器 */}
        {isError && (
          <div class={styles['dag-node-status']} style={{ background: '#EF4444' }} />
        )}
        {unsaved && !isError && (
          <div class={styles['dag-node-status']} style={{ background: '#F59E0B' }} />
        )}
      </div>
    )
  }
})

export default Node
