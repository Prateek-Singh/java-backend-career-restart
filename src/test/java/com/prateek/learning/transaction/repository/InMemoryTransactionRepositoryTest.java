package com.prateek.learning.transaction.repository;

import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTransactionRepositoryTest {

    private final InMemoryTransactionRepository repository  = new InMemoryTransactionRepository();

    @Test
    void shouldThrowIllegalArgumentWhenTransactionIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.save(null);
        });
        assertEquals("Transaction cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentWhenTransactionIdIsNull() {
        Transaction txn = new Transaction();
        txn.setId(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.save(txn);
        });
        assertEquals("Transaction id cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentWhenTransactionIdIsBlank() {
        Transaction txn = new Transaction();
        txn.setId(" ");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.save(txn);
        });
        assertEquals("Transaction id cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldReturnTransactionWhenTransactionIsValid() {
        Transaction txn = new Transaction(
                "Txn-111",
                "ACC-111",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly EMI",
                Instant.now());

        Transaction savedTxn = repository.save(txn);

        assertEquals(txn.getId(), savedTxn.getId());
        assertEquals(txn.getAccountId(), savedTxn.getAccountId());
        assertEquals(txn.getAmount(), savedTxn.getAmount());
        assertEquals(txn.getType(), savedTxn.getType());
        assertEquals(txn.getDescription(), savedTxn.getDescription());
        assertNotNull(savedTxn.getTimestamp());
        assertSame(txn, savedTxn);
    }

    @Test
    void shouldThrowIllegalArgumentWhenExistingTransaction() {
        Transaction txn = new Transaction(
                "Txn-111",
                "ACC-111",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly EMI",
                Instant.now());

        Transaction duplicate = new Transaction(
                "Txn-111",
                "ACC-222",
                BigDecimal.ONE,
                TransactionType.DEBIT,
                "Different transaction",
                Instant.now()
        );

        repository.save(txn);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.save(duplicate);
        });

        assertEquals("Transaction already exists", exception.getMessage());
    }

    @Test
    void shouldReturnEmptyWhenTransactionDoesNotExist() {
        Optional<Transaction> optionalTransaction = repository.findById("Txn-111");
        assertTrue(optionalTransaction.isEmpty());
    }

    @Test
    void shouldThrowIllegalArgumentWhenTransactionIdIsInvalid() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.findById(" ");
        });
        assertEquals("Transaction id cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentWhenTransactionIdIsPassedNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.findById(null);
        });
        assertEquals("Transaction id cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldSaveAndRetrieveTransactionById() {
        Transaction transaction = new Transaction(
                "Txn-111",
                "ACC-111",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly EMI",
                Instant.now()
        );

        Transaction savedTransaction = repository.save(transaction);

        Optional<Transaction> retrievedTransaction =
                repository.findById("Txn-111");

        assertTrue(retrievedTransaction.isPresent());
        assertSame(savedTransaction, retrievedTransaction.get());
    }

    @Test
    void shouldSaveAndRetrieveTransactionByAccountId() {
        Transaction transaction1 = new Transaction(
                "Txn-111",
                "ACC-111",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly EMI",
                Instant.now()
        );

        Transaction transaction2 = new Transaction(
                "Txn-112",
                "ACC-112",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly EMI",
                Instant.now()
        );

        repository.save(transaction1);
        repository.save(transaction2);

        List<Transaction> transactions =
                repository.findByAccountId("ACC-111");

        assertEquals(1, transactions.size());
        assertTrue(transactions.contains(transaction1));
    }

    @Test
    void shouldReturnEmptyListWhenAccountIdHasNoTransactions() {
        Transaction transaction1 = new Transaction(
                "Txn-111",
                "ACC-111",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly EMI",
                Instant.now()
        );

        Transaction transaction2 = new Transaction(
                "Txn-112",
                "ACC-112",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly EMI",
                Instant.now()
        );

        repository.save(transaction1);
        repository.save(transaction2);

        List<Transaction> accounts = repository.findByAccountId("ACC-213");
        assertTrue(accounts.isEmpty());
    }

    @Test
    void shouldThrowIllegalArgumentWhenAccountIdIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.findByAccountId(null);
        });
        assertEquals("Account id cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentWhenAccountIdIsBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.findByAccountId(" ");
        });
        assertEquals("Account id cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldReturnMultipleTransactionsByAccountId() {
        Transaction transaction1 = new Transaction(
                "Txn-111",
                "ACC-111",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly EMI",
                Instant.now()
        );

        Transaction transaction2 = new Transaction(
                "Txn-112",
                "ACC-111",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly EMI",
                Instant.now()
        );

        repository.save(transaction1);
        repository.save(transaction2);

        List<Transaction> transactions =
                repository.findByAccountId("ACC-111");

        assertEquals(2, transactions.size());
        assertTrue(transactions.contains(transaction1));
        assertTrue(transactions.contains(transaction2));
    }

    @Test
    void shouldReturnUnmodifiableTransactionList() {
        Transaction transaction1 = new Transaction(
                "Txn-111",
                "ACC-111",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly EMI",
                Instant.now()
        );

        Transaction transaction2 = new Transaction(
                "Txn-112",
                "ACC-111",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly EMI",
                Instant.now()
        );

        repository.save(transaction1);

        List<Transaction> list = repository.findByAccountId("ACC-111");

        assertEquals(1, list.size());

        assertTrue(list.contains(transaction1));

        assertThrows(UnsupportedOperationException.class, () -> list.add(transaction2));

        assertEquals(1, list.size());
        assertFalse(list.contains(transaction2));
    }
}