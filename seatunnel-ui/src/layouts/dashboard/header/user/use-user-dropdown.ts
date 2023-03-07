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

import { reactive, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import { userLogout } from '@/service/user'
import { useUserStore } from '@/store/user'
import {
  LogoutOutlined,
  QuestionCircleOutlined,
  SettingOutlined
} from '@vicons/antd'
import type { Router } from 'vue-router'
import type { Component } from 'vue'

export function useUserDropdown() {
  const router: Router = useRouter()
  const { t } = useI18n()
  const userStore = useUserStore()

  const renderIcon = (icon: Component) => {
    return () => {
      return h(NIcon, null, {
        default: () => h(icon)
      })
    }
  }

  const dropdownOptions = [
    {
      key: 'help',
      label: t('menu.help'),
      icon: renderIcon(QuestionCircleOutlined)
    },
    {
      key: 'setting',
      label: t('menu.setting'),
      icon: renderIcon(SettingOutlined)
    },
    {
      key: 'logout',
      label: t('menu.logout'),
      icon: renderIcon(LogoutOutlined)
    }
  ]

  const state = reactive({
    dropdownOptions
  })

  const handleSelect = (key: string) => {
    if (key === 'help') {
      window.open('http://seatunnel.incubator.apache.org/versions/')
    } else if (key === 'setting') {
      router.push({ path: '/setting' })
    } else if (key === 'logout') {
      userLogout().then(() => {
        userStore.setUserInfo({})
        router.push({ path: '/login' })
      })
    }
  }

  return { state, handleSelect }
}
