package com.prateek.learning.java.day01;

import com.prateek.learning.transaction.exception.InvalidTransactionAmountException;
import com.prateek.learning.transaction.dto.CreateTransactionRequest;
import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.repository.InMemoryTransactionRepository;
import com.prateek.learning.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceCreateTransactionTest {

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(new InMemoryTransactionRepository());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransaction(null);
        });

        assertEquals("Transaction request is required", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTransactionIdIsBlank() {
        CreateTransactionRequest createTransactionRequest = new CreateTransactionRequest(
                " ",
                "ACC-111",
                new BigDecimal("25.00"),
                "CREDIT",
                "Monthly Savings"
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransaction(createTransactionRequest);
        });

        assertEquals("Transaction id is required", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenAccountIdIsBlank() {
        CreateTransactionRequest createTransactionRequest = new CreateTransactionRequest(
                "TXN-125",
                " ",
                new BigDecimal("25.00"),
                "CREDIT",
                "Monthly Savings"
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransaction(createTransactionRequest);
        });

        assertEquals("Account id is required", exception.getMessage());
    }

    @Test
    void shouldThrowInvalidTransactionAmountExceptionWhenAmountIsNull() {
        CreateTransactionRequest createTransactionRequest = new CreateTransactionRequest(
                "TXN-125",
                "ACC-111",
                null,
                "CREDIT",
                "Monthly Savings"
        );

        InvalidTransactionAmountException exception = assertThrows(InvalidTransactionAmountException.class, () -> {
            transactionService.createTransaction(createTransactionRequest);
        });

        assertEquals("Transaction amount is required", exception.getMessage());
    }

    @Test
    void shouldThrowInvalidTransactionAmountExceptionWhenAmountIsZero() {
        CreateTransactionRequest createTransactionRequest = new CreateTransactionRequest(
                "TXN-125",
                "ACC-111",
                BigDecimal.ZERO,
                "CREDIT",
                "Monthly Savings"
        );

        InvalidTransactionAmountException exception = assertThrows(InvalidTransactionAmountException.class, () -> {
            transactionService.createTransaction(createTransactionRequest);
        });

        assertEquals("Transaction amount must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldThrowInvalidTransactionAmountExceptionWhenAmountIsNegative() {
        CreateTransactionRequest createTransactionRequest = new CreateTransactionRequest(
                "TXN-125",
                "ACC-111",
                BigDecimal.valueOf(-25.00),
                "CREDIT",
                "Monthly Savings"
        );

        InvalidTransactionAmountException exception = assertThrows(InvalidTransactionAmountException.class, () -> {
            transactionService.createTransaction(createTransactionRequest);
        });

        assertEquals("Transaction amount must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTypeIsBlank() {
        CreateTransactionRequest createTransactionRequest = new CreateTransactionRequest(
                "TXN-125",
                "ACC-111",
                BigDecimal.TEN,
                " ",
                "Monthly Savings"
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransaction(createTransactionRequest);
        });

        assertEquals("Transaction type is required", exception.getMessage());
    }

    @Test
    void shouldReturnTransactionWhenValidRequest() {
        CreateTransactionRequest createTransactionRequest = new CreateTransactionRequest(
                "TXN-125",
                "ACC-111",
                BigDecimal.TEN,
                "CREDIT",
                "Monthly Savings"
        );

        Transaction transaction = transactionService.createTransaction(createTransactionRequest);
        assertNotNull(transaction);
        assertEquals("TXN-125", transaction.getId());
        assertEquals("ACC-111", transaction.getAccountId());
        assertEquals(BigDecimal.TEN, transaction.getAmount());
        assertEquals("CREDIT", transaction.getType());
        assertEquals("Monthly Savings", transaction.getDescription());
        assertNotNull(transaction.getTimestamp());
        assertNotSame(createTransactionRequest, transaction);
    }
}