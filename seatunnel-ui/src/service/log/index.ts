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

import { axios } from '@/service/service'
import rawAxios from 'axios'
import type { LogParams, LogRes, LogNode } from './types'

// 查询任务日志
export function queryLog(params: LogParams): Promise<LogRes> {
  return axios({
    url: '/log/detail',
    method: 'get',
    params
  })
}

// 获取日志节点列表
export function getLogNodes(jobId: string | number): Promise<any> {
  // 这里我们使用原生axios直接请求，避免添加/seatunnel/api/v1前缀
  return rawAxios.get(`/api/logs/${jobId}`, {
    params: { format: 'json' }
  })
}

// 获取日志内容
export function getLogContent(logUrl: string): Promise<{ data: string }> {
  console.log('Getting log content for URL:', logUrl);
  
  // 处理外部URL
  if (logUrl.startsWith('http')) {
    try {
      // 从URL中提取路径部分
      const url = new URL(logUrl);
      const pathName = url.pathname;
      const search = url.search;
      
      // 通过代理请求
      return rawAxios.get(`/api${pathName}${search}`);
    } catch (e) {
      console.error('Error fetching log content:', e);
      return Promise.reject(new Error('Failed to fetch log content'));
    }
  } else {
    // 如果不是完整URL，则直接使用文件名
    const logFileName = logUrl.split('/').pop() || '';
    
    // 通过原生axios直接请求，避免添加/seatunnel/api/v1前缀
    return rawAxios.get(`/api/logs/content/${logFileName}`);
  }
}