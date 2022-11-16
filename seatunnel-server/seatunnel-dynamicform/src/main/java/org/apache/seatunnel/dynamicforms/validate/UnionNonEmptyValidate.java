package org.apache.seatunnel.dynamicforms.validate;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class UnionNonEmptyValidate extends AbstractValidate {
    private final boolean required = false;
    private List<String> fields;
    private final RequiredType requiredType = RequiredType.UNION_NON_EMPTY;

    public UnionNonEmptyValidate(@NonNull List<String> fields) {
        Preconditions.checkArgument(fields.size() > 0);
        this.fields = fields;
    }
}
