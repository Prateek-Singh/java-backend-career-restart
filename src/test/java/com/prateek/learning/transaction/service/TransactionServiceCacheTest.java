package com.prateek.learning.transaction.service;

import com.prateek.learning.kafka.producer.TransactionEventPublisher;
import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.model.TransactionType;
import com.prateek.learning.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = TransactionServiceCacheTest.TestConfig.class)
class TransactionServiceCacheTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(cacheManager.getCache("transactions")).clear();
        reset(transactionRepository);
    }

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        TransactionRepository transactionRepository() {
            return mock(TransactionRepository.class);
        }

        @Bean
        TransactionEventPublisher transactionEventPublisher() {
            return mock(TransactionEventPublisher.class);
        }

        @Bean
        TransactionService transactionService(
                TransactionRepository transactionRepository,
                TransactionEventPublisher transactionEventPublisher) {
            return new TransactionService(transactionRepository, transactionEventPublisher);
        }

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("transactions");
        }
    }

    @Test
    void shouldUseCacheForRepeatedTransactionLookup() {
        Transaction transaction = new Transaction(
                "TXN-101",
                "ACC-101",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Test transaction",
                Instant.now()
        );

        when(transactionRepository.findById("TXN-101"))
                .thenReturn(Optional.of(transaction));

        Transaction first =
                transactionService.getTransactionById("TXN-101");

        Transaction second =
                transactionService.getTransactionById("TXN-101");

        assertSame(transaction, first);
        assertSame(transaction, second);

        verify(transactionRepository, times(1))
                .findById("TXN-101");
    }
}
