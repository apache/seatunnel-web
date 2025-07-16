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

import { ref, computed, watch } from 'vue'
import { CanvasDesignTokens, type Theme, type NodeType } from './design-tokens'

/**
 * 画布主题管理器
 * 提供主题切换和样式应用功能
 */
export class CanvasThemeManager {
  private currentTheme = ref<Theme>('light')
  
  constructor() {
    // 初始化主题
    this.initTheme()
    
    // 监听主题变化
    watch(this.currentTheme, (newTheme) => {
      this.applyTheme(newTheme)
    })
  }
  
  /**
   * 初始化主题
   */
  private initTheme() {
    // 从系统偏好或本地存储获取主题
    const savedTheme = localStorage.getItem('canvas-theme') as Theme
    const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    
    this.currentTheme.value = savedTheme || (systemPrefersDark ? 'dark' : 'light')
    
    // 监听系统主题变化
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
      if (!localStorage.getItem('canvas-theme')) {
        this.currentTheme.value = e.matches ? 'dark' : 'light'
      }
    })
  }
  
  /**
   * 应用主题到DOM
   */
  private applyTheme(theme: Theme) {
    const root = document.documentElement
    
    // 设置主题属性
    root.setAttribute('data-theme', theme)
    
    // 保存到本地存储
    localStorage.setItem('canvas-theme', theme)
    
    // 触发主题变化事件
    window.dispatchEvent(new CustomEvent('canvas-theme-change', {
      detail: { theme }
    }))
  }
  
  /**
   * 切换主题
   */
  toggleTheme() {
    this.currentTheme.value = this.currentTheme.value === 'light' ? 'dark' : 'light'
  }
  
  /**
   * 设置主题
   */
  setTheme(theme: Theme) {
    this.currentTheme.value = theme
  }
  
  /**
   * 获取当前主题
   */
  get theme() {
    return this.currentTheme.value
  }
  
  /**
   * 获取主题响应式引用
   */
  get themeRef() {
    return this.currentTheme
  }
  
  /**
   * 获取当前主题的画布颜色
   */
  get canvasColors() {
    return computed(() => CanvasDesignTokens.colors.canvas[this.currentTheme.value])
  }
  
  /**
   * 获取节点颜色
   */
  getNodeColors(type: NodeType) {
    return CanvasDesignTokens.colors.nodes[type]
  }
  
  /**
   * 获取连接线颜色
   */
  get connectionColors() {
    return CanvasDesignTokens.colors.connections
  }
  
  /**
   * 获取小地图颜色
   */
  get minimapColors() {
    return computed(() => ({
      background: this.currentTheme.value === 'light' 
        ? CanvasDesignTokens.colors.minimap.background
        : CanvasDesignTokens.colors.minimap.backgroundDark,
      border: this.currentTheme.value === 'light'
        ? CanvasDesignTokens.colors.minimap.border
        : CanvasDesignTokens.colors.minimap.borderDark,
      viewport: CanvasDesignTokens.colors.minimap.viewport,
      viewportBorder: CanvasDesignTokens.colors.minimap.viewportBorder
    }))
  }
  
  /**
   * 获取CSS变量值
   */
  getCSSVariable(variableName: string): string {
    return getComputedStyle(document.documentElement)
      .getPropertyValue(`--canvas-${variableName}`)
      .trim()
  }
  
  /**
   * 设置CSS变量值
   */
  setCSSVariable(variableName: string, value: string) {
    document.documentElement.style.setProperty(`--canvas-${variableName}`, value)
  }
  
  /**
   * 获取动画配置
   */
  get animations() {
    return CanvasDesignTokens.animations
  }
  
  /**
   * 获取尺寸配置
   */
  get sizes() {
    return CanvasDesignTokens.sizes
  }
  
  /**
   * 获取间距配置
   */
  get spacing() {
    return CanvasDesignTokens.spacing
  }
  
  /**
   * 检查是否为暗黑主题
   */
  get isDark() {
    return computed(() => this.currentTheme.value === 'dark')
  }
  
  /**
   * 检查是否为明亮主题
   */
  get isLight() {
    return computed(() => this.currentTheme.value === 'light')
  }
}

// 创建全局主题管理器实例
export const canvasThemeManager = new CanvasThemeManager()

// 导出便捷的组合式函数
export function useCanvasTheme() {
  return {
    theme: canvasThemeManager.themeRef,
    isDark: canvasThemeManager.isDark,
    isLight: canvasThemeManager.isLight,
    canvasColors: canvasThemeManager.canvasColors,
    minimapColors: canvasThemeManager.minimapColors,
    connectionColors: canvasThemeManager.connectionColors,
    animations: canvasThemeManager.animations,
    sizes: canvasThemeManager.sizes,
    spacing: canvasThemeManager.spacing,
    toggleTheme: () => canvasThemeManager.toggleTheme(),
    setTheme: (theme: Theme) => canvasThemeManager.setTheme(theme),
    getNodeColors: (type: NodeType) => canvasThemeManager.getNodeColors(type),
    getCSSVariable: (name: string) => canvasThemeManager.getCSSVariable(name),
    setCSSVariable: (name: string, value: string) => canvasThemeManager.setCSSVariable(name, value)
  }
}