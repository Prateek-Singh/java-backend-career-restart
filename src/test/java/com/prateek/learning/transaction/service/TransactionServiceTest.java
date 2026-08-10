package com.prateek.learning.transaction.service;

import com.prateek.learning.common.exception.DuplicateTransactionException;
import com.prateek.learning.transaction.dto.CreateTransactionRequest;
import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.model.TransactionType;
import com.prateek.learning.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void shouldSaveMappedTransactionAndReturnRepositoryResultWhenRequestIsValid() {

        BigDecimal amount = new BigDecimal("25.00");

        CreateTransactionRequest request = new CreateTransactionRequest(
                "Txn-123",
                "ACC-111",
                amount,
                TransactionType.CREDIT,
                "Monthly EMI"
        );

        Transaction repositoryResult = new Transaction();

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(repositoryResult);

        Transaction result = transactionService.createTransaction(request);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        verify(transactionRepository).save(captor.capture());

        Transaction transaction = captor.getValue();

        assertEquals(request.id(), transaction.getId());
        assertEquals(request.accountId(), transaction.getAccountId());
        assertEquals(request.amount(), transaction.getAmount());
        assertEquals(request.type(), transaction.getType());
        assertEquals(request.description(), transaction.getDescription());
        assertNotNull(transaction.getTimestamp());
        assertSame(repositoryResult, result);
    }

    @Test
    void shouldReturnRepositoryTransactionsWhenAccountIdMatches() {
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

        List<Transaction> repositoryResult = List.of(
                transaction1,
                transaction2
        );

        when(transactionRepository.findByAccountId("ACC-111"))
                .thenReturn(repositoryResult);

        List<Transaction> result =
                transactionService.findByAccountId("ACC-111");

        assertSame(repositoryResult, result);

        verify(transactionRepository)
                .findByAccountId("ACC-111");
    }

    @Test
    void shouldReturnEmptyListWhenRepositoryFindsNoTransactions() {
        List<Transaction> repositoryResult = List.of();

        when(transactionRepository.findByAccountId("ACC-111"))
                .thenReturn(repositoryResult);

        List<Transaction> result =
                transactionService.findByAccountId("ACC-111");

        assertSame(repositoryResult, result);

        verify(transactionRepository)
                .findByAccountId("ACC-111");
    }

    @Test
    void shouldThrowDuplicateTransactionWhenTransactionExists() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TXN-131",
                "ACC-131",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly Savings"
        );

        Transaction repositoryResult = new Transaction();
        repositoryResult.setId(request.id());
        repositoryResult.setAccountId(request.accountId());

        when(transactionRepository.findById(request.id())).thenReturn(Optional.of(repositoryResult));

        assertThatThrownBy(
                () -> transactionService.createTransaction(request))
        .isInstanceOf(DuplicateTransactionException.class)
                .hasMessage("Transaction with id " + request.id() + " already exists");

        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}