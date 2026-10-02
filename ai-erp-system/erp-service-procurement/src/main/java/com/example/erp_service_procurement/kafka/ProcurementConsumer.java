package com.example.erp_service_procurement.kafka;

import com.example.erp_service_procurement.entity.ProcessedEvent;
import com.example.erp_service_procurement.repository.ProcessedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.time.LocalDateTime;

@Slf4j
@Service
public class ProcurementConsumer {

private final ProcessedEventRepository processedEventRepository;

public ProcurementConsumer(ProcessedEventRepository processedEventRepository) {
    this.processedEventRepository = processedEventRepository;
}

    @KafkaListener(topics = "payment-events", groupId = "procurement-group")
    public void handlePaymentEvent(Map<String, Object> event) {
        if (isAlreadyProcessed(event)) return;
        log.info("📦 PROCUREMENT: Received payment event: {}", event);
        // Handle payment confirmation from Finance service
        // Update invoice status based on payment
        markProcessed(event, "PAYMENT_RECEIVED");
    }

    @KafkaListener(topics = "inventory-events", groupId = "procurement-group")
    public void handleInventoryEvent(Map<String, Object> event) {
        if (isAlreadyProcessed(event)) return;
        log.info("📦 PROCUREMENT: Received inventory event: {}", event);
        // Handle inventory updates (e.g., stock level changes)
        markProcessed(event, "INVENTORY_UPDATED");
    }

    private boolean isAlreadyProcessed(Map<String, Object> event) {
        String eventId = String.valueOf(event.get("eventId"));
        String tenantId = String.valueOf(event.getOrDefault("tenantId", "unknown"));
        return "null".equals(eventId)
                || processedEventRepository.existsByTenantIdAndEventId(tenantId, eventId);
    }

    private void markProcessed(Map<String, Object> event, String eventType) {
        String eventId = String.valueOf(event.get("eventId"));
        String tenantId = String.valueOf(event.getOrDefault("tenantId", "unknown"));
        ProcessedEvent processed = new ProcessedEvent();
        processed.setEventId(eventId);
        processed.setTenantId(tenantId);
        processed.setEventType(eventType);
        processed.setProcessedAt(LocalDateTime.now());
        processedEventRepository.save(processed);
    }
}
