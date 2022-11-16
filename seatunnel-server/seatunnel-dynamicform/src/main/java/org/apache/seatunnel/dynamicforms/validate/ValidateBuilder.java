package org.apache.seatunnel.dynamicforms.validate;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ValidateBuilder {

    public static ValidateBuilder builder() {
        return new ValidateBuilder();
    }

    public NonEmptyValidateBuilder nonEmptyValidateBuilder() {
        return new NonEmptyValidateBuilder();
    }

    public UnionNonEmptyValidateBuilder unionNonEmptyValidateBuilder() {
        return new UnionNonEmptyValidateBuilder();
    }

    public static class NonEmptyValidateBuilder {
        public NonEmptyValidate nonEmptyValidate() {
            return new NonEmptyValidate();
        }
    }

    public static class UnionNonEmptyValidateBuilder {
        private List<String> fields = new ArrayList<>();

        public UnionNonEmptyValidateBuilder fields(@NonNull String... fields) {
            for (String field : fields) {
                this.fields.add(field);
            }
            return this;
        }

        public UnionNonEmptyValidate unionNonEmptyValidate() {
            return new UnionNonEmptyValidate(fields);
        }
    }
}
