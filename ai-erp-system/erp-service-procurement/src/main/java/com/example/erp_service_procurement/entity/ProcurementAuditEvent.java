package com.example.erp_service_procurement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "procurement_audit_events")
@Getter
@Setter
@NoArgsConstructor
public class ProcurementAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String documentType;

    @Column(nullable = false)
    private Long documentId;

    @Column(nullable = false)
    private String action;

    private String actor;
    private String details;

    @Column(nullable = false)
    private LocalDateTime occurredAt;
}
