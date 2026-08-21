package com.prateek.learning.kafka.config;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.BackOffExecution;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaRetryBackoffPolicyTest {

    @Test
    void shouldConfigureThreeRetriesWithExponentialBackoff() {

        ExponentialBackOffWithMaxRetries backOff =
                new ExponentialBackOffWithMaxRetries(3);

        backOff.setInitialInterval(1_000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(4_000L);

        BackOffExecution execution =
                backOff.start();

        assertThat(execution.nextBackOff())
                .isEqualTo(1_000L);

        assertThat(execution.nextBackOff())
                .isEqualTo(2_000L);

        assertThat(execution.nextBackOff())
                .isEqualTo(4_000L);

        assertThat(execution.nextBackOff())
                .isEqualTo(BackOffExecution.STOP);
    }
}