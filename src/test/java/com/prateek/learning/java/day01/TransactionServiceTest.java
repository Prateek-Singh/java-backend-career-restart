package com.prateek.learning.java.day01;

import com.prateek.learning.transaction.exception.InvalidTransactionAmountException;
import com.prateek.learning.transaction.exception.TransactionNotFoundException;
import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.model.TransactionType;
import com.prateek.learning.transaction.repository.InMemoryTransactionRepository;
import com.prateek.learning.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(new InMemoryTransactionRepository());
    }

    @Test
    void shouldReturnEmptyWhenTransactionsAreNull() {
        assertTrue(
                transactionService
                        .findByAccountId(null, "ACC-1001")
                        .isEmpty()
        );
    }

    @Test
    void shouldReturnZeroWhenTransactionAmountIsNull() {
        Transaction transaction = new Transaction();
        List<Transaction> transactions = List.of(transaction);

        BigDecimal result =
                transactionService.calculateTotalAmount(transactions);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void shouldFindDescriptionUsingCaseInsensitivePartialKeyword() {
        List<Transaction> result =
                transactionService.findByDescriptionKeyword(
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
                transactionService.removeDuplicatesById(
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
                transactionService.calculateTotalAmount(
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
                transactionService.calculateTotalAmountPerAccount(
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
                () -> transactionService.findById(createSampleTransactions(), "")
        );

        assertEquals("Transaction ID is required", exception.getMessage());
    }

    @Test
    void shouldThrowTransactionNotFoundExceptionForNullTransactions() {
        TransactionNotFoundException exception = assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.findById(null, "TXN-001")
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
                () -> transactionService.findById(
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
    void shouldThrowInvalidTransactionAmountExceptionForNullAmount() {
        InvalidTransactionAmountException exception = assertThrows(
                InvalidTransactionAmountException.class,
                () -> transactionService.validateAmount(null)
        );

        assertEquals(
                "Transaction amount is required",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowInvalidTransactionAmountExceptionForZeroAmount() {
        InvalidTransactionAmountException exception = assertThrows(
                InvalidTransactionAmountException.class,
                () -> transactionService.validateAmount(BigDecimal.ZERO)
        );

        assertEquals(
                "Transaction amount must be greater than zero",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowInvalidTransactionAmountExceptionForNegativeAmount() {
        InvalidTransactionAmountException exception = assertThrows(
                InvalidTransactionAmountException.class,
                () -> transactionService.validateAmount(
                        new BigDecimal("-15.00")
                )
        );

        assertEquals(
                "Transaction amount must be greater than zero",
                exception.getMessage()
        );
    }

    @Test
    void shouldNotThrowExceptionForPositiveAmount() {
        assertDoesNotThrow(
                () -> transactionService.validateAmount(
                        new BigDecimal("15.00")
                )
        );
    }

    @Test
    void shouldReturnExistingTransaction() {
        Transaction transaction = transactionService.findById(
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
                LocalDateTime.of(2026, 8, 1, 9, 30)
        ));

        transactions.add(new Transaction(
                "TXN-001",
                "ACC-1001",
                new BigDecimal("-1250.50"),
                TransactionType.DEBIT,
                "Electricity Bill Payment",
                LocalDateTime.of(2026, 8, 1, 11, 15)
        ));

        transactions.add(new Transaction(
                "TXN-003",
                "ACC-1002",
                new BigDecimal("5000.00"),
                TransactionType.CREDIT,
                "Freelance Project PAYMENT",
                LocalDateTime.of(2026, 8, 1, 12, 0)
        ));

        transactions.add(new Transaction(
                "TXN-004",
                "ACC-1002",
                new BigDecimal("-750.25"),
                TransactionType.DEBIT,
                "Online Grocery purchase",
                LocalDateTime.of(2026, 8, 1, 14, 10)
        ));

        transactions.add(new Transaction(
                "TXN-005",
                "ACC-1001",
                new BigDecimal("-2200.00"),
                TransactionType.DEBIT,
                "House RENT",
                LocalDateTime.of(2026, 8, 2, 8, 45)
        ));

        transactions.add(new Transaction(
                "TXN-006",
                "ACC-1002",
                new BigDecimal("1500.75"),
                TransactionType.CREDIT,
                "Cashback Reward",
                LocalDateTime.of(2026, 8, 2, 10, 20)
        ));

        transactions.add(new Transaction(
                "TXN-007",
                "ACC-1001",
                new BigDecimal("-499.99"),
                TransactionType.DEBIT,
                "Streaming Subscription",
                LocalDateTime.of(2026, 8, 2, 13, 5)
        ));

        transactions.add(new Transaction(
                "TXN-008",
                "ACC-1002",
                new BigDecimal("-3000.00"),
                TransactionType.TRANSFER,
                "Transfer to SAVINGS account",
                LocalDateTime.of(2026, 8, 2, 15, 30)
        ));

        transactions.add(new Transaction(
                "TXN-009",
                "ACC-1001",
                new BigDecimal("850.00"),
                TransactionType.REFUND,
                "Refund for Online Purchase",
                LocalDateTime.of(2026, 8, 2, 17, 40)
        ));

        transactions.add(new Transaction(
                "TXN-009",
                "ACC-1001",
                new BigDecimal("850.00"),
                TransactionType.REFUND,
                "REFUND for online purchase",
                LocalDateTime.of(2026, 8, 2, 17, 40)
        ));

        return transactions;
    }
}