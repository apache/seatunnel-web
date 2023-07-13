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

import { defineComponent, ref, PropType, onMounted, watch, h } from 'vue'
import { NLayoutSider, NMenu, NIcon, NDropdown, NEllipsis } from 'naive-ui'
import { useThemeStore } from '@/store/theme'
import styles from './index.module.scss'
import { PartitionOutlined, ProjectOutlined, RightOutlined } from '@vicons/antd'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { MenuOption } from 'naive-ui'
import { useI18n } from 'vue-i18n'

const Sidebar = defineComponent({
  name: 'Sidebar',
  props: {
    sideMenuOptions: {
      type: Array as PropType<any>,
      default: []
    },
    sideKey: {
      type: String as PropType<string>,
      default: ''
    }
  },
  setup() {
    const router = useRouter()
    const collapsedRef = ref(false)
    const defaultExpandedKeys = ['']
    const route = useRoute()
    const { t } = useI18n()
    // Determine if it is a project overview

    const showDrop = ref(false)
    const themeStore = useThemeStore()
    const menuStyle = ref(themeStore.getTheme as 'dark' | 'dark-blue' | 'light')

    const sideMenuOptions = ref([
      {
        label: () =>
          h(
            RouterLink,
            {
              to: {
                path: '/task/synchronization-definition'
              },
              exact: false
            },
            { default: () => t('menu.sync_task_definition') }
          ),
        key: 'synchronization-definition'
      },
      {
        label: () =>
          h(
            RouterLink,
            {
              to: {
                path: '/task/synchronization-instance'
              },
              exact: false
            },
            { default: () => t('menu.sync_task_instance') }
          ),
        key: 'synchronization-instance'
      }
    ])

    onMounted(() => {})

    return {
      collapsedRef,
      defaultExpandedKeys,
      menuStyle,
      themeStore,
      showDrop,
      sideMenuOptions,
      route
    }
  },
  render() {
    return (
      <NLayoutSider
        bordered
        nativeScrollbar={false}
        show-trigger='bar'
        collapse-mode='width'
        collapsed={this.collapsedRef}
        onCollapse={() => (this.collapsedRef = true)}
        onExpand={() => (this.collapsedRef = false)}
        width={196}
      >
        <NMenu
          class='tab-vertical'
          value={this.$props.sideKey}
          options={this.sideMenuOptions}
          defaultExpandedKeys={this.defaultExpandedKeys}
        />
      </NLayoutSider>
    )
  }
})

export default Sidebar
