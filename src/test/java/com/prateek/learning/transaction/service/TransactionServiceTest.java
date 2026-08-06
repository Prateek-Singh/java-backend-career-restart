package com.prateek.learning.transaction.service;

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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void shouldSaveAndReturnTransactionWhenValid() {

        BigDecimal amount = new BigDecimal("25.00");

        CreateTransactionRequest request = new CreateTransactionRequest(
                "Txn-123",
                "ACC-111",
                amount,
                TransactionType.CREDIT,
                "Monthly EMI"
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction savedTxn = transactionService.createTransaction(request);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        verify(transactionRepository).save(captor.capture());

        Transaction transaction = captor.getValue();

        assertEquals(request.id(), transaction.getId());
        assertEquals(request.accountId(), transaction.getAccountId());
        assertEquals(request.amount(), transaction.getAmount());
        assertEquals(request.type(), transaction.getType());
        assertEquals(request.description(), transaction.getDescription());
        assertNotNull(transaction.getTimestamp());
        assertSame(savedTxn, transaction);
    }

    @Test
    void shouldReturnTransactionsByAccountId() {
        Transaction transaction1 = new Transaction(
                "Txn-111",
                "ACC-111",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly EMI",
                LocalDateTime.now()
        );

        Transaction transaction2 = new Transaction(
                "Txn-112",
                "ACC-111",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Monthly EMI",
                LocalDateTime.now()
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
    void shouldReturnEmptyTransactionsByAccountId() {
        List<Transaction> repositoryResult = Collections.emptyList();

        when(transactionRepository.findByAccountId("ACC-111"))
                .thenReturn(repositoryResult);

        List<Transaction> result =
                transactionService.findByAccountId("ACC-111");

        assertSame(repositoryResult, result);

        verify(transactionRepository)
                .findByAccountId("ACC-111");
    }
}