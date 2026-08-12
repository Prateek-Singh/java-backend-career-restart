package com.prateek.learning.common.exception;

import com.prateek.learning.transaction.exception.InvalidTransactionAmountException;
import com.prateek.learning.transaction.exception.TransactionNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldHandleTransactionNotFoundException() {
        ResponseEntity<ApiError> response =
                handler.handleTransactionNotFoundException(
                        new TransactionNotFoundException("Transaction not found")
                );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Not Found", response.getBody().error());
        assertEquals(
                "Transaction not found",
                response.getBody().message()
        );
    }

    @Test
    void shouldHandleInvalidTransactionAmountException() {
        ResponseEntity<ApiError> response =
                handler.handleInvalidTransactionAmountException(
                        new InvalidTransactionAmountException(
                                "Transaction amount must be greater than zero"
                        )
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals(
                "Transaction amount must be greater than zero",
                response.getBody().message()
        );
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        ResponseEntity<ApiError> response =
                handler.handleIllegalArgumentException(
                        new IllegalArgumentException("Invalid input")
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Invalid input", response.getBody().message());
    }

    @Test
    void shouldHandleUnexpectedExceptionWithoutExposingDetails() {
        ResponseEntity<ApiError> response =
                handler.handleException(
                        new RuntimeException("Unexpected database failure")
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals(
                "Internal Server Error",
                response.getBody().error()
        );
        assertEquals(
                "An unexpected error occurred",
                response.getBody().message()
        );
    }

    @Test
    void shouldHandleDuplicateTransaction() {
        ResponseEntity<ApiError> response =
                handler.handleDuplicateTransaction(
                        new DuplicateTransactionException("Transaction with id TXN-100 already exists")
                );

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals(
                "Conflict",
                response.getBody().error()
        );
        assertEquals(
                "Transaction with id TXN-100 already exists",
                response.getBody().message()
        );
    }
}