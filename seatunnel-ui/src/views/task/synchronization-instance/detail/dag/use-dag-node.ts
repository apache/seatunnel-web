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

import '@antv/x6-vue-shape'
import Node from './node'
import { CanvasDesignTokens, getNodeColors, getNodeStateColor, NodeType, NodeState } from './design-tokens'


export interface ModernNodeData {

  id: string
  name: string
  nodeType?: NodeType
  connectorType?: string
  

  theme?: {
    primaryColor: string
    secondaryColor: string
    gradient: string
    borderColor: string
    textColor: string
    iconColor?: string
  }
  

  status?: NodeState
  progress?: number
  statusMessage?: string
  

  isSelected?: boolean
  isHovered?: boolean
  isDragging?: boolean
  isDisabled?: boolean
  

  animations?: {
    entrance?: boolean
    pulse?: boolean
    glow?: boolean
    shake?: boolean
  }
  

  style?: {
    width?: number
    height?: number
    borderRadius?: string
    shadow?: string
    opacity?: number
    zIndex?: number
  }
  

  metadata?: {
    vertexId?: string
    description?: string
    tags?: string[]
    icon?: string
    createdAt?: string
    updatedAt?: string
    [key: string]: any
  }
}


export interface NodeOptions {
  width?: number
  height?: number
  minWidth?: number
  maxWidth?: number
  resizable?: boolean
  rotatable?: boolean
  selectable?: boolean
  movable?: boolean
  portVisible?: boolean
  animationEnabled?: boolean
  themeEnabled?: boolean
}


export function useDagNode(options: NodeOptions = {}) {
  const defaultOptions: NodeOptions = {
    width: CanvasDesignTokens.sizes.node.width,
    height: CanvasDesignTokens.sizes.node.height,
    minWidth: CanvasDesignTokens.sizes.node.minWidth,
    maxWidth: CanvasDesignTokens.sizes.node.maxWidth,
    resizable: false,
    rotatable: false,
    selectable: true,
    movable: true,
    portVisible: true,
    animationEnabled: true,
    themeEnabled: true
  }
  
  const finalOptions = { ...defaultOptions, ...options }
  
  return {
    inherit: 'vue-shape',
    width: finalOptions.width,
    height: finalOptions.height,
    resizing: finalOptions.resizable ? {
      enabled: true,
      minWidth: finalOptions.minWidth,
      maxWidth: finalOptions.maxWidth,
      preserveAspectRatio: false
    } : false,
    rotating: finalOptions.rotatable,
    selecting: finalOptions.selectable,
    moving: finalOptions.movable,
    

    attrs: {
      body: {
        stroke: 'transparent',
        fill: 'transparent',
        rx: parseInt(CanvasDesignTokens.borderRadius.node),
        ry: parseInt(CanvasDesignTokens.borderRadius.node)
      }
    },
    

    ports: {
      groups: {
        input: {
          position: 'left',
          attrs: {
            circle: {
              r: 5,
              magnet: true,
              stroke: CanvasDesignTokens.colors.connections.default,
              strokeWidth: 2,
              fill: '#fff',
              opacity: 0
            }
          }
        },
        output: {
          position: 'right',
          attrs: {
            circle: {
              r: 5,
              magnet: true,
              stroke: CanvasDesignTokens.colors.connections.default,
              strokeWidth: 2,
              fill: '#fff',
              opacity: 0
            }
          }
        }
      },

      items: []
    },
    

    component: Node,
    

    events: {

      'node:mouseenter': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        if (data.isDisabled) return
        

        node.setData({ 
          ...data, 
          isHovered: true,

          animations: finalOptions.animationEnabled ? {
            ...data.animations,
            glow: data.status === 'success' ? true : data.animations?.glow
          } : data.animations
        })
        

        if (finalOptions.portVisible) {

          const ports = node.getPorts() || [];
          const hasInputPort = ports.some((port: any) => port.id === 'input');
          const hasOutputPort = ports.some((port: any) => port.id === 'output');
          
          if (hasInputPort) {
            node.setPortProp('input', 'attrs/circle/opacity', 1);
            node.setPortProp('input', 'attrs/circle/magnet', true);
          }
          if (hasOutputPort) {
            node.setPortProp('output', 'attrs/circle/opacity', 1);
            node.setPortProp('output', 'attrs/circle/magnet', true);
          }
        }
      },
      

      'node:mouseleave': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        if (data.isDisabled) return
        

        node.setData({ 
          ...data, 
          isHovered: false,

          animations: finalOptions.animationEnabled ? {
            ...data.animations,
            glow: data.status === 'success'
          } : data.animations
        })
        

        if (finalOptions.portVisible && !data.isSelected) {

          const ports = node.getPorts() || [];
          const hasInputPort = ports.some((port: any) => port.id === 'input');
          const hasOutputPort = ports.some((port: any) => port.id === 'output');
          
          if (hasInputPort) {
            node.setPortProp('input', 'attrs/circle/opacity', 0);
          }
          if (hasOutputPort) {
            node.setPortProp('output', 'attrs/circle/opacity', 0);
          }
        }
      },
      

      'node:selected': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        if (data.isDisabled) return
        

        node.setData({ ...data, isSelected: true })
        

        if (finalOptions.portVisible) {

          const ports = node.getPorts() || [];
          const hasInputPort = ports.some((port: any) => port.id === 'input');
          const hasOutputPort = ports.some((port: any) => port.id === 'output');
          
          if (hasInputPort) {
            node.setPortProp('input', 'attrs/circle/opacity', 1);
            node.setPortProp('input', 'attrs/circle/magnet', true);
          }
          if (hasOutputPort) {
            node.setPortProp('output', 'attrs/circle/opacity', 1);
            node.setPortProp('output', 'attrs/circle/magnet', true);
          }
        }
      },
      

      'node:unselected': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        

        node.setData({ ...data, isSelected: false })
        

        if (finalOptions.portVisible && !data.isHovered) {

          const ports = node.getPorts() || [];
          const hasInputPort = ports.some((port: any) => port.id === 'input');
          const hasOutputPort = ports.some((port: any) => port.id === 'output');
          
          if (hasInputPort) {
            node.setPortProp('input', 'attrs/circle/opacity', 0);
          }
          if (hasOutputPort) {
            node.setPortProp('output', 'attrs/circle/opacity', 0);
          }
        }
      },
      

      'node:move': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        if (data.isDisabled) return
        

        node.setData({ 
          ...data, 
          isDragging: true,

          animations: finalOptions.animationEnabled ? {
            ...data.animations,
            entrance: false
          } : data.animations
        })
      },
      

      'node:moved': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        

        node.setData({ 
          ...data, 
          isDragging: false,

          metadata: {
            ...data.metadata,
            position: node.getPosition(),
            updatedAt: new Date().toISOString()
          }
        })
      },
      

      'node:resize': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        if (data.isDisabled) return
        

        const size = node.getSize()
        node.setData({
          ...data,
          style: {
            ...data.style,
            width: size.width,
            height: size.height
          }
        })
      },
      

      'node:added': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        

        if (finalOptions.animationEnabled) {
          node.setData({
            ...data,
            animations: {
              ...data.animations,
              entrance: true
            }
          })
        }
        

        if (finalOptions.themeEnabled && data.nodeType && !data.theme) {
          updateNodeTheme(node, data.nodeType)
        }
        

        const nodeType = data.nodeType || determineNodeType(undefined, data.connectorType, data.name);
        

        node.removePorts();
        

        if (nodeType !== 'sink') {
          node.addPort({
            id: 'output',
            group: 'output',
            attrs: {
              circle: {
                magnet: true,
                r: 5,
                stroke: CanvasDesignTokens.colors.connections.default,
                strokeWidth: 2,
                fill: '#fff'
              }
            }
          });
        }
        
        if (nodeType !== 'source') {
          node.addPort({
            id: 'input',
            group: 'input',
            attrs: {
              circle: {
                magnet: true,
                r: 5,
                stroke: CanvasDesignTokens.colors.connections.default,
                strokeWidth: 2,
                fill: '#fff'
              }
            }
          });
        }
      }
    }
  }
}


export function generateNodeTheme(type: NodeType) {
  const colors = getNodeColors(type)
  return {
    primaryColor: colors.primary,
    secondaryColor: colors.secondary,
    gradient: colors.gradient,
    borderColor: colors.border,
    textColor: colors.text,
    iconColor: colors.primary
  }
}


export function getStatusColor(status: NodeState) {
  return getNodeStateColor(status)
}


export function determineNodeType(
  type?: string,
  connector?: string,
  nodeName?: string
): NodeType {
  if (type && ['source', 'sink', 'transform'].includes(type)) {
    return type as NodeType;
  }
  
  const lowerText = [(connector || ''), (nodeName || '')].join(' ').toLowerCase()
  
  if (lowerText.includes('source') || lowerText.includes('input')) {
    return 'source'
  } else if (lowerText.includes('sink') || lowerText.includes('output')) {
    return 'sink'
  } else {
    return 'transform'
  }
}

export function createNodeData(
  id: string,
  name: string,
  type: NodeType,
  options: Partial<ModernNodeData> = {}
): ModernNodeData {

  const theme = generateNodeTheme(type)
  

  const defaultStyle = {
    width: CanvasDesignTokens.sizes.node.width,
    height: CanvasDesignTokens.sizes.node.height,
    borderRadius: CanvasDesignTokens.borderRadius.node,
    shadow: CanvasDesignTokens.shadows.node,
    opacity: 1,
    zIndex: CanvasDesignTokens.zIndex.nodes
  }
  
  return {
    id,
    name,
    nodeType: type,
    theme,
    status: 'idle',
    progress: 0,
    isSelected: false,
    isHovered: false,
    isDragging: false,
    isDisabled: false,
    animations: {
      entrance: true,
      pulse: false,
      glow: false,
      shake: false
    },
    style: {
      ...defaultStyle,
      ...(options.style || {})
    },
    metadata: {
      icon: getNodeTypeIcon(type),
      ...(options.metadata || {})
    },
    ...options
  }
}


function getNodeTypeIcon(type: NodeType): string {
  switch (type) {
    case 'source':
      return 'data-source'
    case 'sink':
      return 'data-target'
    case 'transform':
      return 'data-transform'
    default:
      return 'default-node'
  }
}

export function updateNodeStatus(
  node: any,
  status: NodeState,
  progress?: number,
  statusMessage?: string
) {
  const data = node.getData() as ModernNodeData


  const animations = {
    ...data.animations,
    pulse: status === 'running',
    glow: status === 'success',
    shake: status === 'error'
  }

  node.setData({
    ...data,
    status,
    progress,
    statusMessage,
    animations
  })
}


export function updateNodeTheme(node: any, type: NodeType) {
  const data = node.getData() as ModernNodeData
  const theme = generateNodeTheme(type)

  node.setData({
    ...data,
    nodeType: type,
    theme
  })
}

export function updateNodeMetadata(node: any, metadata: Partial<ModernNodeData['metadata']>) {
  const data = node.getData() as ModernNodeData

  node.setData({
    ...data,
    metadata: {
      ...data.metadata,
      ...metadata,
      updatedAt: new Date().toISOString()
    }
  })
}


export function updateNodeStyle(node: any, style: Partial<ModernNodeData['style']>) {
  const data = node.getData() as ModernNodeData

  node.setData({
    ...data,
    style: {
      ...data.style,
      ...style
    }
  })
}

