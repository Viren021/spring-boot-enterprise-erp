package com.example.erp_service_sales.entity;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal;
@Entity @Getter @Setter @NoArgsConstructor @Table(name="sales_customers", uniqueConstraints=@UniqueConstraint(columnNames={"tenant_id","code"}))
public class Customer { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="tenant_id",nullable=false) private String tenantId; @Column(nullable=false) private String code; @Column(nullable=false) private String name; private String email; private String phone; private String billingAddress; private BigDecimal creditLimit=BigDecimal.ZERO; private boolean active=true; }
