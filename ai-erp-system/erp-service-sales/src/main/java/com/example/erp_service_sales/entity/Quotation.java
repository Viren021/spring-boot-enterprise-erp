package com.example.erp_service_sales.entity;
import jakarta.persistence.*; import lombok.*; import java.math.*; import java.time.*;
@Entity @Getter @Setter @NoArgsConstructor @Table(name="sales_quotations")
public class Quotation { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="tenant_id",nullable=false) private String tenantId; @Column(nullable=false) private Long customerId; @Column(nullable=false,unique=true) private String quoteNumber; private String status="DRAFT"; private LocalDate validUntil; private BigDecimal total=BigDecimal.ZERO; }
