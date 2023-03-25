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
import { useSettingStore } from '@/store/setting'

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
                <NSwitch
                  value={useSettingStore().getSequenceColumn}
                  onUpdateValue={(v) => {
                    useSettingStore().setSequenceColumn(v)
                  }}
                />
              </NSpace>
            </NListItem>
            <NListItem>
              <NSpace justify='space-between' align='center'>
                <span>{this.t('setting.data_unique_value')}</span>
                <NSwitch
                  value={useSettingStore().getDataUniqueValue}
                  onUpdateValue={(v) => {
                    useSettingStore().setDataUniqueValue(v)
                  }}
                />
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
        <NCard title={this.t('setting.request_setting')}>
          <NList>
            <NListItem>
              <NSpace justify='space-between' align='center'>
                <span>{this.t('setting.request_time')}</span>
                <div class='w-56'>
                  <NSelect
                    value={useSettingStore().getRequestTimeValue}
                    onUpdateValue={(v) => {
                      useSettingStore().setRequestTimeValue(v)
                    }}
                    options={[
                      { value: 3000, label: '3000ms' },
                      { value: 6000, label: '6000ms' },
                      { value: 10000, label: '10000ms' },
                      { value: 20000, label: '20000ms' },
                      { value: 30000, label: '30000ms' }
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
                <span>{this.t('setting.model')}</span>
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
            <NListItem>
              <NSpace justify='space-between' align='center'>
                <span>{this.t('setting.hue')}</span>
                <div class='w-56'>
                  <NSelect
                    value={'purple'}
                    options={[
                      { value: 'purple', label: this.t('setting.purple') }
                    ]}
                  />
                </div>
              </NSpace>
            </NListItem>
            <NListItem>
              <NSpace justify='space-between' align='center'>
                <span>{this.t('setting.fillet')}</span>
                <div class='w-56'>
                  <NSelect
                    value={useSettingStore().getFilletValue}
                    onUpdateValue={(v) => {
                      useSettingStore().setFilletValue(v)
                    }}
                    options={[
                      { value: 5, label: '5px' },
                      { value: 10, label: '10px' },
                      { value: 15, label: '15px' },
                      { value: 20, label: '20px' },
                      { value: 25, label: '25px' },
                      { value: 30, label: '30px' }
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
