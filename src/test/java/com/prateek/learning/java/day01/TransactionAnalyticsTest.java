package com.prateek.learning.java.day01;

import com.prateek.learning.transaction.exception.TransactionNotFoundException;
import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.model.TransactionType;
import com.prateek.learning.transaction.repository.InMemoryTransactionRepository;
import com.prateek.learning.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TransactionAnalyticsTest {

    private TransactionAnalytics transactionAnalytics;

    @BeforeEach
    void setUp() {
        transactionAnalytics = new TransactionAnalytics();
    }

    @Test
    void shouldReturnEmptyWhenTransactionsAreNull() {
        assertTrue(
                transactionAnalytics
                        .findByAccountId(null, "ACC-1001")
                        .isEmpty()
        );
    }

    @Test
    void shouldReturnZeroWhenTransactionAmountIsNull() {
        Transaction transaction = new Transaction();
        List<Transaction> transactions = List.of(transaction);

        BigDecimal result =
                transactionAnalytics.calculateTotalAmount(transactions);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void shouldFindDescriptionUsingCaseInsensitivePartialKeyword() {
        List<Transaction> result =
                transactionAnalytics.findByDescriptionKeyword(
                        createSampleTransactions(),
                        "freelance project"
                );

        assertEquals(1, result.size());
        assertEquals("TXN-003", result.get(0).getId());
        assertEquals(
                "Freelance Project PAYMENT",
                result.get(0).getDescription()
        );
    }

    @Test
    void shouldKeepFirstTransactionWhenIdsAreDuplicated() {
        List<Transaction> result =
                transactionAnalytics.removeDuplicatesById(
                        createSampleTransactions()
                );

        assertEquals(8, result.size());

        List<Transaction> txn001Transactions = result.stream()
                .filter(txn -> "TXN-001".equals(txn.getId()))
                .toList();

        assertEquals(1, txn001Transactions.size());
        assertEquals(
                "Monthly Salary",
                txn001Transactions.get(0).getDescription()
        );

        List<Transaction> txn009Transactions = result.stream()
                .filter(txn -> "TXN-009".equals(txn.getId()))
                .toList();

        assertEquals(1, txn009Transactions.size());
        assertEquals(
                "Refund for Online Purchase",
                txn009Transactions.get(0).getDescription()
        );
    }

    @Test
    void shouldCalculateTotalAmount() {
        BigDecimal result =
                transactionAnalytics.calculateTotalAmount(
                        createSampleTransactions()
                );

        assertEquals(
                0,
                new BigDecimal("25500.01").compareTo(result)
        );
    }

    @Test
    void shouldCalculateTotalAmountPerAccount() {
        Map<String, BigDecimal> result =
                transactionAnalytics.calculateTotalAmountPerAccount(
                        createSampleTransactions()
                );

        assertEquals(2, result.size());

        assertEquals(
                0,
                new BigDecimal("22749.51")
                        .compareTo(result.get("ACC-1001"))
        );

        assertEquals(
                0,
                new BigDecimal("2750.50")
                        .compareTo(result.get("ACC-1002"))
        );
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForBlankTransactionId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionAnalytics.findById(createSampleTransactions(), "")
        );

        assertEquals("Transaction id is required", exception.getMessage());
    }

    @Test
    void shouldThrowTransactionNotFoundExceptionForNullTransactions() {
        TransactionNotFoundException exception = assertThrows(
                TransactionNotFoundException.class,
                () -> transactionAnalytics.findById(null, "TXN-001")
        );

        assertEquals(
                "Transaction not found for ID: TXN-001",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowTransactionNotFoundExceptionForMissingTransactionId() {
        TransactionNotFoundException exception = assertThrows(
                TransactionNotFoundException.class,
                () -> transactionAnalytics.findById(
                        createSampleTransactions(),
                        "TXN-0015"
                )
        );

        assertEquals(
                "Transaction not found for ID: TXN-0015",
                exception.getMessage()
        );
    }

    @Test
    void shouldReturnExistingTransaction() {
        Transaction transaction = transactionAnalytics.findById(
                createSampleTransactions(),
                "TXN-001"
        );

        assertNotNull(transaction);
        assertEquals("TXN-001", transaction.getId());
    }

    private static List<Transaction> createSampleTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        transactions.add(new Transaction(
                "TXN-001",
                "ACC-1001",
                new BigDecimal("25000.00"),
                TransactionType.CREDIT,
                "Monthly Salary",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-001",
                "ACC-1001",
                new BigDecimal("-1250.50"),
                TransactionType.DEBIT,
                "Electricity Bill Payment",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-003",
                "ACC-1002",
                new BigDecimal("5000.00"),
                TransactionType.CREDIT,
                "Freelance Project PAYMENT",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-004",
                "ACC-1002",
                new BigDecimal("-750.25"),
                TransactionType.DEBIT,
                "Online Grocery purchase",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-005",
                "ACC-1001",
                new BigDecimal("-2200.00"),
                TransactionType.DEBIT,
                "House RENT",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-006",
                "ACC-1002",
                new BigDecimal("1500.75"),
                TransactionType.CREDIT,
                "Cashback Reward",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-007",
                "ACC-1001",
                new BigDecimal("-499.99"),
                TransactionType.DEBIT,
                "Streaming Subscription",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-008",
                "ACC-1002",
                new BigDecimal("-3000.00"),
                TransactionType.TRANSFER,
                "Transfer to SAVINGS account",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-009",
                "ACC-1001",
                new BigDecimal("850.00"),
                TransactionType.REFUND,
                "Refund for Online Purchase",
                Instant.now()
        ));

        transactions.add(new Transaction(
                "TXN-009",
                "ACC-1001",
                new BigDecimal("850.00"),
                TransactionType.REFUND,
                "REFUND for online purchase",
                Instant.now()
        ));

        return transactions;
    }
}