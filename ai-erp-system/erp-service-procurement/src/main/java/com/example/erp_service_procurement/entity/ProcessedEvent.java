package com.example.erp_service_procurement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_business_events",
        uniqueConstraints = @UniqueConstraint(name = "uk_processed_event_tenant_id",
                columnNames = {"tenantId", "eventId"}))
@Getter
@Setter
@NoArgsConstructor
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private LocalDateTime processedAt;

    @Column(nullable = false)
    private int attemptCount = 1;

    private LocalDateTime lastFailedAt;
    private String lastError;
}
