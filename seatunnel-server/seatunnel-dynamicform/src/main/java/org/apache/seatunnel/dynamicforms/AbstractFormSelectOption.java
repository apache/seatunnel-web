package org.apache.seatunnel.dynamicforms;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        @JsonProperty
        @Getter
        private String label;

        @JsonProperty
        @Getter
        private Object value;

        public SelectOption(@NonNull String label, @NonNull Object value) {
            this.label = label;
            this.value = value;
        }
    }
}
