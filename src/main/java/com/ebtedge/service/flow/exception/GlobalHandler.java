package com.ebtedge.service.flow.exception;

import com.ebtedge.service.flow.domain.ErrorDetails;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler(WorkflowException.class)
    public ResponseEntity<Object> handle(WorkflowException ex) {
        log.error("GlobalHandler: Handling WorkflowException - ErrorCode: {}, Message: {}",
                ex.getError().errorCode(), ex.getError().message(), ex);
        return ResponseEntity.status(400).body(ex.getError());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handle(ConstraintViolationException ex) {
        log.error("GlobalHandler: Handling ConstraintViolationException - Message: {}", ex.getMessage());
        ErrorDetails error = new ErrorDetails("VALIDATION_ERROR", ex.getMessage());
        return ResponseEntity.status(400).body(error);
    }
}

