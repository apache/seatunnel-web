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


export class CanvasThemeManager {
  private currentTheme = ref<Theme>('light')
  
  constructor() {

    this.initTheme()
    

    watch(this.currentTheme, (newTheme) => {
      this.applyTheme(newTheme)
    })
  }
  

  private initTheme() {

    const savedTheme = localStorage.getItem('canvas-theme') as Theme
    const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    
    this.currentTheme.value = savedTheme || (systemPrefersDark ? 'dark' : 'light')
    

    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
      if (!localStorage.getItem('canvas-theme')) {
        this.currentTheme.value = e.matches ? 'dark' : 'light'
      }
    })
  }
  

  private applyTheme(theme: Theme) {
    const root = document.documentElement
    

    root.setAttribute('data-theme', theme)
    

    localStorage.setItem('canvas-theme', theme)
    

    window.dispatchEvent(new CustomEvent('canvas-theme-change', {
      detail: { theme }
    }))
  }
  

  toggleTheme() {
    this.currentTheme.value = this.currentTheme.value === 'light' ? 'dark' : 'light'
  }
  

  setTheme(theme: Theme) {
    this.currentTheme.value = theme
  }
  

  get theme() {
    return this.currentTheme.value
  }
  

  get themeRef() {
    return this.currentTheme
  }
  

  get canvasColors() {
    return computed(() => CanvasDesignTokens.colors.canvas[this.currentTheme.value])
  }
  

  getNodeColors(type: NodeType) {
    return CanvasDesignTokens.colors.nodes[type]
  }
  

  get connectionColors() {
    return CanvasDesignTokens.colors.connections
  }
  

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
  

  getCSSVariable(variableName: string): string {
    return getComputedStyle(document.documentElement)
      .getPropertyValue(`--canvas-${variableName}`)
      .trim()
  }
  

  setCSSVariable(variableName: string, value: string) {
    document.documentElement.style.setProperty(`--canvas-${variableName}`, value)
  }
  

  get animations() {
    return CanvasDesignTokens.animations
  }
  

  get sizes() {
    return CanvasDesignTokens.sizes
  }
  

  get spacing() {
    return CanvasDesignTokens.spacing
  }
  

  get isDark() {
    return computed(() => this.currentTheme.value === 'dark')
  }
  

  get isLight() {
    return computed(() => this.currentTheme.value === 'light')
  }
}


export const canvasThemeManager = new CanvasThemeManager()


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