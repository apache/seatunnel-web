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

/**
 * 现代化画布设计令牌系统
 * 定义统一的颜色、阴影、动画等设计变量
 */
export const CanvasDesignTokens = {
  // 颜色系统
  colors: {
    // 节点颜色系统
    nodes: {
      source: {
        primary: '#10B981',      // 绿色主色 - 数据源
        secondary: '#D1FAE5',    // 浅绿背景
        gradient: 'linear-gradient(135deg, #10B981 0%, #059669 100%)',
        border: '#059669',
        text: '#065F46'
      },
      sink: {
        primary: '#3B82F6',      // 蓝色主色 - 数据目标
        secondary: '#DBEAFE',    // 浅蓝背景
        gradient: 'linear-gradient(135deg, #3B82F6 0%, #1D4ED8 100%)',
        border: '#1D4ED8',
        text: '#1E3A8A'
      },
      transform: {
        primary: '#8B5CF6',      // 紫色主色 - 数据转换
        secondary: '#EDE9FE',    // 浅紫背景
        gradient: 'linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%)',
        border: '#7C3AED',
        text: '#5B21B6'
      },
      // 通用节点状态
      states: {
        idle: '#6B7280',
        running: '#F59E0B',
        success: '#10B981',
        error: '#EF4444',
        warning: '#F59E0B'
      }
    },
    
    // 连接线颜色
    connections: {
      default: '#6B7280',
      hover: '#374151',
      active: '#1F2937',
      selected: '#3B82F6',
      gradient: 'linear-gradient(90deg, #6B7280 0%, #374151 100%)',
      animated: '#3B82F6'
    },
    
    // 画布背景颜色
    canvas: {
      light: {
        background: '#FAFAFA',
        grid: '#E5E7EB',
        gridSecondary: '#F3F4F6',
        guideLine: '#3B82F6'
      },
      dark: {
        background: '#111827',
        grid: '#374151',
        gridSecondary: '#1F2937',
        guideLine: '#60A5FA'
      }
    },
    
    // 小地图颜色
    minimap: {
      background: 'rgba(255, 255, 255, 0.95)',
      backgroundDark: 'rgba(17, 24, 39, 0.95)',
      border: '#E5E7EB',
      borderDark: '#374151',
      viewport: 'rgba(59, 130, 246, 0.2)',
      viewportBorder: '#3B82F6'
    }
  },
  
  // 阴影系统
  shadows: {
    node: '0 4px 12px rgba(0, 0, 0, 0.08)',
    nodeHover: '0 8px 24px rgba(0, 0, 0, 0.12)',
    nodeActive: '0 12px 32px rgba(0, 0, 0, 0.16)',
    nodeSelected: '0 0 0 2px rgba(59, 130, 246, 0.3)',
    minimap: '0 4px 16px rgba(0, 0, 0, 0.1)',
    tooltip: '0 2px 8px rgba(0, 0, 0, 0.1)'
  },
  
  // 边框半径
  borderRadius: {
    node: '8px',
    minimap: '6px',
    tooltip: '4px',
    button: '4px'
  },
  
  // 间距系统
  spacing: {
    xs: '4px',
    sm: '8px',
    md: '12px',
    lg: '16px',
    xl: '24px',
    xxl: '32px'
  },
  
  // 字体系统
  typography: {
    node: {
      fontSize: '12px',
      fontWeight: '500',
      lineHeight: '1.2'
    },
    tooltip: {
      fontSize: '11px',
      fontWeight: '400',
      lineHeight: '1.4'
    },
    minimap: {
      fontSize: '10px',
      fontWeight: '500',
      lineHeight: '1.2'
    }
  },
  
  // 动画系统
  animations: {
    // 持续时间
    duration: {
      fast: '150ms',
      normal: '250ms',
      slow: '350ms',
      slower: '500ms'
    },
    
    // 缓动函数
    easing: {
      default: 'cubic-bezier(0.4, 0, 0.2, 1)',
      bounce: 'cubic-bezier(0.68, -0.55, 0.265, 1.55)',
      smooth: 'cubic-bezier(0.25, 0.46, 0.45, 0.94)'
    },
    
    // 动画配置
    configs: {
      nodeEntrance: {
        duration: '350ms',
        easing: 'cubic-bezier(0.68, -0.55, 0.265, 1.55)',
        delay: '0ms'
      },
      nodeHover: {
        duration: '150ms',
        easing: 'cubic-bezier(0.4, 0, 0.2, 1)'
      },
      edgeFlow: {
        duration: '2000ms',
        easing: 'linear',
        iterationCount: 'infinite'
      },
      themeTransition: {
        duration: '250ms',
        easing: 'cubic-bezier(0.4, 0, 0.2, 1)'
      }
    }
  },
  
  // 尺寸系统
  sizes: {
    node: {
      width: 150,
      height: 36,
      minWidth: 120,
      maxWidth: 200
    },
    minimap: {
      width: 200,
      height: 120,
      minWidth: 150,
      minHeight: 90
    },
    connection: {
      strokeWidth: 2,
      strokeWidthHover: 3,
      arrowSize: 8
    }
  },
  
  // Z-index 层级
  zIndex: {
    canvas: 1,
    nodes: 10,
    connections: 5,
    minimap: 1000,
    tooltip: 2000,
    modal: 3000
  }
} as const

// 类型定义
export type NodeType = 'source' | 'sink' | 'transform'
export type NodeState = 'idle' | 'running' | 'success' | 'error' | 'warning'
export type Theme = 'light' | 'dark'

// 工具函数
export const getNodeColors = (type: NodeType) => {
  return CanvasDesignTokens.colors.nodes[type]
}

export const getNodeStateColor = (state: NodeState) => {
  return CanvasDesignTokens.colors.nodes.states[state]
}

export const getCanvasColors = (theme: Theme) => {
  return CanvasDesignTokens.colors.canvas[theme]
}