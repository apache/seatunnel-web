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

import { defineComponent, PropType, ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  NModal,
  NSelect,
  NSpace,
  NSpin,
  NButton,
  NEmpty,
  NAlert,
  NSwitch
} from 'naive-ui'
import { getLogNodes, getLogContent } from '@/service/log'
import styles from './log-viewer-modal.module.scss'

const LogViewerModal = defineComponent({
  name: 'LogViewerModal',
  props: {
    show: {
      type: Boolean as PropType<boolean>,
      default: false
    },
    jobId: {
      type: [String, Number] as PropType<string | number>,
      default: ''
    },
    jobName: {
      type: String as PropType<string>,
      default: ''
    }
  },
  emits: ['update:show'],
  setup(props) {
    const { t } = useI18n()
    
    // 状态
    const logNodes = ref<any[]>([])
    const selectedLogNode = ref('')
    const logContent = ref('')
    const loading = ref(false)
    const loadingLogs = ref(false)
    const refreshInterval = ref(5)
    const autoScroll = ref(true)
    const logContentRef = ref<HTMLElement | null>(null)
    const error = ref('')
    const refreshTimerId = ref<number | null>(null)
    const userScrolled = ref(false)
    
    // 刷新间隔选项
    const refreshIntervalOptions = [
      { label: "关闭刷新", value: 0 },
      { label: "1秒", value: 1 },
      { label: "5秒", value: 5 },
      { label: "10秒", value: 10 },
      { label: "30秒", value: 30 },
      { label: "60秒", value: 60 }
    ]
    
    // 获取日志节点列表
    const fetchLogNodes = async () => {
      if (!props.jobId) return
      
      loading.value = true
      error.value = ''
      
      try {
        const response = await getLogNodes(props.jobId)
        console.log('Log nodes response:', response)
        
        // 确保response.data是一个数组
        if (Array.isArray(response.data)) {
          logNodes.value = response.data
        } else {
          console.error('Log nodes response is not an array:', response.data)
          logNodes.value = []
        }
        
        console.log('Log nodes:', logNodes.value)
        
        if (logNodes.value.length > 0) {
          selectedLogNode.value = logNodes.value[0].logLink
          console.log('Selected log node:', selectedLogNode.value)
          fetchLogContent()
        } else {
          loading.value = false
          logContent.value = ''
        }
      } catch (err: any) {
        console.error('Error fetching log nodes:', err)
        error.value = err.message || "获取日志列表失败"
        loading.value = false
      }
    }
    
    // 获取日志内容
    const fetchLogContent = async () => {
      if (!selectedLogNode.value) return
      
      // 只在首次加载时显示加载状态，避免刷新时闪烁
      if (logContent.value === '') {
        loadingLogs.value = true
      }
      error.value = ''
      
      try {
        console.log('Fetching log content for:', selectedLogNode.value)
        const response = await getLogContent(selectedLogNode.value)
        console.log('Log content response:', response)
        
        // 检查response.data是否存在
        if (response && response.data !== undefined) {
          // 确保日志内容是字符串
          let newContent = '';
          if (typeof response.data === 'string') {
            newContent = response.data
          } else if (typeof response.data === 'object') {
            // 如果是对象，尝试将其转换为字符串
            newContent = JSON.stringify(response.data, null, 2)
          } else {
            // 其他情况，尝试强制转换为字符串
            newContent = String(response.data)
          }
          
          // 只更新内容，不替换整个内容，避免闪烁
          if (newContent !== logContent.value) {
            logContent.value = newContent
            console.log('Log content updated:', newContent.substring(0, 100) + '...')
            
            // 只有在自动滚动开启且用户没有手动滚动时才滚动到底部
            if (autoScroll.value && !userScrolled.value) {
              scrollToBottom()
            }
          }
        } else {
          console.error('Log content response is empty or invalid')
          if (logContent.value === '') {
            logContent.value = ''
          }
        }
        
        loading.value = false
        loadingLogs.value = false
      } catch (err: any) {
        console.error('Error fetching log content:', err)
        error.value = err.message || "获取日志内容失败"
        loading.value = false
        loadingLogs.value = false
      }
    }
    
    // 滚动到底部
    const scrollToBottom = () => {
      nextTick(() => {
        if (logContentRef.value) {
          logContentRef.value.scrollTop = logContentRef.value.scrollHeight
        }
      })
    }
    
    // 设置刷新定时器
    const setupRefreshInterval = () => {
      clearRefreshTimer()
      
      if (refreshInterval.value > 0) {
        refreshTimerId.value = window.setInterval(() => {
          fetchLogContent()
        }, refreshInterval.value * 1000)
      }
    }
    
    // 清除刷新定时器
    const clearRefreshTimer = () => {
      if (refreshTimerId.value !== null) {
        clearInterval(refreshTimerId.value)
        refreshTimerId.value = null
      }
    }
    
    // 手动刷新
    const handleRefresh = () => {
      fetchLogContent()
    }
    
    // 处理滚动事件
    const handleScroll = (e: Event) => {
      const target = e.target as HTMLElement
      const isAtBottom = target.scrollHeight - target.scrollTop - target.clientHeight < 10
      
      userScrolled.value = !isAtBottom
    }
    
    // 处理节点选择变化
    watch(() => selectedLogNode.value, () => {
      logContent.value = ''
      fetchLogContent()
    })
    
    // 处理刷新间隔变化
    watch(() => refreshInterval.value, () => {
      setupRefreshInterval()
    })
    
    // 处理模态框显示状态变化
    watch(() => props.show, (newVal) => {
      if (newVal) {
        fetchLogNodes()
        setupRefreshInterval()
      } else {
        clearRefreshTimer()
      }
    })
    
    // 组件挂载
    onMounted(() => {
      if (props.show) {
        fetchLogNodes()
        setupRefreshInterval()
      }
    })
    
    // 组件卸载
    onUnmounted(() => {
      clearRefreshTimer()
    })
    
    return {
      t,
      logNodes,
      selectedLogNode,
      logContent,
      loading,
      loadingLogs,
      refreshInterval,
      autoScroll,
      logContentRef,
      error,
      refreshIntervalOptions,
      userScrolled,
      handleRefresh,
      handleScroll,
      scrollToBottom
    }
  },
  render() {
    const { t } = this
    
    return (
      <NModal
        show={this.show}
        onUpdateShow={(v: boolean) => this.$emit('update:show', v)}
        title={"查看日志" + (this.jobName ? `: ${this.jobName}` : '')}
        style="width: 90%; max-width: 1600px;"
        preset="card"
      >
        <NSpace vertical size="large">
          {this.error && (
            <NAlert type="error" closable>
              {this.error}
            </NAlert>
          )}
          
          <div class={styles['control-panel']}>
            <div class={styles['control-group']}>
              <label class={styles['control-label']}>日志节点:</label>
              <NSelect
                v-model:value={this.selectedLogNode}
                options={this.logNodes.map(node => ({
                  label: node.node + ' - ' + node.logName,
                  value: node.logLink
                }))}
                style="min-width: 300px;"
                loading={this.loading}
                disabled={this.loading || this.logNodes.length === 0}
              />
            </div>
            
            <div class={styles['control-group']}>
              <div class={styles['control-item']}>
                <label class={styles['control-label']}>自动滚动:</label>
                <NSwitch v-model:value={this.autoScroll} />
              </div>
              <div class={styles['control-item']}>
                <label class={styles['control-label']}>自动刷新:</label>
                <NSelect
                  v-model:value={this.refreshInterval}
                  options={this.refreshIntervalOptions}
                  style="min-width: 120px;"
                />
              </div>
              <NButton onClick={this.handleRefresh} loading={this.loadingLogs} class={styles['refresh-button']}>
                刷新
              </NButton>
            </div>
          </div>
          
          <div class={styles['log-content-container']}>
            {this.loading ? (
              <div class={styles['loading-container']}>
                <NSpin size="large" />
              </div>
            ) : this.logNodes.length === 0 ? (
              <NEmpty description="没有可用的日志" />
            ) : (
              <div 
                class={styles['log-content']} 
                ref="logContentRef"
                onScroll={this.handleScroll}
              >
                {this.loadingLogs ? (
                  <NSpin size="small" />
                ) : (
                  <pre>{this.logContent || "暂无日志内容"}</pre>
                )}
                {/* 添加一个小提示，当用户手动滚动时显示 */}
                {this.userScrolled && this.autoScroll && (
                  <div 
                    style="position: absolute; bottom: 20px; right: 20px; background: rgba(0,0,0,0.6); color: white; padding: 5px 10px; border-radius: 4px; cursor: pointer;"
                    onClick={this.scrollToBottom}
                  >
                    滚动到底部
                  </div>
                )}
              </div>
            )}
          </div>
        </NSpace>
      </NModal>
    )
  }
})

export default LogViewerModal