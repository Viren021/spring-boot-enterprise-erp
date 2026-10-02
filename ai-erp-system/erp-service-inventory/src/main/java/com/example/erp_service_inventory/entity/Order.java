package com.example.erp_service_inventory.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id") // <-- THIS IS THE FIX!
    private Long productId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    private Integer quantity;
    @Column(name = "unit_price", precision = 19, scale = 4)
    private java.math.BigDecimal unitPrice;
    private String status;
}