package com.prateek.learning.transaction.repository;

import com.prateek.learning.transaction.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
                "CREDIT",
                "Monthly EMI",
                LocalDateTime.now());

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
                "CREDIT",
                "Monthly EMI",
                LocalDateTime.now());

        Transaction duplicate = new Transaction(
                "Txn-111",
                "ACC-222",
                BigDecimal.ONE,
                "DEBIT",
                "Different transaction",
                LocalDateTime.now()
        );

        repository.save(txn);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.save(duplicate);
        });

        assertEquals("Transaction already exists", exception.getMessage());
    }

//    @Test
//    void shouldReturnTransactionWhenTransactionExists() {
//        Transaction txn = new Transaction(
//                "Txn-111",
//                "ACC-111",
//                BigDecimal.TEN,
//                "CREDIT",
//                "Monthly EMI",
//                LocalDateTime.now());
//
//        repository.save(txn);
//
//        Optional<Transaction> optionalTransaction = repository.findById("Txn-111");
//        assertTrue(optionalTransaction.isPresent());
//        Transaction transaction = optionalTransaction.get();
//        assertEquals(txn.getId(), transaction.getId());
//        assertEquals(txn.getAccountId(), transaction.getAccountId());
//    }

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
                "CREDIT",
                "Monthly EMI",
                LocalDateTime.now()
        );

        Transaction savedTransaction = repository.save(transaction);

        Optional<Transaction> retrievedTransaction =
                repository.findById("Txn-111");

        assertTrue(retrievedTransaction.isPresent());
        assertSame(savedTransaction, retrievedTransaction.get());
    }
}