package com.example.erp_service_sales.entity;
import jakarta.persistence.*; import lombok.*; import java.math.*; import java.time.*;
@Entity @Getter @Setter @NoArgsConstructor @Table(name="sales_payments")
public class Payment { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="tenant_id",nullable=false) private String tenantId; @Column(nullable=false) private Long invoiceId; @Column(nullable=false) private BigDecimal amount; private String method; private String reference; private Instant paidAt=Instant.now(); }
