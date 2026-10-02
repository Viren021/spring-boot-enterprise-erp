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
@Table(name = "receipts")
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String receiptNumber;

    @Column(nullable = false)
    private Long purchaseOrderId;

    @Column(nullable = false)
    private LocalDateTime receiptDate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "receipt_id")
    private List<ReceiptLineItem> lineItems;

    private String receivedBy;
    private String qualityInspection; // PASSED, FAILED, PARTIAL
    private String inspectionNotes;

    @Column(nullable = false)
    private String status; // RECEIVED, INSPECTED, REJECTED, ACCEPTED

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
