package com.example.erp_service_sales.entity;
import jakarta.persistence.*; import lombok.*; import java.math.*;
@Entity @Getter @Setter @NoArgsConstructor @Table(name="sales_order_lines")
public class SalesOrderLine { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="tenant_id",nullable=false) private String tenantId; @Column(nullable=false) private Long orderId; @Column(nullable=false) private Long productId; @Column(nullable=false) private BigDecimal quantity; @Column(nullable=false) private BigDecimal unitPrice; private BigDecimal lineTotal; }
