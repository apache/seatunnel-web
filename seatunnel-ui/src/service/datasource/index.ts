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

export function datasourceList(params: any): any {
  return axios({
    url: '/datasource/list',
    method: 'get',
    params
  })
}

export function datasourceDelete(id: string): any {
  return axios({
    url: '/datasource/' + id,
    method: 'delete'
  })
}

export function datasourceTypeList(params: {
  showVirtualDataSource: boolean
  source?: 'WS' | 'WT'
}): any {
  return axios({
    url: '/datasource/support-datasources',
    method: 'get',
    params
  })
}

export function datasourceDetail(id: string): any {
  return axios({
    url: '/datasource/' + id,
    method: 'get'
  })
}

export function datasourceAdd(data: any): any {
  return axios({
    url: '/datasource/create',
    method: 'post',
    data
  })
}

export function datasourceUpdate(data: any, id: string): any {
  return axios({
    url: '/datasource/' + id,
    method: 'put',
    data
  })
}

export function checkConnect(data: any): any {
  return axios({
    url: '/datasource/check/connect',
    method: 'post',
    data
  })
}

export function dynamicFormItems(pluginName: string): any {
  return axios({
    url: '/datasource/dynamic-form',
    method: 'get',
    params: { pluginName }
  })
}
