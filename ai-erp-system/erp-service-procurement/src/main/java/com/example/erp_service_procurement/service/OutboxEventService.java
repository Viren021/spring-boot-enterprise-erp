package com.example.erp_service_procurement.service;

import com.example.erp_service_procurement.entity.OutboxEvent;
import com.example.erp_service_procurement.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class OutboxEventService {
    private static final int MAX_ATTEMPTS = 5;

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OutboxEventService(OutboxEventRepository repository, ObjectMapper objectMapper,
                              KafkaTemplate<String, Object> kafkaTemplate) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public OutboxEvent enqueue(String topic, String eventType, String aggregateType,
                               Map<String, Object> payload) {
        try {
            OutboxEvent event = new OutboxEvent();
            event.setId(UUID.randomUUID().toString());
            event.setTopic(topic);
            event.setEventType(eventType);
            event.setAggregateType(aggregateType);
            event.setPayload(objectMapper.writeValueAsString(payload));
            event.setStatus("PENDING");
            event.setAttemptCount(0);
            event.setNextAttemptAt(LocalDateTime.now());
            event.setCreatedAt(LocalDateTime.now());
            return repository.save(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize procurement event", ex);
        }
    }

    @Scheduled(fixedDelayString = "${procurement.outbox.poll-delay-ms:5000}")
    @Transactional
    public void publishPending() {
        repository.findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                        "PENDING", LocalDateTime.now())
                .forEach(this::publish);
    }

    private void publish(OutboxEvent event) {
        try {
            Map<?, ?> payload = objectMapper.readValue(event.getPayload(), Map.class);
            kafkaTemplate.send(event.getTopic(), payload).get();
            event.setStatus("PUBLISHED");
            event.setPublishedAt(LocalDateTime.now());
            event.setLastError(null);
        } catch (Exception ex) {
            int attempts = event.getAttemptCount() + 1;
            event.setAttemptCount(attempts);
            event.setLastError(ex.getMessage());
            event.setNextAttemptAt(LocalDateTime.now().plusSeconds(Math.min(300, 5L * attempts * attempts)));
            if (attempts >= MAX_ATTEMPTS) {
                event.setStatus("DEAD_LETTER");
                event.setDeadLetteredAt(LocalDateTime.now());
            }
            log.warn("Failed to publish procurement outbox event {} (attempt {})", event.getId(), attempts);
        }
        repository.save(event);
    }
}
