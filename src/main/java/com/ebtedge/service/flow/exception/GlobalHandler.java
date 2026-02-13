package com.ebtedge.service.flow.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler(WorkflowException.class)
    public ResponseEntity<Object> handle(WorkflowException ex) {
        return ResponseEntity.status(400).body(ex.getError());
    }
}

