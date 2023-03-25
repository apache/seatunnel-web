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

import type { GlobalThemeOverrides } from 'naive-ui'

const light: GlobalThemeOverrides = {
  common: {
    primaryColor: '#614bdd',
    primaryColorHover: '#7d68de',
    primaryColorSuppl: '#7d68de',
    primaryColorPressed: '#513ac2',

    infoColor: '#614bdd',
    infoColorHover: '#7d68de',
    infoColorSuppl: '#7d68de',
    infoColorPressed: '#513ac2',

    errorColor: '#db2777',
    errorColorHover: '#d64687',
    errorColorSuppl: '#d64687',
    errorColorPressed: '#c60165',

    successColor: '#04beca',
    successColorHover: '#69c8d5',
    successColorSuppl: '#69c8d5',
    successColorPressed: '#04a6ae',

    warningColor: '#eab308',
    warningColorHover: '#e5cb41',
    warningColorSuppl: '#e5cb41',
    warningColorPressed: '#b38706',

    textColorBase: '#151666',
    textColor1: '#242660',
    textColor2: '#313377',
    textColor3: '#9096b8',

    bodyColor: '#f7f8fa',
    borderRadius: '15px',
    tableHeaderColor: '#614bdd'
  },
  Layout: {
    headerColor: '#fff'
  },
  DataTable: {
    thTextColor: '#fff',
    tdColorHover: '#f2f2fa'
  },
  Space: {
    gapLarge: '28px 32px'
  }
}

export default light
