package com.example.erp_service_finance.service;

import com.example.erp_service_finance.config.TenantContext;
import com.example.erp_service_finance.dto.OrderEvent;
import com.example.erp_service_finance.dto.PaymentEvent;
import com.example.erp_service_finance.kafka.FinanceProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Converts an order event into the finance service's normal double-entry
 * journal.  Failures are deliberately propagated to Kafka so invalid events
 * can be retried/dead-lettered instead of being acknowledged as paid.
 */
@Service
public class OrderEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final FinanceService financeService;
    private final FinanceProducer financeProducer;

    public OrderEventListener(FinanceService financeService, FinanceProducer financeProducer) {
        this.financeService = financeService;
        this.financeProducer = financeProducer;
    }

    @KafkaListener(topics = "order-events", groupId = "finance-group")
    public void handleOrderEvent(OrderEvent event) {
        if (event == null || event.getTenantId() == null || event.getTenantId().isBlank()) {
            log.warn("Rejecting order event without tenantId: {}", event);
            throw new IllegalArgumentException("tenantId is required");
        }
        try {
            TenantContext.setTenantId(event.getTenantId());
            financeService.recordOrder(event);
            financeProducer.publishPaymentEvent(new PaymentEvent(
                    event.getTenantId(), event.getOrderId(), "APPROVED"));
            log.info("Recorded order {} in finance journal", event.getOrderId());
        } catch (RuntimeException ex) {
            log.warn("Rejecting order event {} for retry: {}",
                    event.getOrderId(), ex.getMessage());
            throw ex;
        } finally {
            TenantContext.clear();
        }
    }
}
