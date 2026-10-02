package com.example.erp_service_finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Stable inbound contract for accounting integrations. Producers must publish
 * one event per business document and retain the same eventId on retries.
 */
public record FinanceIntegrationEvent(
        String eventId,
        String tenantId,
        String eventType,
        String sourceDocumentId,
        BigDecimal amount,
        LocalDate eventDate,
        String description,
        String direction) {
}
