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

// Query task logs
export function queryLog(params: LogParams): Promise<LogRes> {
  return axios({
    url: '/log/detail',
    method: 'get',
    params
  })
}

// Get log node list
export function getLogNodes(jobId: string | number): Promise<any> {
  // Here we use raw axios to make direct requests, avoiding the addition of /seatunnel/api/v1 prefix
  return rawAxios.get(`/api/logs/${jobId}`, {
    params: { format: 'json' }
  })
}

// Get log content
export function getLogContent(logUrl: string): Promise<{ data: string }> {
  console.log('Getting log content for URL:', logUrl);
  
  // Handle external URLs
  if (logUrl.startsWith('http')) {
    try {
      // Extract path part from URL
      const url = new URL(logUrl);
      const pathName = url.pathname;
      const search = url.search;
      
      // Request through proxy
      return rawAxios.get(`/api${pathName}${search}`);
    } catch (e) {
      console.error('Error fetching log content:', e);
      return Promise.reject(new Error('Failed to fetch log content'));
    }
  } else {
    // If not a complete URL, use the file name directly
    const logFileName = logUrl.split('/').pop() || '';
    
    // Directly request through raw axios, avoiding the addition of /seatunnel/api/v1 prefix
    return rawAxios.get(`/api/logs/content/${logFileName}`);
  }
}