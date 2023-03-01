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

import { defineComponent } from 'vue'
import { useI18n } from 'vue-i18n'
import { NSpace, NCard, NSwitch, NList, NListItem, NSelect } from 'naive-ui'

const Setting = defineComponent({
  name: 'Setting',
  setup() {
    const { t } = useI18n()

    return {
      t
    }
  },
  render() {
    return (
      <NSpace vertical>
        <NCard title={this.t('setting.table_setting')}>
          <NList>
            <NListItem>
              <NSpace justify='space-between' align='center'>
                <span>{this.t('setting.sequence_column')}</span>
                <NSwitch />
              </NSpace>
            </NListItem>
            <NListItem>
              <NSpace justify='space-between' align='center'>
                <span>{this.t('setting.data_unique_value')}</span>
                <NSwitch />
              </NSpace>
            </NListItem>
          </NList>
        </NCard>
        <NCard title={this.t('setting.language_setting')}>
          <NList>
            <NListItem>
              <NSpace justify='space-between' align='center'>
                <span>{this.t('setting.language')}</span>
                <div class='w-56'>
                  <NSelect
                    value={'english'}
                    options={[
                      { value: 'english', label: this.t('setting.english') }
                    ]}
                  />
                </div>
              </NSpace>
            </NListItem>
          </NList>
        </NCard>
        <NCard title={this.t('setting.theme_setting')}>
          <NList>
            <NListItem>
              <NSpace justify='space-between' align='center'>
                <span>{this.t('setting.theme')}</span>
                <div class='w-56'>
                  <NSelect
                    value={'light'}
                    options={[
                      { value: 'light', label: this.t('setting.light') }
                    ]}
                  />
                </div>
              </NSpace>
            </NListItem>
          </NList>
        </NCard>
      </NSpace>
    )
  }
})

export default Setting
