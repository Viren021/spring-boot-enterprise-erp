package com.example.erp_service_inventory.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@Table(name = "inventory_reconciliations", indexes = @Index(
        name = "idx_inventory_reconciliation_scope",
        columnList = "tenant_id,product_id,warehouse_id,location_id,created_at"))
public class InventoryReconciliation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;
    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "expected_quantity", nullable = false, precision = 19, scale = 3)
    private BigDecimal expectedQuantity;
    @Column(name = "actual_quantity", nullable = false, precision = 19, scale = 3)
    private BigDecimal actualQuantity;
    @Column(name = "quantity_discrepancy", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantityDiscrepancy;
    @Column(name = "expected_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal expectedValue;
    @Column(name = "actual_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal actualValue;
    @Column(name = "value_discrepancy", nullable = false, precision = 19, scale = 2)
    private BigDecimal valueDiscrepancy;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(length = 500)
    private String notes;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
