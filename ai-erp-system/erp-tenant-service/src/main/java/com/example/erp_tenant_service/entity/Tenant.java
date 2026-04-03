package com.example.erp_tenant_service.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenants", schema = "public")
@Data // Lombok automatically generates Getters and Setters
public class Tenant {

    @Id
    @Column(name = "tenant_id", unique = true, nullable = false, length = 50)
    private String tenantId; // e.g., "company_a"

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "db_schema_name", unique = true, nullable = false)
    private String dbSchemaName; // The isolated schema name in Postgres (e.g., "tenant_company_a")

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
