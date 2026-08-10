package com.prateek.learning.transaction.persistence.adapter;

import com.prateek.learning.common.exception.DuplicateTransactionException;
import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.model.TransactionType;
import com.prateek.learning.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("jpa")
@SpringBootTest
class JpaTransactionRepositoryAdapterIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.clear();
    }

    @Test
    void shouldSaveAndReturnTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId("TXN-125");
        transaction.setAccountId("ACC-125");
        transaction.setAmount(new BigDecimal("25.00"));
        transaction.setType(TransactionType.CREDIT);
        transaction.setDescription("Monthly EMI");
        transaction.setTimestamp(Instant.now());

        Transaction savedTransaction = transactionRepository.save(transaction);
        assertNotNull(savedTransaction);

        Transaction retrievedTransaction = transactionRepository
                .findById(savedTransaction.getId())
                .orElseThrow();

        assertThat(retrievedTransaction).isEqualTo(savedTransaction);
    }

    @Test
    void shouldReturnTransactionsMatchingAccountId() {
        Transaction transaction1 = new Transaction(
                "TXN-127",
                "ACC-126",
                new BigDecimal("30.00"),
                TransactionType.CREDIT,
                "Monthly EMI",
                Instant.now()
        );

        Transaction transaction2 = new Transaction(
                "TXN-128",
                "ACC-126",
                new BigDecimal("35.00"),
                TransactionType.DEBIT,
                "Monthly Salary",
                Instant.now()
        );

        Transaction transaction3 = new Transaction(
                "TXN-129",
                "ACC-127",
                new BigDecimal("15.00"),
                TransactionType.CREDIT,
                "Monthly EMI",
                Instant.now()
        );

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
        transactionRepository.save(transaction3);

        List<Transaction> transactions =
                transactionRepository.findByAccountId("ACC-126");

        assertThat(transactions).hasSize(2);

        assertThat(transactions)
                .extracting(Transaction::getId)
                .containsExactlyInAnyOrder("TXN-127", "TXN-128");

        assertThat(transactions)
                .extracting(Transaction::getAccountId)
                .containsOnly("ACC-126");
    }

    @Test
    void shouldThrowDuplicateTransactionExceptionWhenUniqueTransactionIdConstraintIsViolated() {
        Transaction transaction = new Transaction();
        transaction.setId("TXN-125");
        transaction.setAccountId("ACC-125");
        transaction.setAmount(new BigDecimal("25.00"));
        transaction.setType(TransactionType.CREDIT);
        transaction.setDescription("Monthly EMI");
        transaction.setTimestamp(Instant.now());

        Transaction duplicateTransaction = new Transaction();
        duplicateTransaction.setId("TXN-125");
        duplicateTransaction.setAccountId("ACC-125");
        duplicateTransaction.setAmount(new BigDecimal("25.00"));
        duplicateTransaction.setType(TransactionType.CREDIT);
        duplicateTransaction.setDescription("Monthly EMI");
        duplicateTransaction.setTimestamp(Instant.now());

        Transaction savedTransaction = transactionRepository.save(transaction);
        assertNotNull(savedTransaction);

        assertThatThrownBy(
                () -> transactionRepository.save(duplicateTransaction)
        )
        .isInstanceOf(DuplicateTransactionException.class)
        .hasMessage("Transaction with id TXN-125 already exists");
    }
}