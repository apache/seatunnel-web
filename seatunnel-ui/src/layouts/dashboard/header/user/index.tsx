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

import { defineComponent, toRefs } from 'vue'
import { NSpace, NDropdown, NIcon, NButton } from 'naive-ui'
import { useUserDropdown } from './use-user-dropdown'
import { useUserStore } from '@/store/user'
import { DownOutlined, UserOutlined } from '@vicons/antd'
import type { UserDetail } from '@/service/user/types'

const User = defineComponent({
  setup() {
    const { state, handleSelect } = useUserDropdown()
    const userDetail = useUserStore()

    return {
      ...toRefs(state),
      handleSelect,
      userDetail
    }
  },
  render() {
    return (
      <NSpace
        justify='end'
        align='center'
        class='h-16 mr-12'
        style={{ cursor: 'pointer' }}
      >
        <NDropdown
          trigger='click'
          options={this.dropdownOptions}
          onSelect={this.handleSelect}
        >
          <NButton text>
            <NSpace>
              <NIcon component={UserOutlined} />
              <span>{(this.userDetail.getUserInfo as UserDetail).name}</span>
              <NIcon component={DownOutlined} />
            </NSpace>
          </NButton>
        </NDropdown>
      </NSpace>
    )
  }
})

export default User
