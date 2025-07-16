# 现代化画布升级指南

## 概述

SeaTunnel Web UI 画布已升级为现代化设计，提供更好的视觉体验和交互性能。本次升级完全向后兼容，不会影响现有功能。

## 新功能特性

### ✨ 视觉改进
- **现代化节点设计**: 渐变背景、圆角边框、阴影效果
- **智能节点类型识别**: 自动根据连接器类型应用相应的颜色主题
  - Source 节点：绿色主题 🟢
  - Sink 节点：蓝色主题 🔵  
  - Transform 节点：紫色主题 🟣
- **平滑连接线**: 贝塞尔曲线替代直线，支持数据流动画
- **现代化小地图**: 半透明背景、圆角设计、增强交互

### 🎨 主题系统
- **明暗主题切换**: 自动适配系统主题偏好
- **平滑过渡动画**: 主题切换时的流畅视觉效果
- **高对比度支持**: 确保在所有主题下的可读性

### 📱 响应式设计
- **移动设备优化**: 自适应不同屏幕尺寸
- **触摸友好**: 增大触摸区域，优化移动端操作
- **高分辨率支持**: 在高DPI屏幕上的清晰显示

### ⚡ 性能优化
- **智能渲染**: 大量节点时自动启用性能模式
- **动画优化**: 硬件加速和帧率监控
- **视口裁剪**: 只渲染可见区域的元素

### 🎭 丰富动画
- **节点入场动画**: 新节点添加时的弹性动画
- **状态指示动画**: 运行、成功、错误状态的脉冲效果
- **交互反馈**: 悬停、选中、拖拽时的视觉反馈

## 使用方法

### 基本使用
无需任何代码更改，新的样式会自动应用到现有画布。

### 主题切换
```typescript
import { useCanvasTheme } from './dag/theme-manager'

const { toggleTheme, setTheme } = useCanvasTheme()

// 切换主题
toggleTheme()

// 设置特定主题
setTheme('dark')
```

### 节点状态控制
```typescript
import { updateNodeStatus } from './dag/use-dag-node'

// 更新节点状态
updateNodeStatus(node, 'running', 50) // 运行状态，进度50%
updateNodeStatus(node, 'success')     // 成功状态
updateNodeStatus(node, 'error')       // 错误状态
```

### 连接线动画
```typescript
import { enableDataFlowAnimation } from './dag/use-dag-edge'

// 启用数据流动画
enableDataFlowAnimation(edge, 'fast')   // 快速流动
enableDataFlowAnimation(edge, 'normal') // 正常速度
enableDataFlowAnimation(edge, 'slow')   // 慢速流动
```

## 兼容性说明

### ✅ 完全兼容
- 所有现有API保持不变
- 现有数据结构无需修改
- 原有事件处理继续有效
- 现有配置选项仍然支持

### 🔄 增强功能
- 节点数据现在支持更多属性（可选）
- 连接线支持更多状态和样式（可选）
- 新增主题管理功能（可选使用）

### 📦 新增文件
```
dag/
├── design-tokens.ts          # 设计令牌系统
├── theme-manager.ts          # 主题管理器
├── canvas-variables.scss     # CSS变量系统
├── canvas-animations.scss    # 动画定义
├── responsive-mixins.scss    # 响应式混入
├── edge-styles.scss          # 连接线样式
├── minimap-styles.scss       # 小地图样式
├── canvas-background.scss    # 背景样式
├── STYLE_GUIDE.md           # 样式指南
└── UPGRADE_GUIDE.md         # 升级指南
```

## 性能影响

### 📈 性能提升
- **渲染优化**: 60fps流畅体验
- **内存优化**: 智能垃圾回收
- **加载优化**: 按需加载样式资源

### 🔧 性能监控
系统会自动监控渲染性能，当帧率低于30fps时自动启用性能模式：
- 禁用复杂动画
- 简化视觉效果
- 启用虚拟化渲染

## 故障排除

### 样式不显示
1. 确保浏览器支持CSS变量
2. 检查是否有样式冲突
3. 清除浏览器缓存

### 动画卡顿
1. 检查设备性能
2. 关闭不必要的浏览器扩展
3. 系统会自动启用性能模式

### 主题切换异常
1. 检查本地存储权限
2. 确保没有其他主题系统冲突
3. 刷新页面重新初始化

## 自定义配置

### 禁用动画
```scss
.canvas-performance-mode {
  .dag-node {
    transition: none !important;
    animation: none !important;
  }
}
```

### 自定义颜色
```scss
:root {
  --canvas-node-source-primary: #your-color;
  --canvas-node-sink-primary: #your-color;
  --canvas-node-transform-primary: #your-color;
}
```

### 调整动画速度
```scss
:root {
  --canvas-animation-fast: 100ms;
  --canvas-animation-normal: 200ms;
  --canvas-animation-slow: 300ms;
}
```

## 反馈和支持

如果遇到任何问题或有改进建议，请：
1. 检查本升级指南
2. 查看样式指南文档
3. 提交问题报告

## 更新日志

### v1.0.0 (2024-01-16)
- 🎨 全新现代化视觉设计
- 🌓 明暗主题支持
- 📱 响应式设计
- ⚡ 性能优化
- 🎭 丰富动画效果
- 🔧 完整的设计系统

---

**注意**: 本次升级完全向后兼容，现有功能不受影响。新功能为可选使用，可以根据需要逐步采用。