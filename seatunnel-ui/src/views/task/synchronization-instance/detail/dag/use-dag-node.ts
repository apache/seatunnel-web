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

// 扩展的节点数据接口
export interface ModernNodeData {
  // 基础属性
  id: string
  name: string
  nodeType?: NodeType
  connectorType?: string
  
  // 视觉属性
  theme?: {
    primaryColor: string
    secondaryColor: string
    gradient: string
    borderColor: string
    textColor: string
    iconColor?: string
  }
  
  // 状态属性
  status?: NodeState
  progress?: number
  statusMessage?: string
  
  // 交互属性
  isSelected?: boolean
  isHovered?: boolean
  isDragging?: boolean
  isDisabled?: boolean
  
  // 动画属性
  animations?: {
    entrance?: boolean
    pulse?: boolean
    glow?: boolean
    shake?: boolean
  }
  
  // 样式属性
  style?: {
    width?: number
    height?: number
    borderRadius?: string
    shadow?: string
    opacity?: number
    zIndex?: number
  }
  
  // 元数据
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

/**
 * 节点配置选项接口
 */
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

/**
 * 创建节点配置
 * @param options 节点选项
 * @returns 节点配置对象
 */
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
    
    // 节点样式配置
    attrs: {
      body: {
        stroke: 'transparent',
        fill: 'transparent',
        rx: parseInt(CanvasDesignTokens.borderRadius.node),
        ry: parseInt(CanvasDesignTokens.borderRadius.node)
      }
    },
    
    // 连接点配置
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
              opacity: 0 // 默认隐藏，只在悬停或选中时显示
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
              opacity: 0 // 默认隐藏，只在悬停或选中时显示
            }
          }
        }
      },
      // 连接点将在节点添加时根据节点类型动态设置
      items: []
    },
    
    // Vue组件配置
    component: Node,
    
    // 事件处理
    events: {
      // 鼠标悬停事件
      'node:mouseenter': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        if (data.isDisabled) return
        
        // 更新悬停状态
        node.setData({ 
          ...data, 
          isHovered: true,
          // 如果启用了动画，添加发光效果
          animations: finalOptions.animationEnabled ? {
            ...data.animations,
            glow: data.status === 'success' ? true : data.animations?.glow
          } : data.animations
        })
        
        // 显示连接点 - 只显示节点拥有的连接点
        if (finalOptions.portVisible) {
          // 检查节点是否有输入端口和输出端口
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
      
      // 鼠标离开事件
      'node:mouseleave': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        if (data.isDisabled) return
        
        // 更新悬停状态
        node.setData({ 
          ...data, 
          isHovered: false,
          // 如果启用了动画，移除发光效果（除非状态是成功）
          animations: finalOptions.animationEnabled ? {
            ...data.animations,
            glow: data.status === 'success'
          } : data.animations
        })
        
        // 隐藏连接点（除非节点被选中）
        if (finalOptions.portVisible && !data.isSelected) {
          // 检查节点是否有输入端口和输出端口
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
      
      // 节点选中事件
      'node:selected': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        if (data.isDisabled) return
        
        // 更新选中状态
        node.setData({ ...data, isSelected: true })
        
        // 显示连接点 - 只显示节点拥有的连接点
        if (finalOptions.portVisible) {
          // 检查节点是否有输入端口和输出端口
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
      
      // 节点取消选中事件
      'node:unselected': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        
        // 更新选中状态
        node.setData({ ...data, isSelected: false })
        
        // 如果不是悬停状态，隐藏连接点
        if (finalOptions.portVisible && !data.isHovered) {
          // 检查节点是否有输入端口和输出端口
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
      
      // 节点开始移动事件
      'node:move': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        if (data.isDisabled) return
        
        // 更新拖拽状态
        node.setData({ 
          ...data, 
          isDragging: true,
          // 如果启用了动画，添加拖拽动画
          animations: finalOptions.animationEnabled ? {
            ...data.animations,
            entrance: false
          } : data.animations
        })
      },
      
      // 节点移动结束事件
      'node:moved': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        
        // 更新拖拽状态
        node.setData({ 
          ...data, 
          isDragging: false,
          // 更新节点元数据中的位置信息
          metadata: {
            ...data.metadata,
            position: node.getPosition(),
            updatedAt: new Date().toISOString()
          }
        })
      },
      
      // 节点大小调整事件
      'node:resize': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        if (data.isDisabled) return
        
        // 更新节点样式中的尺寸
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
      
      // 节点添加事件
      'node:added': ({ node }: any) => {
        const data = node.getData() as ModernNodeData
        
        // 如果启用了动画，添加入场动画
        if (finalOptions.animationEnabled) {
          node.setData({
            ...data,
            animations: {
              ...data.animations,
              entrance: true
            }
          })
        }
        
        // 如果启用了主题，应用节点类型主题
        if (finalOptions.themeEnabled && data.nodeType && !data.theme) {
          updateNodeTheme(node, data.nodeType)
        }
        
        // 根据节点类型设置连接点
        const nodeType = data.nodeType || determineNodeType(undefined, data.connectorType, data.name);
        
        // 清除现有的连接点
        node.removePorts();
        
        // 添加适合节点类型的连接点
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

/**
 * 根据节点类型生成主题颜色
 * @param type 节点类型
 * @returns 节点主题颜色对象
 */
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

/**
 * 根据节点状态获取状态颜色
 * @param status 节点状态
 * @returns 状态颜色
 */
export function getStatusColor(status: NodeState) {
  return getNodeStateColor(status)
}

/**
 * 确定节点类型
 * @param type 显式指定的类型
 * @param connector 连接器类型
 * @param nodeName 节点名称
 * @returns 节点类型
 */
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

/**
 * 工具函数：创建节点数据
 * @param id 节点ID
 * @param name 节点名称
 * @param type 节点类型
 * @param options 其他选项
 * @returns 完整的节点数据对象
 */
export function createNodeData(
  id: string,
  name: string,
  type: NodeType,
  options: Partial<ModernNodeData> = {}
): ModernNodeData {
  // 生成节点主题
  const theme = generateNodeTheme(type)
  
  // 默认样式
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

/**
 * 获取节点类型对应的图标
 * @param type 节点类型
 * @returns 图标标识符
 */
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

/**
 * 工具函数：更新节点状态
 * @param node 节点对象
 * @param status 状态
 * @param progress 进度值
 * @param statusMessage 状态消息
 */
export function updateNodeStatus(
  node: any,
  status: NodeState,
  progress?: number,
  statusMessage?: string
) {
  const data = node.getData() as ModernNodeData
  
  // 根据状态设置动画效果
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

/**
 * 工具函数：更新节点主题
 * @param node 节点对象
 * @param type 节点类型
 */
export function updateNodeTheme(node: any, type: NodeType) {
  const data = node.getData() as ModernNodeData
  const theme = generateNodeTheme(type)
  
  node.setData({
    ...data,
    nodeType: type,
    theme
  })
}

/**
 * 工具函数：更新节点元数据
 * @param node 节点对象
 * @param metadata 元数据对象
 */
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

/**
 * 工具函数：更新节点样式
 * @param node 节点对象
 * @param style 样式对象
 */
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
