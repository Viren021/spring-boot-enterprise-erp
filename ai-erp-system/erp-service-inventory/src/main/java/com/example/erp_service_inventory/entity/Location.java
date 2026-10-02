package com.example.erp_service_inventory.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "warehouse_locations", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "warehouse_id", "code"}))
public class Location {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tenant_id", nullable = false) private String tenantId;
    @Column(name = "warehouse_id", nullable = false) private Long warehouseId;
    @Column(nullable = false) private String code;
    private String name;
    @Column(nullable = false) private Boolean active = true;
}
