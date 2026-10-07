package com.example.erp_service_sales.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "sales_approval_policies", indexes = @Index(
        name = "idx_sales_approval_policy_scope", columnList = "tenant_id,document_type,active"))
public class SalesApprovalPolicy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "tenant_id", nullable = false) private String tenantId;
    @Column(name = "document_type", nullable = false) private String documentType;
    @Column(name = "min_amount", nullable = false, precision = 19, scale = 2) private BigDecimal minAmount = BigDecimal.ZERO;
    @Column(name = "max_amount", precision = 19, scale = 2) private BigDecimal maxAmount;
    @Column(name = "maker_checker", nullable = false) private boolean makerChecker = true;
    @Column(nullable = false) private boolean active = true;
}
