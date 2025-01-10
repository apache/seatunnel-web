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

import { reactive, ref } from 'vue'
import type { EChartsOption, LineSeriesOption } from 'echarts'
import * as echarts from 'echarts'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { 
  queryJobMetricsHistory,
  querySyncTaskInstanceDag,
  querySyncTaskInstanceDetail,
  queryRunningInstancePaging 
} from '@/service/sync-task-instance'

export function useTaskMetrics() {
  const route = useRoute()
  const { t } = useI18n()  // 使用 i18n
  
  const variables = reactive({
    readRowCountChartRef: ref(),
    writeRowCountChartRef: ref(),
    readQpsChartRef: ref(),
    writeQpsChartRef: ref(),
    delayChartRef: ref(),
    readRowCountChart: null as echarts.ECharts | null,
    writeRowCountChart: null as echarts.ECharts | null,
    readQpsChart: null as echarts.ECharts | null,
    writeQpsChart: null as echarts.ECharts | null,
    delayChart: null as echarts.ECharts | null,
    metricsData: [] as any[]
  })

  const formatTimeData = (data: any[]) => {
    return data.map(item => new Date(item.createTime).toLocaleTimeString('en-US', { hour12: false }))
  }

  const getChartOption = (title: string, data: any[], key: string): EChartsOption => ({
    title: { 
      text: title,
      textStyle: {
        fontSize: 14,
        fontWeight: 'normal'
      },
      left: 'center'
    },
    tooltip: { 
      show: true,
      trigger: 'item',
      axisPointer: {
        type: 'none'
      },
      position: 'top',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E5E5E5',
      borderWidth: 1,
      padding: [8, 12],
      borderRadius: 4,
      textStyle: {
        color: '#595959',
        fontSize: 13
      },
      formatter: (params: any) => {
        let value = params.value
        if (key.includes('Qps')) {
          value = value.toFixed(2)
        } else if (value >= 10000) {
          value = (value / 10000).toFixed(1) + 'w'
        } else {
          value = Math.round(value)
        }
        
        // 格式化完整的日期时间
        const date = new Date(variables.metricsData[params.dataIndex].createTime)
        const fullDateTime = date.toLocaleString(['zh-CN', 'en-US'], {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        })
        .replace(/\//g, '-')  // 将斜杠替换为横杠
        .replace(/,/g, '')    // 移除英文环境下的逗号

        return `<div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif">
          <div style="color: #8c8c8c; font-size: 12px; margin-bottom: 4px">
            ${fullDateTime}
          </div>
          <div style="display: flex; align-items: center">
            <span style="display: inline-block; width: 6px; height: 6px; border-radius: 50%; background-color: ${params.color}; margin-right: 8px"></span>
            <span style="font-weight: 500">${value}</span>
          </div>
          <div style="font-size: 12px; color: #8c8c8c; margin-top: 4px">
            ${title}
          </div>
        </div>`
      }
    },
    grid: {
      top: '15%',
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: formatTimeData(data),
      axisLine: {
        lineStyle: {
          color: '#E5E5E5'
        }
      },
      axisLabel: {
        color: '#7F7F7F',
        formatter: (value: string) => {
          return value.substring(value.indexOf(' ') + 1) // 只显示时间部分
        }
      }
    },
    yAxis: { 
      type: 'value',
      splitLine: {
        lineStyle: {
          type: 'dashed',
          color: '#E5E5E5'
        }
      },
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#7F7F7F',
        formatter: (value: number) => {
          if (key.includes('Qps')) {
            return value.toFixed(2)
          }
          // 对大数字进行格式化
          if (value >= 10000) {
            return (value / 10000).toFixed(1) + 'w'
          }
          return Math.round(value).toString()
        }
      }
    },
    series: [{
      type: 'line',
      data: data.map(item => item[key]),
      smooth: true,
      symbol: 'circle',
      symbolSize: 4,
      showSymbol: true,
      triggerEvent: true,
      emphasis: {
        focus: 'series',
        itemStyle: {
          color: '#1890FF',
          borderWidth: 3,
          borderColor: '#1890FF',
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      },
      itemStyle: {
        color: '#1890FF',
        borderWidth: 1,
        borderColor: '#fff',
        opacity: 0.3
      },
      lineStyle: {
        width: 2
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {
            offset: 0,
            color: 'rgba(24,144,255,0.3)'
          },
          {
            offset: 1,
            color: 'rgba(24,144,255,0.1)'
          }
        ])
      }
    } as LineSeriesOption],
  })

  const initCharts = () => {
    if (variables.readRowCountChartRef) {
      variables.readRowCountChart = echarts.init(variables.readRowCountChartRef)
    }
    if (variables.writeRowCountChartRef) {
      variables.writeRowCountChart = echarts.init(variables.writeRowCountChartRef)
    }
    if (variables.readQpsChartRef) {
      variables.readQpsChart = echarts.init(variables.readQpsChartRef)
    }
    if (variables.writeQpsChartRef) {
      variables.writeQpsChart = echarts.init(variables.writeQpsChartRef)
    }
    if (variables.delayChartRef) {
      variables.delayChart = echarts.init(variables.delayChartRef)
    }
  }

  const getChartTitle = (key: string) => {
    return t(`project.task.metrics.${key}`)  // 修改国际化路径
  }

  const updateCharts = async () => {
    try {
      const res = await queryJobMetricsHistory({
        jobInstanceId: route.query.jobInstanceId as string
      })
      variables.metricsData = res

      if (variables.readRowCountChart) {
        variables.readRowCountChart.setOption(
          getChartOption(getChartTitle('read_row_count'), variables.metricsData, 'readRowCount')
        )
      }
      if (variables.writeRowCountChart) {
        variables.writeRowCountChart.setOption(
          getChartOption(getChartTitle('write_row_count'), variables.metricsData, 'writeRowCount')
        )
      }
      if (variables.readQpsChart) {
        variables.readQpsChart.setOption(
          getChartOption(getChartTitle('read_qps'), variables.metricsData, 'readQps')
        )
      }
      if (variables.writeQpsChart) {
        variables.writeQpsChart.setOption(
          getChartOption(getChartTitle('write_qps'), variables.metricsData, 'writeQps')
        )
      }
      if (variables.delayChart) {
        variables.delayChart.setOption(
          getChartOption(getChartTitle('record_delay'), variables.metricsData, 'recordDelay')
        )
      }
    } catch (err) {
      console.error('Failed to fetch metrics data:', err)
    }
  }

  return {
    variables,
    initCharts,
    updateCharts
  }
} 