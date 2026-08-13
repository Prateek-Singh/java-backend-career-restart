package com.prateek.learning.transaction.service;

import com.prateek.learning.kafka.producer.TransactionEventPublisher;
import com.prateek.learning.transaction.dto.CreateTransactionRequest;
import com.prateek.learning.transaction.exception.InvalidTransactionAmountException;
import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.model.TransactionType;
import com.prateek.learning.transaction.repository.InMemoryTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceCreateTransactionTest {

    private TransactionService transactionService;

    private InMemoryTransactionRepository repository = new InMemoryTransactionRepository();

    @Mock
    private TransactionEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(repository, eventPublisher);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(null)
        );

        assertEquals("Transaction request is required", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTransactionIdIsBlank() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                " ",
                "ACC-111",
                new BigDecimal("25.00"),
                TransactionType.CREDIT,
                "Monthly Savings"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(request)
        );

        assertEquals("Transaction id is required", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenAccountIdIsBlank() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-125",
                " ",
                new BigDecimal("25.00"),
                TransactionType.CREDIT,
                "Monthly Savings"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(request)
        );

        assertEquals("Account id is required", exception.getMessage());
    }

    @Test
    void shouldThrowInvalidTransactionAmountExceptionWhenAmountIsNull() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-125",
                "ACC-111",
                null,
                TransactionType.CREDIT,
                "Monthly Savings"
        );

        InvalidTransactionAmountException exception = assertThrows(
                InvalidTransactionAmountException.class,
                () -> transactionService.createTransaction(request)
        );

        assertEquals("Transaction amount is required", exception.getMessage());
    }

    @Test
    void shouldThrowInvalidTransactionAmountExceptionWhenAmountIsZero() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-125",
                "ACC-111",
                BigDecimal.ZERO,
                TransactionType.CREDIT,
                "Monthly Savings"
        );

        InvalidTransactionAmountException exception = assertThrows(
                InvalidTransactionAmountException.class,
                () -> transactionService.createTransaction(request)
        );

        assertEquals("Transaction amount must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldThrowInvalidTransactionAmountExceptionWhenAmountIsNegative() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-125",
                "ACC-111",
                new BigDecimal("-25.00"),
                TransactionType.CREDIT,
                "Monthly Savings"
        );

        InvalidTransactionAmountException exception = assertThrows(
                InvalidTransactionAmountException.class,
                () -> transactionService.createTransaction(request)
        );

        assertEquals("Transaction amount must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTransactionTypeIsNull() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-125",
                "ACC-111",
                BigDecimal.TEN,
                null,
                "Monthly Savings"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(request)
        );

        assertEquals("Transaction type is required", exception.getMessage());
    }

    @Test
    void shouldReturnTransactionWhenValidRequest() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-125",
                "ACC-111",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly Savings"
        );

        Transaction transaction = transactionService.createTransaction(request);
        assertNotNull(transaction);
        assertEquals("TXN-125", transaction.getId());
        assertEquals("ACC-111", transaction.getAccountId());
        assertEquals(BigDecimal.TEN, transaction.getAmount());
        assertEquals(TransactionType.CREDIT, transaction.getType());
        assertEquals("Monthly Savings", transaction.getDescription());
        assertNotNull(transaction.getTimestamp());
        assertNotSame(request, transaction);
    }
}