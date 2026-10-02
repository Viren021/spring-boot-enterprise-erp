package com.example.erp_service_sales.entity;
import jakarta.persistence.*; import lombok.*; import java.math.*; import java.time.*;
@Entity @Getter @Setter @NoArgsConstructor @Table(name="sales_credit_notes")
public class CreditNote { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="tenant_id",nullable=false) private String tenantId; @Column(nullable=false) private Long returnId; @Column(nullable=false,unique=true) private String creditNoteNumber; private BigDecimal amount=BigDecimal.ZERO; private String status="ISSUED"; private Instant issuedAt=Instant.now(); }
