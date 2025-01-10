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

import { defineComponent, onMounted, onUnmounted } from 'vue'
import { NCard, NGrid, NGi } from 'naive-ui'
import { useTaskMetrics } from './use-task-metrics'
import styles from './task-metrics.module.scss'

const TaskMetrics = defineComponent({
  name: 'TaskMetrics',
  setup() {
    const { variables, initCharts, updateCharts } = useTaskMetrics()
    let timer: ReturnType<typeof setInterval>

    onMounted(() => {
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
    })

    return { variables }
  },
  render() {
    return (
      <NGrid x-gap='12' cols='2'>
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
        <NGi span='2'>
          <NCard>
            <div
              ref={(el) => (this.variables.delayChartRef = el)}
              class={styles.chart}
            />
          </NCard>
        </NGi>
      </NGrid>
    )
  }
})

export { TaskMetrics } 