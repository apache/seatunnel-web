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

package org.apache.seatunnel.dynamicforms;

import org.apache.seatunnel.common.utils.JsonUtils;
import org.apache.seatunnel.dynamicforms.exception.FormStructureValidateException;
import org.apache.seatunnel.dynamicforms.validate.ValidateBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FormStructureBuilderTest {

    @Test
    public void testFormStructureBuild() {
        Locale locale = new Locale();
        locale.addZhCN("name_password_union_required", "all name and password are required")
            .addZhCN("username", "username")
            .addEnUS("name_password_union_required", "all name and password are required")
            .addEnUS("username", "username");

        FormInputOption nameOption = (FormInputOption) FormOptionBuilder.builder()
            .withI18nLabel("username")
            .withField("username")
            .inputOptionBuilder()
            .formTextInputOption()
            .withDescription("username")
            .withClearable()
            .withPlaceholder("username")
            .withShow("checkType", "nameAndPassword")
            .withValidate(ValidateBuilder.builder()
                .unionNonEmptyValidateBuilder()
                .fields("username", "password")
                .unionNonEmptyValidate()
                .withI18nMessage("name_password_union_required"));

        FormInputOption passwordOption = (FormInputOption) FormOptionBuilder.builder()
            .withLabel("password")
            .withField("password")
            .inputOptionBuilder()
            .formPasswordInputOption()
            .withDescription("password")
            .withPlaceholder("password")
            .withShow("checkType", "nameAndPassword")
            .withValidate(ValidateBuilder.builder()
                .unionNonEmptyValidateBuilder()
                .fields("username", "password")
                .unionNonEmptyValidate()
                .withI18nMessage("name_password_union_required"));

        FormInputOption textAreaOption = (FormInputOption) FormOptionBuilder.builder()
            .withLabel("content")
            .withField("context")
            .inputOptionBuilder()
            .formTextareaInputOption()
            .withClearable()
            .withDescription("content");

        StaticSelectOption checkTypeOption = (StaticSelectOption) FormOptionBuilder.builder()
            .withLabel("checkType")
            .withField("checkType")
            .staticSelectOptionBuilder()
            .addSelectOptions(new AbstractFormSelectOption.SelectOption("no", "no"),
                new AbstractFormSelectOption.SelectOption("nameAndPassword", "nameAndPassword"))
            .formStaticSelectOption()
            .withClearable()
            .withDefaultValue("no")
            .withDescription("check type")
            .withValidate(ValidateBuilder.builder().nonEmptyValidateBuilder().nonEmptyValidate());

        DynamicSelectOption cityOption = (DynamicSelectOption) FormOptionBuilder.builder()
            .withField("city")
            .withLabel("city")
            .dynamicSelectOptionBuilder()
            .withSelectApi("getCity")
            .formDynamicSelectOption()
            .withDescription("city")
            .withValidate(ValidateBuilder.builder().nonEmptyValidateBuilder().nonEmptyValidate());

        FormStructure testForm = FormStructure.builder()
            .name("testForm")
            .addFormOption(nameOption, passwordOption, textAreaOption, checkTypeOption, cityOption)
            .withLocale(locale)
            .addApi("getCity", "/api/get_city", FormStructure.HttpMethod.GET)
            .build();

        String s = JsonUtils.toJsonString(testForm);
        String result =
            "{\"name\":\"testForm\",\"locales\":{\"zh_CN\":{\"name_password_union_required\":\"all name and password are required\",\"username\":\"username\"}" +
                ",\"en_US\":{\"name_password_union_required\":\"all name and password are required\",\"username\":\"username\"}},\"apis\":{\"getCity\":{\"method\":\"get\",\"url\":\"/api/get_city\"}}" +
                ",\"forms\":[{\"label\":\"i18n.username\",\"field\":\"username\",\"defaultValue\":null,\"description\":\"username\",\"clearable\":true,\"show\":{\"field\":\"checkType\",\"value\":\"nameAndPassword\"}" +
                ",\"placeholder\":\"username\",\"validate\":{\"trigger\":[\"input\",\"blur\"],\"message\":\"i18n.name_password_union_required\",\"required\":false,\"fields\":[\"username\",\"password\"],\"requiredType\":\"union-non-empty\"}" +
                ",\"inputType\":\"text\",\"type\":\"input\"},{\"label\":\"password\",\"field\":\"password\",\"defaultValue\":null,\"description\":\"password\",\"clearable\":false,\"show\":{\"field\":\"checkType\",\"value\":\"nameAndPassword\"}" +
                ",\"placeholder\":\"password\",\"validate\":{\"trigger\":[\"input\",\"blur\"],\"message\":\"i18n.name_password_union_required\",\"required\":false,\"fields\":[\"username\",\"password\"],\"requiredType\":\"union-non-empty\"}" +
                ",\"inputType\":\"password\",\"type\":\"input\"},{\"label\":\"content\",\"field\":\"context\",\"defaultValue\":null,\"description\":\"content\",\"clearable\":true,\"placeholder\":\"\",\"inputType\":\"textarea\",\"type\":\"input\"}" +
                ",{\"label\":\"checkType\",\"field\":\"checkType\",\"defaultValue\":\"no\",\"description\":\"check type\",\"clearable\":true,\"placeholder\":\"\",\"validate\":{\"trigger\":[\"input\",\"blur\"],\"message\":\"required\",\"required\":true,\"type\":\"non-empty\"}" +
                ",\"options\":[{\"label\":\"no\",\"value\":\"no\"},{\"label\":\"nameAndPassword\",\"value\":\"nameAndPassword\"}],\"type\":\"select\"},{\"label\":\"city\",\"field\":\"city\",\"defaultValue\":null,\"description\":\"city\",\"clearable\":false,\"placeholder\":\"\"," +
                "\"validate\":{\"trigger\":[\"input\",\"blur\"],\"message\":\"required\",\"required\":true,\"type\":\"non-empty\"},\"api\":\"getCity\",\"type\":\"select\"}]}";
        Assertions.assertEquals(result, s);
    }

    @Test
    public void testFormStructureValidate() {
        Locale locale = new Locale();
        locale.addZhCN("name_password_union_required", "all name and password are required")
            .addEnUS("name_password_union_required", "all name and password are required")
            .addEnUS("username", "username");

        FormInputOption nameOption = (FormInputOption) FormOptionBuilder.builder()
            .withI18nLabel("username")
            .withField("username")
            .inputOptionBuilder()
            .formTextInputOption()
            .withDescription("username")
            .withClearable()
            .withPlaceholder("username")
            .withShow("checkType1", "nameAndPassword")
            .withValidate(ValidateBuilder.builder()
                .unionNonEmptyValidateBuilder()
                .fields("user", "password")
                .unionNonEmptyValidate()
                .withI18nMessage("name_password_union_required"));

        FormInputOption passwordOption = (FormInputOption) FormOptionBuilder.builder()
            .withLabel("password")
            .withField("password")
            .inputOptionBuilder()
            .formPasswordInputOption()
            .withDescription("password")
            .withPlaceholder("password")
            .withShow("checkType", "nameAndPassword")
            .withValidate(ValidateBuilder.builder()
                .unionNonEmptyValidateBuilder()
                .fields("username", "password")
                .unionNonEmptyValidate()
                .withI18nMessage("name_password_union_required"));

        FormInputOption textAreaOption = (FormInputOption) FormOptionBuilder.builder()
            .withLabel("content")
            .withField("context")
            .inputOptionBuilder()
            .formTextareaInputOption()
            .withClearable()
            .withDescription("content");

        StaticSelectOption checkTypeOption = (StaticSelectOption) FormOptionBuilder.builder()
            .withLabel("checkType")
            .withField("checkType")
            .staticSelectOptionBuilder()
            .addSelectOptions(new AbstractFormSelectOption.SelectOption("no", "no"),
                new AbstractFormSelectOption.SelectOption("nameAndPassword", "nameAndPassword"))
            .formStaticSelectOption()
            .withClearable()
            .withDefaultValue("no")
            .withDescription("check type")
            .withValidate(ValidateBuilder.builder().nonEmptyValidateBuilder().nonEmptyValidate());

        DynamicSelectOption cityOption = (DynamicSelectOption) FormOptionBuilder.builder()
            .withField("city")
            .withLabel("city")
            .dynamicSelectOptionBuilder()
            .withSelectApi("getCity")
            .formDynamicSelectOption()
            .withDescription("city")
            .withValidate(ValidateBuilder.builder().nonEmptyValidateBuilder().nonEmptyValidate());

        String error = "";
        try {
            FormStructure testForm = FormStructure.builder()
                .name("testForm")
                .addFormOption(nameOption, passwordOption, textAreaOption, checkTypeOption, cityOption)
                .withLocale(locale)
                .addApi("getCity1", "/api/get_city", FormStructure.HttpMethod.GET)
                .build();
        } catch (FormStructureValidateException e) {
            error = e.getMessage();
        }

        String result =
            "Form: testForm, validate error - [DynamicSelectOption[city] used api[getCity] can not found in FormStructure.apis, FormOption[i18n.username] used i18n label[username] can not found in FormStructure.locales zh_CN, FormOption[i18n.username] used show field[checkType1] can not found in select options, UnionNonEmptyValidate Option field[username] must in validate union field list, UnionNonEmptyValidate Option field[username] , validate union field[user] can not found in form options]";
        Assertions.assertEquals(result, error);
    }
}
