package com.ebtedge.service.flow.exception;

import com.ebtedge.service.flow.domain.ErrorDetails;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalHandlerTest {

    private final GlobalHandler handler = new GlobalHandler();

    @Test
    void testHandleWorkflowException() {
        ErrorDetails error = new ErrorDetails("TEST_ERR", "Test error message");
        WorkflowException exception = new WorkflowException(error);

        ResponseEntity<Object> response = handler.handle(exception);

        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertEquals(error, response.getBody());
    }

    @Test
    void testHandleWorkflowException_VerifyErrorDetails() {
        ErrorDetails error = new ErrorDetails("ERR_123", "Detailed error");
        WorkflowException exception = new WorkflowException(error);

        ResponseEntity<Object> response = handler.handle(exception);

        ErrorDetails responseError = (ErrorDetails) response.getBody();
        assertEquals("ERR_123", responseError.errorCode());
        assertEquals("Detailed error", responseError.message());
    }

    @Test
    void testHandleWorkflowException_ResponseBodyType() {
        ErrorDetails error = new ErrorDetails("CODE", "Message");
        WorkflowException exception = new WorkflowException(error);

        ResponseEntity<Object> response = handler.handle(exception);

        assertTrue(response.getBody() instanceof ErrorDetails);
    }

    @Test
    void testHandleWorkflowException_HttpStatus() {
        ErrorDetails error = new ErrorDetails("ANY_CODE", "Any message");
        WorkflowException exception = new WorkflowException(error);

        ResponseEntity<Object> response = handler.handle(exception);

        assertEquals(400, response.getStatusCode().value());
    }
}
