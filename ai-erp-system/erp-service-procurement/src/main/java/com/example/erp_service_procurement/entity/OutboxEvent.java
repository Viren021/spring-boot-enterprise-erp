package com.example.erp_service_procurement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "procurement_outbox_events",
        indexes = @Index(name = "idx_outbox_pending", columnList = "status,nextAttemptAt"))
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 150)
    private String topic;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, length = 100)
    private String aggregateType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private int attemptCount;

    @Column(nullable = false)
    private LocalDateTime nextAttemptAt;

    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private LocalDateTime deadLetteredAt;
    private String lastError;
}
