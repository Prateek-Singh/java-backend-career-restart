package com.prateek.learning.kafka.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class KafkaValidationConfig implements KafkaListenerConfigurer {

    private final LocalValidatorFactoryBean validator;

    public KafkaValidationConfig(LocalValidatorFactoryBean validator) {
        this.validator = validator;
    }

    @Override
    public void configureKafkaListeners(
            KafkaListenerEndpointRegistrar registrar) {
        registrar.setValidator(validator);
    }
}