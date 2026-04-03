package com.example.erp_service_inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    private String tenantId;
    private Long orderId;
    private String paymentStatus;
}