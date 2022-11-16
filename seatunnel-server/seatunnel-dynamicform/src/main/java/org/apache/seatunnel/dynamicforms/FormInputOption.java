package org.apache.seatunnel.dynamicforms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;

public class FormInputOption extends AbstractFormOption {
    @JsonProperty("type")
    @Getter
    private final FormType formType = FormType.INPUT;

    @Getter
    private final InputType inputType;

    public FormInputOption(@NonNull InputType inputType, @NonNull String label, @NonNull String field) {
        super(label, field);
        this.inputType = inputType;
    }

    public enum InputType {
        @JsonProperty("text")
        TEXT("text"),

        @JsonProperty("password")
        PASSWORD("password"),

        @JsonProperty("textarea")
        TEXTAREA("textarea");

        @Getter
        private String inputType;

        InputType(String inputType) {
            this.inputType = inputType;
        }
    }
}
