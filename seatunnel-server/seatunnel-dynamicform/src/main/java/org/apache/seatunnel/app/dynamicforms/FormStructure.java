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

package org.apache.seatunnel.app.dynamicforms;

import org.apache.seatunnel.shade.com.fasterxml.jackson.annotation.JsonIgnoreType;
import org.apache.seatunnel.shade.com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

/** SeaTunnel Web UI will use this json data to automatically create page form elements */
@Data
public class FormStructure {
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FormLocale locales;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Map<String, String>> apis;

    private List<AbstractFormOption> forms;

    public FormStructure() {}

    public FormStructure(
            @NonNull String name,
            @NonNull List<AbstractFormOption> formOptionList,
            FormLocale locale,
            Map<String, Map<String, String>> apis) {
        this.name = name;
        this.forms = formOptionList;
        this.locales = locale;
        this.apis = apis;
    }

    @JsonIgnoreType
    public enum HttpMethod {
        GET,

        POST
    }

    public static FormStructureBuilder builder() {
        return new FormStructureBuilder();
    }
}
