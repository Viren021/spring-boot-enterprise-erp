package com.example.erp_service_sales.entity;
import jakarta.persistence.*; import lombok.*; import java.math.*;
@Entity @Getter @Setter @NoArgsConstructor @Table(name="sales_quotation_lines")
public class QuotationLine { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="tenant_id",nullable=false) private String tenantId; @Column(nullable=false) private Long quotationId; @Column(nullable=false) private Long productId; @Column(nullable=false) private BigDecimal quantity; @Column(nullable=false) private BigDecimal unitPrice; private BigDecimal lineTotal; }
