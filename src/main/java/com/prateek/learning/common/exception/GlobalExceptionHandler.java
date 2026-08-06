package com.prateek.learning.common.exception;

import com.prateek.learning.transaction.exception.InvalidTransactionAmountException;
import com.prateek.learning.transaction.exception.TransactionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ApiError> handleTransactionNotFoundException(
            TransactionNotFoundException exception
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ApiError apiError = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                exception.getMessage()
        );

        return ResponseEntity.status(status).body(apiError);
    }

    @ExceptionHandler(InvalidTransactionAmountException.class)
    public ResponseEntity<ApiError> handleInvalidTransactionAmountException(
            InvalidTransactionAmountException exception
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ApiError apiError = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                exception.getMessage()
        );

        return ResponseEntity.status(status).body(apiError);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ApiError apiError = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                exception.getMessage()
        );

        return ResponseEntity.status(status).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception exception) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiError apiError = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                "An unexpected error occurred"
        );

        return ResponseEntity.status(status).body(apiError);
    }
}