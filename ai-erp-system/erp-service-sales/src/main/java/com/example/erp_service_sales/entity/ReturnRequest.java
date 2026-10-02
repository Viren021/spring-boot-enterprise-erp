package com.example.erp_service_sales.entity;
import jakarta.persistence.*; import lombok.*; import java.math.*; import java.time.*;
@Entity @Getter @Setter @NoArgsConstructor @Table(name="sales_returns")
public class ReturnRequest { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="tenant_id",nullable=false) private String tenantId; @Column(nullable=false) private Long orderId; private String reason; private String status="REQUESTED"; private BigDecimal amount=BigDecimal.ZERO; private Instant createdAt=Instant.now(); }
