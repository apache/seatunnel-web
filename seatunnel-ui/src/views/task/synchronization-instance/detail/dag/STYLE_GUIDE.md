# 现代化画布样式指南

## 概述

本文档描述了 SeaTunnel Web UI 现代化画布的样式系统，包括设计令牌、组件样式和最佳实践。

## 设计令牌系统

### 颜色系统

#### 节点颜色
```scss
// Source 节点（数据源）- 绿色系
--canvas-node-source-primary: #10B981;
--canvas-node-source-secondary: #D1FAE5;
--canvas-node-source-gradient: linear-gradient(135deg, #10B981 0%, #059669 100%);

// Sink 节点（数据目标）- 蓝色系
--canvas-node-sink-primary: #3B82F6;
--canvas-node-sink-secondary: #DBEAFE;
--canvas-node-sink-gradient: linear-gradient(135deg, #3B82F6 0%, #1D4ED8 100%);

// Transform 节点（数据转换）- 紫色系
--canvas-node-transform-primary: #8B5CF6;
--canvas-node-transform-secondary: #EDE9FE;
--canvas-node-transform-gradient: linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%);
```

#### 连接线颜色
```scss
--canvas-connection-default: #6B7280;
--canvas-connection-hover: #374151;
--canvas-connection-selected: #3B82F6;
--canvas-connection-animated: #3B82F6;
```

#### 画布背景颜色
```scss
// 明亮主题
--canvas-bg: #FAFAFA;
--canvas-grid: #E5E7EB;
--canvas-grid-secondary: #F3F4F6;

// 暗黑主题
[data-theme="dark"] {
  --canvas-bg: #111827;
  --canvas-grid: #374151;
  --canvas-grid-secondary: #1F2937;
}
```

### 阴影系统

```scss
--canvas-shadow-node: 0 4px 12px rgba(0, 0, 0, 0.08);
--canvas-shadow-node-hover: 0 8px 24px rgba(0, 0, 0, 0.12);
--canvas-shadow-node-active: 0 12px 32px rgba(0, 0, 0, 0.16);
--canvas-shadow-minimap: 0 4px 16px rgba(0, 0, 0, 0.1);
```

### 动画系统

```scss
--canvas-animation-fast: 150ms;
--canvas-animation-normal: 250ms;
--canvas-animation-slow: 350ms;
--canvas-easing-default: cubic-bezier(0.4, 0, 0.2, 1);
--canvas-easing-bounce: cubic-bezier(0.68, -0.55, 0.265, 1.55);
```

## 组件样式

### 节点样式

#### 基础节点
```scss
.dag-node {
  display: flex;
  align-items: center;
  height: 100%;
  position: relative;
  background: #fff;
  border-radius: var(--canvas-border-radius-node);
  box-shadow: var(--canvas-shadow-node);
  transition: all var(--canvas-animation-normal) var(--canvas-easing-default);
}
```

#### 节点类型样式
```scss
.dag-node.node-type-source {
  background: var(--canvas-node-source-gradient);
  border-color: var(--canvas-node-source-border);
  color: var(--canvas-node-source-text);
}

.dag-node.node-type-sink {
  background: var(--canvas-node-sink-gradient);
  border-color: var(--canvas-node-sink-border);
  color: var(--canvas-node-sink-text);
}

.dag-node.node-type-transform {
  background: var(--canvas-node-transform-gradient);
  border-color: var(--canvas-node-transform-border);
  color: var(--canvas-node-transform-text);
}
```

#### 节点状态样式
```scss
.dag-node.state-running {
  animation: canvas-node-pulse 2s ease-in-out infinite;
}

.dag-node.state-success {
  animation: canvas-node-pulse-success 2s ease-in-out infinite;
}

.dag-node.state-error {
  animation: canvas-node-pulse-error 2s ease-in-out infinite;
}
```

### 连接线样式

#### 基础连接线
```scss
.modern-edge {
  stroke: var(--canvas-connection-default);
  stroke-width: var(--canvas-connection-stroke-width);
  fill: none;
  transition: stroke var(--canvas-animation-normal) var(--canvas-easing-default);
}
```

#### 连接线状态
```scss
.modern-edge:hover {
  stroke: var(--canvas-connection-hover);
  stroke-width: var(--canvas-connection-stroke-width-hover);
}

.modern-edge.is-selected {
  stroke: var(--canvas-connection-selected);
  animation: canvas-connection-pulse 1.5s ease-in-out infinite;
}

.modern-edge.is-active {
  stroke-dasharray: 8, 4;
  animation: canvas-data-flow 2s linear infinite;
}
```

### 小地图样式

```scss
.modern-minimap {
  position: absolute;
  right: var(--canvas-spacing-lg);
  bottom: var(--canvas-spacing-lg);
  background: var(--canvas-minimap-bg);
  border-radius: var(--canvas-border-radius-minimap);
  box-shadow: var(--canvas-shadow-minimap);
  backdrop-filter: blur(8px);
}
```

## 动画效果

### 节点动画

#### 入场动画
```scss
@keyframes canvas-node-entrance {
  0% {
    opacity: 0;
    transform: scale(0.8) translateY(10px);
  }
  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
```

#### 脉冲动画
```scss
@keyframes canvas-node-pulse {
  0%, 100% {
    box-shadow: var(--canvas-shadow-node), 0 0 0 0 rgba(59, 130, 246, 0.4);
  }
  50% {
    box-shadow: var(--canvas-shadow-node), 0 0 0 8px rgba(59, 130, 246, 0);
  }
}
```

### 连接线动画

#### 数据流动画
```scss
@keyframes canvas-data-flow {
  0% {
    stroke-dashoffset: 20;
  }
  100% {
    stroke-dashoffset: 0;
  }
}
```

## 响应式设计

### 断点系统
```scss
$canvas-breakpoints: (
  'mobile': 768px,
  'tablet': 1024px,
  'desktop': 1440px,
  'wide': 1920px
);
```

### 响应式混入
```scss
@mixin canvas-respond-to($breakpoint) {
  @media (min-width: map-get($canvas-breakpoints, $breakpoint)) {
    @content;
  }
}
```

### 移动设备适配
```scss
@include canvas-respond-to-max('mobile') {
  .dag-node {
    width: calc(var(--canvas-node-width) * 0.8);
    height: calc(var(--canvas-node-height) * 0.8);
  }
  
  .modern-minimap {
    width: calc(var(--canvas-minimap-width) * 0.75);
    height: calc(var(--canvas-minimap-height) * 0.75);
  }
}
```

## 主题系统

### 主题切换
```typescript
import { useCanvasTheme } from './theme-manager'

const { theme, toggleTheme, setTheme } = useCanvasTheme()

// 切换主题
toggleTheme()

// 设置特定主题
setTheme('dark')
```

### 主题适配
```scss
[data-theme="dark"] {
  .dag-node {
    background: rgba(31, 41, 55, 0.8);
    backdrop-filter: blur(8px);
  }
  
  .modern-edge {
    filter: brightness(1.2);
  }
}
```

## 最佳实践

### 1. 使用设计令牌
始终使用 CSS 变量而不是硬编码的颜色值：
```scss
// ✅ 推荐
color: var(--canvas-node-source-primary);

// ❌ 不推荐
color: #10B981;
```

### 2. 响应式优先
使用响应式混入确保在所有设备上的良好体验：
```scss
.my-component {
  @include canvas-node-responsive;
  @include canvas-shadow-responsive;
}
```

### 3. 动画性能
使用 CSS 变换而不是改变布局属性：
```scss
// ✅ 推荐
transform: translateY(-2px);

// ❌ 不推荐
top: -2px;
```

### 4. 主题兼容
确保组件在明暗主题下都有良好的对比度：
```scss
.my-component {
  color: var(--canvas-connection-default);
  
  [data-theme="dark"] & {
    color: var(--canvas-connection-default);
  }
}
```

### 5. 性能优化
对于大量节点的场景，使用性能优化类：
```scss
.canvas-performance-mode {
  .dag-node {
    transition: none;
    animation: none;
  }
}
```

## 自定义扩展

### 添加新的节点类型
1. 在设计令牌中定义颜色：
```scss
:root {
  --canvas-node-custom-primary: #FF6B6B;
  --canvas-node-custom-secondary: #FFE0E0;
  --canvas-node-custom-gradient: linear-gradient(135deg, #FF6B6B 0%, #FF5252 100%);
}
```

2. 添加节点样式：
```scss
.dag-node.node-type-custom {
  background: var(--canvas-node-custom-gradient);
  border-color: var(--canvas-node-custom-primary);
  color: var(--canvas-node-custom-text);
}
```

3. 更新类型定义：
```typescript
export type NodeType = 'source' | 'sink' | 'transform' | 'custom'
```

### 添加新的动画效果
```scss
@keyframes my-custom-animation {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.my-custom-animation {
  animation: my-custom-animation 1s ease-in-out infinite;
}
```

## 故障排除

### 常见问题

1. **样式不生效**
   - 确保正确导入了样式文件
   - 检查 CSS 变量是否正确定义
   - 验证选择器优先级

2. **动画卡顿**
   - 检查是否启用了性能模式
   - 减少同时运行的动画数量
   - 使用 `will-change` 属性优化性能

3. **主题切换异常**
   - 确保主题管理器正确初始化
   - 检查 CSS 变量的作用域
   - 验证主题切换事件监听

4. **响应式问题**
   - 检查断点定义是否正确
   - 验证媒体查询语法
   - 测试不同屏幕尺寸

## 更新日志

### v1.0.0 (2024-01-16)
- 初始版本发布
- 完整的设计令牌系统
- 现代化节点和连接线样式
- 响应式设计支持
- 明暗主题切换
- 性能优化和动画系统