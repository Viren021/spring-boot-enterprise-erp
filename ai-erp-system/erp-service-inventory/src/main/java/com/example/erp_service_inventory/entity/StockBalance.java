package com.example.erp_service_inventory.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "stock_balances", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "product_id", "warehouse_id", "location_id"}))
public class StockBalance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tenant_id", nullable = false) private String tenantId;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "warehouse_id", nullable = false) private Long warehouseId;
    @Column(name = "location_id") private Long locationId;
    @Column(nullable = false, precision = 19, scale = 3) private BigDecimal quantity = BigDecimal.ZERO;
    @Column(name = "reserved_quantity", nullable = false, precision = 19, scale = 3) private BigDecimal reservedQuantity = BigDecimal.ZERO;
}
