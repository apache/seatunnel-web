package org.apache.seatunnel.dynamicforms;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class DynamicSelectOption extends AbstractFormSelectOption {
    @Getter
    @Setter
    private String api;

    public DynamicSelectOption(@NonNull String api, @NonNull String label, @NonNull String field) {
        super(label, field);
        this.api = api;
    }
}
