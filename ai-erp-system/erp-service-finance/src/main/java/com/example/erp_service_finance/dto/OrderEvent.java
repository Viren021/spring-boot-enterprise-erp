package com.example.erp_service_finance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private String tenantId;
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private String status;
}