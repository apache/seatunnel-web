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

import { CanvasDesignTokens } from './design-tokens'

// 连接线数据接口
export interface ModernEdgeData {
  id: string
  source: string
  target: string
  
  // 视觉属性
  style?: {
    stroke?: string
    strokeWidth?: number
    strokeDasharray?: string
    animated?: boolean
  }
  
  // 状态属性
  status?: 'normal' | 'active' | 'error' | 'warning' | 'success'
  
  // 数据流属性
  dataFlow?: {
    direction?: 'forward' | 'backward' | 'bidirectional'
    speed?: 'slow' | 'normal' | 'fast'
    volume?: number
  }
  
  // 交互属性
  isSelected?: boolean
  isHovered?: boolean
  
  // 标签
  label?: string
  
  // 元数据
  metadata?: {
    [key: string]: any
  }
}

// 连接线配置选项
export interface EdgeOptions {
  router?: 'normal' | 'smooth' | 'orthogonal'
  connector?: 'normal' | 'rounded' | 'smooth'
  animated?: boolean
  showArrow?: boolean
  interactive?: boolean
}

export function useDagEdge(options: EdgeOptions = {}) {
  const defaultOptions: EdgeOptions = {
    router: 'smooth',
    connector: 'smooth',
    animated: false,
    showArrow: true,
    interactive: true
  }
  
  const finalOptions = { ...defaultOptions, ...options }
  
  return {
    inherit: 'edge',
    
    // 路由器配置 - 控制连接线路径
    router: {
      name: 'orth'
    },
    connector: {
      name: 'normal'
    },
    connector: {
      name: 'normal'
    },
    
    // 连接器配置 - 控制连接线样式
    connector: {
      name: 'smooth'
    },
    
    // 基础样式属性
    attrs: {
      line: {
        stroke: CanvasDesignTokens.colors.connections.default,
        strokeWidth: CanvasDesignTokens.sizes.connection.strokeWidth,
        strokeLinecap: 'round',
        strokeLinejoin: 'round',
        fill: 'none',
        class: 'modern-edge',
        
        // 动画效果
        ...(finalOptions.animated && {
          strokeDasharray: '8,4',
          class: 'modern-edge modern-edge-animated'
        })
      },
      
      // 箭头配置
      ...(finalOptions.showArrow && {
        targetMarker: {
          name: 'block',
          size: CanvasDesignTokens.sizes.connection.arrowSize,
          fill: CanvasDesignTokens.colors.connections.default,
          stroke: CanvasDesignTokens.colors.connections.default,
          strokeWidth: 1,
          class: 'modern-edge-arrow'
        }
      }),
      
      // 交互区域（增加点击区域）
      ...(finalOptions.interactive && {
        wrap: {
          stroke: 'transparent',
          strokeWidth: 12,
          fill: 'none',
          cursor: 'pointer',
          class: 'modern-edge-interaction'
        }
      })
    },
    
    // 默认标签配置
    defaultLabel: {
      markup: [
        {
          tagName: 'rect',
          selector: 'body'
        },
        {
          tagName: 'text',
          selector: 'label'
        }
      ],
      attrs: {
        body: {
          ref: 'label',
          fill: 'rgba(255, 255, 255, 0.95)',
          stroke: CanvasDesignTokens.colors.connections.default,
          strokeWidth: 1,
          rx: 4,
          ry: 4,
          refWidth: '100%',
          refHeight: '100%',
          refX: '-50%',
          refY: '-50%',
          class: 'modern-edge-tooltip-bg'
        },
        label: {
          fontSize: CanvasDesignTokens.typography.tooltip.fontSize,
          fontWeight: CanvasDesignTokens.typography.tooltip.fontWeight,
          fill: CanvasDesignTokens.colors.connections.default,
          textAnchor: 'middle',
          textVerticalAnchor: 'middle',
          class: 'modern-edge-label'
        }
      },
      position: {
        distance: 0.5,
        offset: 0
      }
    },
    
    // 事件处理
    events: {
      'edge:mouseenter': ({ edge }: any) => {
        const data = edge.getData() || {}
        edge.setData({ ...data, isHovered: true })
        
        // 添加悬停样式
        edge.attr('line/class', 'modern-edge modern-edge:hover')
        edge.attr('line/stroke', CanvasDesignTokens.colors.connections.hover)
        edge.attr('line/strokeWidth', CanvasDesignTokens.sizes.connection.strokeWidthHover)
      },
      
      'edge:mouseleave': ({ edge }: any) => {
        const data = edge.getData() || {}
        edge.setData({ ...data, isHovered: false })
        
        // 移除悬停样式
        const isSelected = data.isSelected
        edge.attr('line/class', isSelected ? 'modern-edge is-selected' : 'modern-edge')
        edge.attr('line/stroke', isSelected 
          ? CanvasDesignTokens.colors.connections.selected 
          : CanvasDesignTokens.colors.connections.default)
        edge.attr('line/strokeWidth', isSelected 
          ? CanvasDesignTokens.sizes.connection.strokeWidthHover
          : CanvasDesignTokens.sizes.connection.strokeWidth)
      },
      
      'edge:selected': ({ edge }: any) => {
        const data = edge.getData() || {}
        edge.setData({ ...data, isSelected: true })
        
        // 添加选中样式
        edge.attr('line/class', 'modern-edge is-selected')
        edge.attr('line/stroke', CanvasDesignTokens.colors.connections.selected)
        edge.attr('line/strokeWidth', CanvasDesignTokens.sizes.connection.strokeWidthHover)
      },
      
      'edge:unselected': ({ edge }: any) => {
        const data = edge.getData() || {}
        edge.setData({ ...data, isSelected: false })
        
        // 移除选中样式
        edge.attr('line/class', 'modern-edge')
        edge.attr('line/stroke', CanvasDesignTokens.colors.connections.default)
        edge.attr('line/strokeWidth', CanvasDesignTokens.sizes.connection.strokeWidth)
      }
    }
  }
}

// 工具函数：创建连接线数据
export function createEdgeData(
  id: string,
  source: string,
  target: string,
  options: Partial<ModernEdgeData> = {}
): ModernEdgeData {
  return {
    id,
    source,
    target,
    status: 'normal',
    isSelected: false,
    isHovered: false,
    style: {
      animated: false
    },
    dataFlow: {
      direction: 'forward',
      speed: 'normal'
    },
    metadata: {},
    ...options
  }
}

// 工具函数：更新连接线状态
export function updateEdgeStatus(
  edge: any,
  status: 'normal' | 'active' | 'error' | 'warning' | 'success'
) {
  const data = edge.getData() || {}
  edge.setData({ ...data, status })
  
  // 应用状态样式
  const statusColors = {
    normal: CanvasDesignTokens.colors.connections.default,
    active: CanvasDesignTokens.colors.connections.animated,
    error: CanvasDesignTokens.colors.nodes.states.error,
    warning: CanvasDesignTokens.colors.nodes.states.warning,
    success: CanvasDesignTokens.colors.nodes.states.success
  }
  
  const statusClasses = {
    normal: 'modern-edge',
    active: 'modern-edge is-active',
    error: 'modern-edge is-error',
    warning: 'modern-edge is-warning',
    success: 'modern-edge is-success'
  }
  
  edge.attr('line/stroke', statusColors[status])
  edge.attr('line/class', statusClasses[status])
  
  // 错误状态添加虚线
  if (status === 'error') {
    edge.attr('line/strokeDasharray', '5,5')
  } else if (status === 'warning') {
    edge.attr('line/strokeDasharray', '3,3')
  } else {
    edge.attr('line/strokeDasharray', 'none')
  }
}

// 工具函数：启用数据流动画
export function enableDataFlowAnimation(
  edge: any,
  speed: 'slow' | 'normal' | 'fast' = 'normal'
) {
  const data = edge.getData() || {}
  edge.setData({ 
    ...data, 
    style: { 
      ...data.style, 
      animated: true 
    },
    dataFlow: {
      ...data.dataFlow,
      speed
    }
  })
  
  const speedClasses = {
    slow: 'modern-edge modern-edge-animated flow-slow',
    normal: 'modern-edge modern-edge-animated',
    fast: 'modern-edge modern-edge-animated flow-fast'
  }
  
  edge.attr('line/class', speedClasses[speed])
  edge.attr('line/strokeDasharray', '8,4')
}
