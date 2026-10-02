package com.example.erp_service_inventory.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "warehouses", uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "code"}))
public class Warehouse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tenant_id", nullable = false) private String tenantId;
    @Column(nullable = false) private String code;
    @Column(nullable = false) private String name;
    private String address;
    @Column(nullable = false) private Boolean active = true;
}
