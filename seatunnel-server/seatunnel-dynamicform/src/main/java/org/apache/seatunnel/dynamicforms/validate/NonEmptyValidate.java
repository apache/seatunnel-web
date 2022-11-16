package org.apache.seatunnel.dynamicforms.validate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NonEmptyValidate extends AbstractValidate {
    private final boolean required = true;
    @JsonProperty("type")
    private final RequiredType requiredType = RequiredType.NON_EMPTY;
}
