package com.prateek.learning.kafka.config;

import com.prateek.learning.kafka.event.TransactionCreatedEvent;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaConsumerErrorHandlingConfig {

    private static final String DLT_TOPIC =
            "transaction-events-dlt";

    @Bean
    DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(
                                        DLT_TOPIC,
                                        record.partition()
                                )
                );

        ExponentialBackOffWithMaxRetries backOff =
                new ExponentialBackOffWithMaxRetries(3);

        backOff.setInitialInterval(1_000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(4_000L);

        return new DefaultErrorHandler(
                recoverer,
                backOff
        );
    }
}