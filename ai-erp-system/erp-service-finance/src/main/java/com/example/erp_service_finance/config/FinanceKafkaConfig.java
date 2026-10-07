package com.example.erp_service_finance.config;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import java.util.HashMap;

@Configuration
public class FinanceKafkaConfig {
    @Bean
    public ConsumerFactory<String, String> financeIntegrationConsumerFactory(KafkaProperties properties) {
        HashMap<String, Object> config = new HashMap<>(properties.buildConsumerProperties());
        config.put("value.deserializer", StringDeserializer.class);
        config.put("spring.json.use.type.headers", false);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean("financeIntegrationKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> financeIntegrationKafkaListenerContainerFactory(
            ConsumerFactory<String, String> financeIntegrationConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(financeIntegrationConsumerFactory);
        return factory;
    }
}
