package com.example.erp_service_finance.kafka;

import com.example.erp_service_finance.dto.OrderEvent;
import com.example.erp_service_finance.dto.PaymentEvent;
import com.example.erp_service_finance.dto.FinanceIntegrationEvent;
import com.example.erp_service_finance.config.TenantContext;
import com.example.erp_service_finance.service.FinanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
/**
 * Retained only for source compatibility. Order events are handled by
 * OrderEventListener; having a second listener would race it and bypass
 * double-entry accounting.
 */
@Service
public class FinanceConsumer {

    private final FinanceProducer financeProducer;
    private final FinanceService financeService;
    private final ObjectMapper objectMapper;

    // 🌟 Inject the Producer via constructor
    public FinanceConsumer(FinanceProducer financeProducer, FinanceService financeService, ObjectMapper objectMapper) {
        this.financeProducer = financeProducer; this.financeService = financeService; this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "finance-integration-events", groupId = "finance-integration",
            containerFactory = "financeIntegrationKafkaListenerContainerFactory")
    public void handleIntegrationEvent(String payload) {
        try {
            String json = payload;
            if (json != null && json.startsWith("\"")) json = objectMapper.readValue(json, String.class);
            FinanceIntegrationEvent event = objectMapper.readValue(json, FinanceIntegrationEvent.class);
            if (event.tenantId() == null || event.tenantId().isBlank()) throw new IllegalArgumentException("tenantId is required");
            TenantContext.setTenantId(event.tenantId());
            financeService.recordIntegrationEvent(event);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid finance integration event: " + ex.getMessage(), ex);
        } finally { TenantContext.clear(); }
    }
}