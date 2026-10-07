package com.example.erp_service_inventory.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "inventory_approval_policies", indexes = @Index(
        name = "idx_inventory_approval_policy_scope", columnList = "tenant_id,operation,active"))
public class InventoryApprovalPolicy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tenant_id", nullable = false) private String tenantId;
    @Column(nullable = false) private String operation;
    @Column(name = "min_amount", nullable = false, precision = 19, scale = 2) private BigDecimal minAmount = BigDecimal.ZERO;
    @Column(name = "maker_checker", nullable = false) private boolean makerChecker = true;
    @Column(nullable = false) private boolean active = true;
}
