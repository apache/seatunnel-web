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

import org.apache.seatunnel.shade.com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractFormSelectOption extends AbstractFormOption {

    @JsonProperty("type")
    @Getter
    private final FormType formType = FormType.SELECT;

    public AbstractFormSelectOption(@NonNull String label, @NonNull String field) {
        super(label, field);
    }

    public static class SelectOption {
        @JsonProperty @Getter private String label;

        @JsonProperty @Getter private Object value;

        public SelectOption(@NonNull String label, @NonNull Object value) {
            this.label = label;
            this.value = value;
        }
    }
}
