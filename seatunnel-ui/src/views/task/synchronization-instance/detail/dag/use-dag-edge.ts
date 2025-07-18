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


export interface ModernEdgeData {
  id: string
  source: string
  target: string
  

  style?: {
    stroke?: string
    strokeWidth?: number
    strokeDasharray?: string
    animated?: boolean
  }
  

  status?: 'normal' | 'active' | 'error' | 'warning' | 'success'
  

  dataFlow?: {
    direction?: 'forward' | 'backward' | 'bidirectional'
    speed?: 'slow' | 'normal' | 'fast'
    volume?: number
  }
  

  isSelected?: boolean
  isHovered?: boolean
  

  label?: string
  

  metadata?: {
    [key: string]: any
  }
}


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
    

    router: {
      name: 'orth'
    },
    connector: {
      name: 'normal'
    },
    attrs: {
      line: {
        stroke: CanvasDesignTokens.colors.connections.default,
        strokeWidth: CanvasDesignTokens.sizes.connection.strokeWidth,
        strokeLinecap: 'round',
        strokeLinejoin: 'round',
        fill: 'none',
        class: 'modern-edge',
        

        ...(finalOptions.animated && {
          strokeDasharray: '8,4',
          class: 'modern-edge modern-edge-animated'
        })
      },
      

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
    

    events: {
      'edge:mouseenter': ({ edge }: any) => {
        const data = edge.getData() || {}
        edge.setData({ ...data, isHovered: true })
        

        edge.attr('line/class', 'modern-edge modern-edge:hover')
        edge.attr('line/stroke', CanvasDesignTokens.colors.connections.hover)
        edge.attr('line/strokeWidth', CanvasDesignTokens.sizes.connection.strokeWidthHover)
      },
      
      'edge:mouseleave': ({ edge }: any) => {
        const data = edge.getData() || {}
        edge.setData({ ...data, isHovered: false })
        

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
        

        edge.attr('line/class', 'modern-edge is-selected')
        edge.attr('line/stroke', CanvasDesignTokens.colors.connections.selected)
        edge.attr('line/strokeWidth', CanvasDesignTokens.sizes.connection.strokeWidthHover)
      },
      
      'edge:unselected': ({ edge }: any) => {
        const data = edge.getData() || {}
        edge.setData({ ...data, isSelected: false })
        

        edge.attr('line/class', 'modern-edge')
        edge.attr('line/stroke', CanvasDesignTokens.colors.connections.default)
        edge.attr('line/strokeWidth', CanvasDesignTokens.sizes.connection.strokeWidth)
      }
    }
  }
}


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


export function updateEdgeStatus(
  edge: any,
  status: 'normal' | 'active' | 'error' | 'warning' | 'success'
) {
  const data = edge.getData() || {}
  edge.setData({ ...data, status })
  

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
  

  if (status === 'error') {
    edge.attr('line/strokeDasharray', '5,5')
  } else if (status === 'warning') {
    edge.attr('line/strokeDasharray', '3,3')
  } else {
    edge.attr('line/strokeDasharray', 'none')
  }
}


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
