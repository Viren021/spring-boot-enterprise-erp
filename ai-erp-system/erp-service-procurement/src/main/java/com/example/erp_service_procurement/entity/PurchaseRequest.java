package com.example.erp_service_procurement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "purchase_requests")
public class PurchaseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String prNumber;

    @Column(nullable = false)
    private String description;

    private String departmentId;
    private String createdBy;

    @Column(nullable = false)
    private String status; // DRAFT, SUBMITTED, APPROVED, REJECTED, CONVERTED_TO_PO

    @Enumerated(EnumType.STRING)
    private PriorityLevel priority;

    private LocalDateTime requiredDate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "purchase_request_id")
    private List<PurchaseRequestLineItem> lineItems;

    private String approverComments;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum PriorityLevel {
        LOW, MEDIUM, HIGH, URGENT
    }
}
