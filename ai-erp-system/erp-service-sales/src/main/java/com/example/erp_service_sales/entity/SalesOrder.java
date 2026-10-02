package com.example.erp_service_sales.entity;
import jakarta.persistence.*; import lombok.*; import java.math.*; import java.time.*;
@Entity @Getter @Setter @NoArgsConstructor @Table(name="sales_orders")
public class SalesOrder { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="tenant_id",nullable=false) private String tenantId; @Column(nullable=false) private Long customerId; private Long quotationId; @Column(nullable=false,unique=true) private String orderNumber; private String status="PENDING_CREDIT_CHECK"; private BigDecimal total=BigDecimal.ZERO; private BigDecimal reservedAmount=BigDecimal.ZERO; private Instant createdAt=Instant.now(); }
