package com.prateek.learning.java.day03.immutability;

import com.prateek.learning.java.day03.immutability.TransactionSummary;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionSummaryTest {

    @Test
    void shouldThrowExceptionWhenAccountIdIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionSummary(null, new BigDecimal("10.00"), List.of("TXN-001"));
        });
        assertEquals("accountId cannot be null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAccountIdIsBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionSummary(" ", new BigDecimal("10.00"), List.of("TXN-001"));
        });
        assertEquals("accountId cannot be null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTotalAmountIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionSummary("ACC-001", null, List.of("TXN-001"));
        });
        assertEquals("totalAmount cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTransactionsIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionSummary("ACC-001", new BigDecimal("10.00"), null);
        });
        assertEquals("transactionIds cannot be null", exception.getMessage());
    }

    @Test
    void shouldNotChangeStoredTransactionsWhenOriginalListChanges() {
        List<String> transactions =
                new ArrayList<>(List.of("TXN-001"));

        TransactionSummary summary =
                new TransactionSummary(
                        "ACC-001",
                        BigDecimal.TEN,
                        transactions
                );

        transactions.add("TXN-002");

        assertEquals(
                List.of("TXN-001"),
                summary.getTransactionIds()
        );
    }

    @Test
    void shouldThrowUnsupportedOperationExceptionWhenStoredListMutationIsAttempted() {
        List<String> transactions = new ArrayList<>(List.of("TXN-001"));
        TransactionSummary transactionSummary = new TransactionSummary("ACC-001", BigDecimal.TEN, transactions);
        assertThrows(UnsupportedOperationException.class, () -> {
            transactionSummary.getTransactionIds().add("TXN-002");
        });
    }

    @Test
    void shouldBeEqualWhenAllValuesAreEqual() {
        TransactionSummary first =
                new TransactionSummary(
                        "ACC-001",
                        BigDecimal.TEN,
                        List.of("TXN-001")
                );

        TransactionSummary second =
                new TransactionSummary(
                        "ACC-001",
                        BigDecimal.TEN,
                        List.of("TXN-001")
                );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenTotalAmountDiffers() {
        TransactionSummary first =
                new TransactionSummary(
                        "ACC-001",
                        new BigDecimal("10.00"),
                        List.of("TXN-001")
                );

        TransactionSummary second =
                new TransactionSummary(
                        "ACC-001",
                        new BigDecimal("20.00"),
                        List.of("TXN-001")
                );

        assertNotEquals(first, second);
    }
}