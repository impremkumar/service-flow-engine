package com.ebtedge.service.flow.exception;

import com.ebtedge.service.flow.domain.ErrorDetails;
import lombok.Getter;

public class WorkflowException extends RuntimeException {
    @Getter
    private final ErrorDetails error;

    public WorkflowException(ErrorDetails error) {
        this.error = error;
    }
}
