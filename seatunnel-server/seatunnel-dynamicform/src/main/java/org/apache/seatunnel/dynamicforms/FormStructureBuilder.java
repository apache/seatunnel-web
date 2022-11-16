package org.apache.seatunnel.dynamicforms;

import org.apache.seatunnel.dynamicforms.exception.FormStructureValidateException;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormStructureBuilder {
    private String name;

    private List<AbstractFormOption> forms = new ArrayList<>();

    private Locale locales;

    private Map<String, Map<String, String>> apis;

    public FormStructureBuilder name(@NonNull String name) {
        this.name = name;
        return this;
    }

    public FormStructureBuilder addFormOption(@NonNull AbstractFormOption... formOptions) {
        for (AbstractFormOption formOption : formOptions) {
            forms.add(formOption);
        }
        return this;
    }

    public FormStructureBuilder withLocale(@NonNull Locale locale) {
        this.locales = locale;
        return this;
    }

    public FormStructureBuilder addApi(@NonNull String apiName,
                                       @NonNull String url,
                                       @NonNull FormStructure.HttpMethod method) {
        if (apis == null) {
            apis = new HashMap<>();
        }
        apis.putIfAbsent(apiName, new HashMap<>());
        apis.get(apiName).put("url", url);
        apis.get(apiName).put("method", method.name().toLowerCase(java.util.Locale.ROOT));

        return this;
    }

    public FormStructure build() throws FormStructureValidateException {
        FormStructure formStructure = new FormStructure(name, forms, locales, apis);
        FormStructureValidate.validateFormStructure(formStructure);
        return formStructure;
    }
}
