package com.prateek.learning.day03.java.spring.exceptionhandling;

public record ApiError(
        int status,
        String error,
        String message
) {
}
