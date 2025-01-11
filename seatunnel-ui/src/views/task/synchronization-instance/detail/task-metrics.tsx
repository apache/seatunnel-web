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

import { defineComponent, onMounted, onUnmounted, nextTick } from 'vue'
import { NCard, NGrid, NGi, NSpace, NDatePicker, NSelect } from 'naive-ui'
import { useTaskMetrics } from './use-task-metrics'
import styles from './task-metrics.module.scss'
import { useI18n } from 'vue-i18n'

const TaskMetrics = defineComponent({
  name: 'TaskMetrics',
  setup() {
    const { variables, initCharts, updateCharts, handleDateRangeChange, handleTimeOptionChange } = useTaskMetrics()
    let timer: ReturnType<typeof setInterval>
    const { t } = useI18n()

    onMounted(async () => {
      await nextTick()
      initCharts()
      updateCharts()
      timer = setInterval(() => {
        updateCharts()
      }, 10000)
    })

    onUnmounted(() => {
      if (timer) {
        clearInterval(timer)
      }
      variables.readRowCountChart?.dispose()
      variables.writeRowCountChart?.dispose()
      variables.readQpsChart?.dispose()
      variables.writeQpsChart?.dispose()
      variables.delayChart?.dispose()
    })

    return { 
      variables, 
      handleDateRangeChange,
      handleTimeOptionChange,
      t
    }
  },
  render() {
    return (
      <NGrid x-gap={12} cols={2}>
        <NGi span={2}>
          <NCard
            title={this.t('project.metrics.metrics_title')}
            headerStyle={{ padding: '16px 20px' }}
            contentStyle={{ padding: '4px 20px 20px' }}
          >
            {{
              header: () => (
                <NSpace justify="space-between" align="center" style="width: 100%">
                  <span class="n-card-header__main">
                    {this.t('project.metrics.metrics_title')}
                  </span>
                  <NSpace align="center">
                    <NSelect
                      value={this.variables.selectedTimeOption}
                      options={this.variables.timeOptions}
                      onUpdateValue={this.handleTimeOptionChange}
                      style={{ width: '150px' }}
                    />
                    {this.variables.showDatePicker && (
                      <NDatePicker
                        type="datetimerange"
                        value={this.variables.dateRange}
                        onUpdateValue={this.handleDateRangeChange}
                        clearable
                        defaultTime={['00:00:00', '23:59:59']}
                        valueFormat="timestamp"
                        actions={['clear', 'confirm']}
                        style={{ width: '320px' }}
                        placeholder={[
                          this.t('project.metrics.start_time'),
                          this.t('project.metrics.end_time')
                        ]}
                        placement="bottom-end"
                        size="small"
                        to={false}
                      />
                    )}
                  </NSpace>
                </NSpace>
              ),
              default: () => (
                <NGrid x-gap={12} y-gap={12} cols={2}>
                  <NGi>
                    <NCard>
                      <div
                        ref={(el) => (this.variables.readRowCountChartRef = el)}
                        class={styles.chart}
                      />
                    </NCard>
                  </NGi>
                  <NGi>
                    <NCard>
                      <div
                        ref={(el) => (this.variables.writeRowCountChartRef = el)}
                        class={styles.chart}
                      />
                    </NCard>
                  </NGi>
                  <NGi>
                    <NCard>
                      <div
                        ref={(el) => (this.variables.readQpsChartRef = el)}
                        class={styles.chart}
                      />
                    </NCard>
                  </NGi>
                  <NGi>
                    <NCard>
                      <div
                        ref={(el) => (this.variables.writeQpsChartRef = el)}
                        class={styles.chart}
                      />
                    </NCard>
                  </NGi>
                  <NGi span={2}>
                    <NCard>
                      <div
                        ref={(el) => (this.variables.delayChartRef = el)}
                        class={styles.chart}
                      />
                    </NCard>
                  </NGi>
                </NGrid>
              )
            }}
          </NCard>
        </NGi>
      </NGrid>
    )
  }
})

export { TaskMetrics } 