package com.prateek.learning.common.exception;

public record ApiError(
        int status,
        String error,
        String message
) {
}
