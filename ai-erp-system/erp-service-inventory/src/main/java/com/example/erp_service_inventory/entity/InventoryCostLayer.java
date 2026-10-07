package com.example.erp_service_inventory.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@Table(name = "inventory_cost_layers", indexes = {
        @Index(name = "idx_cost_layer_balance", columnList = "tenant_id,product_id,warehouse_id,location_id")})
public class InventoryCostLayer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="tenant_id", nullable=false) private String tenantId;
    @Column(name="product_id", nullable=false) private Long productId;
    @Column(name="warehouse_id", nullable=false) private Long warehouseId;
    @Column(name="location_id") private Long locationId;
    @Column(name="source_movement_id") private Long sourceMovementId;
    @Column(name="quantity_received", nullable=false, precision=19, scale=3) private BigDecimal quantityReceived;
    @Column(name="quantity_remaining", nullable=false, precision=19, scale=3) private BigDecimal quantityRemaining;
    @Column(name="unit_cost", nullable=false, precision=19, scale=6) private BigDecimal unitCost;
    @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
