package com.example.erp_service_finance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private String tenantId;
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private String status;
    /** Explicit order total. Required unless unitPrice is supplied. */
    private BigDecimal amount;
    /** Unit price for legacy producers that do not publish a total. */
    private BigDecimal unitPrice;
    /** Stable producer event id, when available, for idempotent processing. */
    private String eventId;
    /** Alternate name used by some event producers. */
    private String sourceEventId;

    // Keep source compatibility with the original five-field Kafka payload.
    public OrderEvent(String tenantId, Long orderId, Long productId, Integer quantity, String status) {
        this(tenantId, orderId, productId, quantity, status, null, null, null, null);
    }
}