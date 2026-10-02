package com.example.erp_service_inventory.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@Table(name = "stock_reservations")
public class StockReservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tenant_id", nullable = false) private String tenantId;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "warehouse_id", nullable = false) private Long warehouseId;
    @Column(name = "location_id") private Long locationId;
    @Column(nullable = false, precision = 19, scale = 3) private BigDecimal quantity;
    @Column(nullable = false) private String status = "ACTIVE";
    private String reference;
    @Column(nullable = false) private Instant createdAt = Instant.now();
}
