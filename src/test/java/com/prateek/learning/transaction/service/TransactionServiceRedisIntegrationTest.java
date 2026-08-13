package com.prateek.learning.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prateek.learning.common.exception.RedisCacheErrorHandler;
import com.prateek.learning.kafka.producer.TransactionEventPublisher;
import com.prateek.learning.transaction.config.CacheConfig;
import com.prateek.learning.transaction.model.Transaction;
import com.prateek.learning.transaction.model.TransactionType;
import com.prateek.learning.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest(classes = {
        TransactionServiceRedisIntegrationTest.TestConfig.class,
        CacheConfig.class,
        RedisCacheErrorHandler.class
})
@ActiveProfiles("jpa")
class TransactionServiceRedisIntegrationTest {

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7.4"))
                    .withExposedPorts(6379);

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(
                cacheManager.getCache("transactions")
        ).clear();

        reset(transactionRepository);
    }

    @Configuration
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
                TransactionRepository transactionRepository, TransactionEventPublisher transactionEventPublisher) {
            return new TransactionService(transactionRepository, transactionEventPublisher);
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper()
                    .findAndRegisterModules();
        }

        @Bean
        RedisConnectionFactory redisConnectionFactory() {
            return new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
        }

        @Bean
        CacheManager cacheManager(
                RedisConnectionFactory redisConnectionFactory,
                RedisCacheConfiguration redisCacheConfiguration) {

            return RedisCacheManager.builder(redisConnectionFactory)
                    .cacheDefaults(redisCacheConfiguration)
                    .build();
        }
    }

    @Test
    void shouldCacheTransactionInRedisAndAvoidSecondRepositoryLookup() {
        Transaction transaction = new Transaction(
                "TXN-REDIS-TEST",
                "ACC-101",
                BigDecimal.TEN,
                TransactionType.CREDIT,
                "Redis integration test",
                Instant.now()
        );

        when(transactionRepository.findById("TXN-REDIS-TEST"))
                .thenReturn(Optional.of(transaction));

        Transaction first =
                transactionService.getTransactionById("TXN-REDIS-TEST");

        Transaction second =
                transactionService.getTransactionById("TXN-REDIS-TEST");

        assertEquals(first.getId(), second.getId());
        assertEquals(first.getAccountId(), second.getAccountId());
        assertEquals(first.getAmount(), second.getAmount());

        verify(transactionRepository, times(1))
                .findById("TXN-REDIS-TEST");
    }
}
