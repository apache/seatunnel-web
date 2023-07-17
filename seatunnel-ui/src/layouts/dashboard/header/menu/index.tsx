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

import { defineComponent, toRefs, ref, watch } from 'vue'
import { NMenu, NSpace } from 'naive-ui'
import { useRouter, useRoute } from 'vue-router'
import { useMenu } from './use-menu'

const Menu = defineComponent({
  setup() {
    const { state } = useMenu()
    const router = useRouter()
    const route = useRoute()

    const handleMenuClick = (key: string) => {
      router.push({ path: `/${key}` })
    }

    const menuKey = ref(route.meta.activeMenu as string)

    watch(
      () => route.path,
      () => {
        menuKey.value = route.meta.activeMenu as string
      }
    )

    return {
      ...toRefs(state),
      handleMenuClick,
      menuKey
    }
  },
  render() {
    return (
      <NSpace align='center' class='h-16'>
        <NMenu
          value={this.menuKey}
          mode='horizontal'
          options={this.menuOptions}
          onUpdateValue={this.handleMenuClick}
        />
      </NSpace>
    )
  }
})

export default Menu
