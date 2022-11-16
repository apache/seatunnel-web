package org.apache.seatunnel.dynamicforms;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class StaticSelectOption extends AbstractFormSelectOption {

    @Getter
    @Setter
    private List<SelectOption> options = new ArrayList<>();

    public StaticSelectOption(@NonNull List<SelectOption> options, @NonNull String label, @NonNull String field) {
        super(label, field);
        this.options = options;
    }
}
