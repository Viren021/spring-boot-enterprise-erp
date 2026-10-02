package com.example.erp_service_inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private String tenantId; // Critical: Finance needs to know WHICH company's order this is!
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private String status;
    private BigDecimal amount;
    private BigDecimal unitPrice;
    private String eventId;
}