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

import { Graph } from '@antv/x6'
import { useCanvasTheme } from './theme-manager'


export interface CanvasOptions {
  enableGrid?: boolean
  enableMinimap?: boolean
  enableScroller?: boolean
  enableSelection?: boolean
  enableKeyboard?: boolean
  enableClipboard?: boolean
  enableHistory?: boolean
  gridSize?: number
  minimapSize?: { width: number; height: number }
  background?: {
    color?: string
    image?: string
    size?: string
    position?: string
  }
}

export function useDagGraph(
  graph: any,
  dagContainer: HTMLElement,
  minimapContainer: HTMLElement,
  options: CanvasOptions = {}
) {

  const graphInstance = new Graph({
    container: dagContainer,
    autoResize: true,
    

    grid: {
      size: 20,
      visible: true,
      type: 'dot'
    },
    

    background: {
      color: '#FAFAFA'
    },
    

    minimap: minimapContainer ? {
      enabled: true,
      container: minimapContainer,
      width: 200,
      height: 120,
      padding: 10
    } : false,
    

    selecting: {
      enabled: true,
      multiple: true,
      rubberband: true,
      movable: true
    },
    

    connecting: {
      allowBlank: false,
      allowLoop: false,
      allowNode: false,
      allowEdge: false,
      allowPort: true
    }
  })
  
  console.log('Graph instance created:', graphInstance)
  console.log('Container element:', dagContainer)
  

  const { canvasColors } = useCanvasTheme()
  const updateTheme = () => {
    const colors = canvasColors.value
    

    graphInstance.drawBackground({
      color: colors.background
    })
  }
  

  window.addEventListener('canvas-theme-change', updateTheme)
  

  graphInstance.on('scale', ({ sx }: any) => {
    const container = dagContainer
    const gridElement = container.querySelector('.x6-graph-grid')
    
    if (gridElement) {

      let opacity = 1
      if (sx < 0.5) {
        opacity = 0.3
        gridElement.classList.add('zoom-small')
      } else if (sx > 2) {
        opacity = 0.6
        gridElement.classList.add('zoom-large')
      } else if (sx > 4) {
        opacity = 0.4
        gridElement.classList.add('zoom-extra-large')
      } else {
        gridElement.classList.remove('zoom-small', 'zoom-large', 'zoom-extra-large')
      }
      
      ;(gridElement as HTMLElement).style.opacity = opacity.toString()
    }
  })
  

  let frameCount = 0
  let lastTime = performance.now()
  
  const monitorPerformance = () => {
    frameCount++
    const currentTime = performance.now()
    
    if (currentTime - lastTime >= 1000) {
      const fps = Math.round((frameCount * 1000) / (currentTime - lastTime))
      

      if (fps < 30) {
        dagContainer.classList.add('canvas-performance-mode')
      } else {
        dagContainer.classList.remove('canvas-performance-mode')
      }
      
      frameCount = 0
      lastTime = currentTime
    }
    
    requestAnimationFrame(monitorPerformance)
  }
  

  requestAnimationFrame(monitorPerformance)
  
  return graphInstance
}
