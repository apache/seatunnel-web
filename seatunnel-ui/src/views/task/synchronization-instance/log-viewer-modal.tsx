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
    
    // State
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
    
    // Refresh interval options
    const refreshIntervalOptions = [
      { label: t('project.synchronization_instance.refresh_off'), value: 0 },
      { label: t('project.synchronization_instance.refresh_1s'), value: 1 },
      { label: t('project.synchronization_instance.refresh_5s'), value: 5 },
      { label: t('project.synchronization_instance.refresh_10s'), value: 10 },
      { label: t('project.synchronization_instance.refresh_30s'), value: 30 },
      { label: t('project.synchronization_instance.refresh_60s'), value: 60 }
    ]
    
    // Fetch log node list
    const fetchLogNodes = async () => {
      if (!props.jobId) return
      
      loading.value = true
      error.value = ''
      
      try {
        const response = await getLogNodes(props.jobId)
        console.log('Log nodes response:', response)
        
        // Ensure response.data is an array
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
        error.value = err.message || t('project.synchronization_instance.fetch_logs_error')
        loading.value = false
      }
    }
    
    // Fetch log content
    const fetchLogContent = async () => {
      if (!selectedLogNode.value) return
      
      // Only show loading status on first load to avoid flicker when refreshing
      if (logContent.value === '') {
        loadingLogs.value = true
      }
      error.value = ''
      
      try {
        console.log('Fetching log content for:', selectedLogNode.value)
        const response = await getLogContent(selectedLogNode.value)
        console.log('Log content response:', response)
        
        // Check if response.data exists
        if (response && response.data !== undefined) {
          // Ensure log content is a string
          let newContent = '';
          if (typeof response.data === 'string') {
            newContent = response.data
          } else if (typeof response.data === 'object') {
            // If it's an object, convert it to string
            newContent = JSON.stringify(response.data, null, 2)
          } else {
            // For other cases, force convert to string
            newContent = String(response.data)
          }
          
          // Only update content, not replace entire content, to avoid flicker
          if (newContent !== logContent.value) {
            logContent.value = newContent
            console.log('Log content updated:', newContent.substring(0, 100) + '...')
            
            // Only scroll to bottom when auto-scroll is enabled and user hasn't manually scrolled
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
        error.value = err.message || t('project.synchronization_instance.fetch_log_content_error')
        loading.value = false
        loadingLogs.value = false
      }
    }
    
    // Scroll to bottom
    const scrollToBottom = () => {
      nextTick(() => {
        if (logContentRef.value) {
          logContentRef.value.scrollTop = logContentRef.value.scrollHeight
        }
      })
    }
    
    // Set refresh timer
    const setupRefreshInterval = () => {
      clearRefreshTimer()
      
      if (refreshInterval.value > 0) {
        refreshTimerId.value = window.setInterval(() => {
          fetchLogContent()
        }, refreshInterval.value * 1000)
      }
    }
    
    // Clear refresh timer
    const clearRefreshTimer = () => {
      if (refreshTimerId.value !== null) {
        clearInterval(refreshTimerId.value)
        refreshTimerId.value = null
      }
    }
    
    // Manual refresh
    const handleRefresh = () => {
      fetchLogContent()
    }
    
    // Handle scroll event
    const handleScroll = (e: Event) => {
      const target = e.target as HTMLElement
      const isAtBottom = target.scrollHeight - target.scrollTop - target.clientHeight < 10
      
      userScrolled.value = !isAtBottom
    }
    
    // Handle node selection change
    watch(() => selectedLogNode.value, () => {
      logContent.value = ''
      fetchLogContent()
    })
    
    // Handle refresh interval change
    watch(() => refreshInterval.value, () => {
      setupRefreshInterval()
    })
    
    // Handle modal show status change
    watch(() => props.show, (newVal) => {
      if (newVal) {
        fetchLogNodes()
        setupRefreshInterval()
      } else {
        clearRefreshTimer()
      }
    })
    
    // Component mounted
    onMounted(() => {
      if (props.show) {
        fetchLogNodes()
        setupRefreshInterval()
      }
    })
    
    // Component unmounted
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
        title={t('project.synchronization_instance.view_logs') + (this.jobName ? `: ${this.jobName}` : '')}
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
              <label class={styles['control-label']}>{t('project.synchronization_instance.log_node')}:</label>
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
                <label class={styles['control-label']}>{t('project.synchronization_instance.auto_scroll')}:</label>
                <NSwitch v-model:value={this.autoScroll} />
              </div>
              <div class={styles['control-item']}>
                <label class={styles['control-label']}>{t('project.synchronization_instance.auto_refresh')}:</label>
                <NSelect
                  v-model:value={this.refreshInterval}
                  options={this.refreshIntervalOptions}
                  style="min-width: 120px;"
                />
              </div>
              <NButton onClick={this.handleRefresh} loading={this.loadingLogs} class={styles['refresh-button']}>
                {t('project.synchronization_instance.refresh')}
              </NButton>
            </div>
          </div>
          
          <div class={styles['log-content-container']}>
            {this.loading ? (
              <div class={styles['loading-container']}>
                <NSpin size="large" />
              </div>
            ) : this.logNodes.length === 0 ? (
              <NEmpty description={t('project.synchronization_instance.no_logs_available')} />
            ) : (
              <div 
                class={styles['log-content']} 
                ref="logContentRef"
                onScroll={this.handleScroll}
              >
                {this.loadingLogs ? (
                  <NSpin size="small" />
                ) : (
                  <pre>{this.logContent || t('project.synchronization_instance.no_log_content')}</pre>
                )}
                {/* Add a small hint that will be displayed when the user manually scrolls */}
                {this.userScrolled && this.autoScroll && (
                  <div 
                    style="position: absolute; bottom: 20px; right: 20px; background: rgba(0,0,0,0.6); color: white; padding: 5px 10px; border-radius: 4px; cursor: pointer;"
                    onClick={this.scrollToBottom}
                  >
                    {t('project.synchronization_instance.scroll_to_bottom')}
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