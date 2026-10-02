package com.example.erp_service_inventory.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double price;

    @Column(name = "stock_quantity") // <-- ADD THIS LINE!
    private Integer stockQuantity;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel = 0;

    @Column(name = "reorder_quantity", nullable = false)
    private Integer reorderQuantity = 0;

    @Column(nullable = false)
    private Boolean active = true;
}