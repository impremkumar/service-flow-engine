package com.ebtedge.service.flow.exception;

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
}

