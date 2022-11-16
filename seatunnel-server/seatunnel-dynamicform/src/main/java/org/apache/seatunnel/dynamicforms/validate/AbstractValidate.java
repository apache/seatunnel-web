package org.apache.seatunnel.dynamicforms.validate;

import org.apache.seatunnel.dynamicforms.Locale;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;

@Data
public class AbstractValidate<T extends AbstractValidate> {
    private final List<String> trigger = Arrays.asList("input", "blur");

    // support i18n
    private String message = "required";

    public enum RequiredType {
        @JsonProperty("non-empty")
        NON_EMPTY("non-empty"),

        @JsonProperty("union-non-empty")
        UNION_NON_EMPTY("union-non-empty");

        @Getter
        private String type;

        RequiredType(String type) {
            this.type = type;
        }
    }

    public T withMessage(@NonNull String message) {
        this.message = message;
        return (T) this;
    }

    public T withI18nMessage(@NonNull String message) {
        this.message = Locale.I18N_PREFIX + message;
        return (T) this;
    }
}
