package com.example.erp_service_procurement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private Long vendorId;

    @Column(nullable = false)
    private Long purchaseOrderId;

    private Long receiptId;

    @Column(nullable = false)
    private LocalDateTime invoiceDate;

    private LocalDateTime dueDate;

    @Column(nullable = false)
    private BigDecimal invoiceAmount;

    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private BigDecimal amountPaid;
    private BigDecimal amountDue;

    @Column(nullable = false)
    private String status; // RECEIVED, MATCHED, APPROVED, PAID, PARTIAL, REJECTED

    private String matchingStatus; // THREE_WAY_PASS, THREE_WAY_FAIL, DISCREPANCIES
    private String discrepancyNotes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "invoice_id")
    private List<InvoiceLineItem> lineItems;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
