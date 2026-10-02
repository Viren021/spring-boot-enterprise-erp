package com.example.erp_service_sales.entity;
import jakarta.persistence.*; import lombok.*; import java.math.*; import java.time.*;
@Entity @Getter @Setter @NoArgsConstructor @Table(name="sales_invoices")
public class Invoice { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="tenant_id",nullable=false) private String tenantId; @Column(nullable=false) private Long orderId; @Column(nullable=false,unique=true) private String invoiceNumber; private BigDecimal total=BigDecimal.ZERO; private BigDecimal paidAmount=BigDecimal.ZERO; private String status="OPEN"; private LocalDate dueDate; }
