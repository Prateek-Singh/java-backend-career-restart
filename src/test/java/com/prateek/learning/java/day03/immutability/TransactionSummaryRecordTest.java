package com.prateek.learning.java.day03.immutability;

import com.prateek.learning.java.day03.immutability.TransactionSummaryRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionSummaryRecordTest {

    @Test
    void shouldThrowExceptionWhenAccountIdIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionSummaryRecord(null, new BigDecimal("10.00"), List.of("TXN-001"));
        });
        assertEquals("accountId cannot be null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAccountIdIsBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionSummaryRecord(" ", new BigDecimal("10.00"), List.of("TXN-001"));
        });
        assertEquals("accountId cannot be null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTotalAmountIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionSummaryRecord("ACC-001", null, List.of("TXN-001"));
        });
        assertEquals("totalAmount cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTransactionsIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionSummaryRecord("ACC-001", new BigDecimal("10.00"), null);
        });
        assertEquals("transactionIds cannot be null", exception.getMessage());
    }

    @Test
    void shouldNotModifyTheStateIfOriginalListIsChanged() {
        List<String> transactions = new ArrayList<>(List.of("TXN-001"));
        TransactionSummaryRecord transactionSummaryRecord = new TransactionSummaryRecord("ACC-001", BigDecimal.TEN, transactions);
        transactions.add("TXN-002");

        assertEquals(
                List.of("TXN-001"),
                transactionSummaryRecord.transactionIds()
        );
    }

    @Test
    void shouldThrowUnsupportedOperationExceptionWhenStoredListMutationIsAttempted() {
        List<String> transactions = new ArrayList<>(List.of("TXN-001"));
        TransactionSummaryRecord transactionSummaryRecord
                = new TransactionSummaryRecord("ACC-001", BigDecimal.TEN, transactions);
        assertThrows(UnsupportedOperationException.class, () -> {
            transactionSummaryRecord
                    .transactionIds().add("TXN-002");
        });
    }

    @Test
    void shouldBeEqualWhenAllValuesAreEqual() {
        TransactionSummaryRecord first =
                new TransactionSummaryRecord(
                        "ACC-001",
                        BigDecimal.TEN,
                        List.of("TXN-001")
                );

        TransactionSummaryRecord second =
                new TransactionSummaryRecord(
                        "ACC-001",
                        BigDecimal.TEN,
                        List.of("TXN-001")
                );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenTotalAmountDiffers() {
        TransactionSummaryRecord first =
                new TransactionSummaryRecord(
                        "ACC-001",
                        new BigDecimal("10.00"),
                        List.of("TXN-001")
                );

        TransactionSummaryRecord second =
                new TransactionSummaryRecord(
                        "ACC-001",
                        new BigDecimal("20.00"),
                        List.of("TXN-001")
                );

        assertNotEquals(first, second);
    }

}