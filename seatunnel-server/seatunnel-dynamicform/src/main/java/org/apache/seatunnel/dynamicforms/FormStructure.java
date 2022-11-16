package org.apache.seatunnel.dynamicforms;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

/**
 * SeaTunnel Web UI will use this json data to automatically create page form elements
 */
@Data
public class FormStructure {
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Locale locales;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Map<String, String>> apis;

    private final List<AbstractFormOption> forms;

    public FormStructure(@NonNull String name, @NonNull List<AbstractFormOption> formOptionList, Locale locale,
                         Map<String, Map<String, String>> apis) {
        Preconditions.checkArgument(formOptionList.size() > 1);
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
