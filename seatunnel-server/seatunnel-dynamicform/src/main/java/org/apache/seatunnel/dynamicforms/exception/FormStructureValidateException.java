package org.apache.seatunnel.dynamicforms.exception;

import lombok.NonNull;

import java.util.List;

public class FormStructureValidateException extends RuntimeException {

    public FormStructureValidateException(@NonNull String formName, @NonNull List<String> errorList, @NonNull Throwable e) {
        super(String.format("Form: %s, validate error - %s", formName, errorList), e);
    }

    public FormStructureValidateException(@NonNull String formName, @NonNull List<String> errorList) {
        super(String.format("Form: %s, validate error - %s", formName, errorList));
    }
}
