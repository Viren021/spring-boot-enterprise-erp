package com.example.erp_service_inventory.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@Table(name = "stock_movements", uniqueConstraints = @UniqueConstraint(
        name = "uk_stock_movement_tenant_idempotency", columnNames = {"tenant_id", "idempotency_key"}))
public class StockMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tenant_id", nullable = false) private String tenantId;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "warehouse_id", nullable = false) private Long warehouseId;
    @Column(name = "location_id") private Long locationId;
    @Column(name = "movement_type", nullable = false) private String movementType;
    @Column(nullable = false, precision = 19, scale = 3) private BigDecimal quantity;
    private String reference;
    @Column(name = "idempotency_key", length = 200) private String idempotencyKey;
    @Column(nullable = false, length = 20) private String status = "POSTED";
    @Column(name = "created_by", length = 200) private String createdBy;
    @Column(name = "approved_by", length = 200) private String approvedBy;
    @Column(nullable = false) private Instant occurredAt = Instant.now();
}
