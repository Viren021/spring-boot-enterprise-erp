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
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String poNumber;

    @Column(nullable = false)
    private Long vendorId;

    @Column(nullable = false)
    private Long purchaseRequestId;

    private LocalDateTime poDate;
    private LocalDateTime dueDate;
    private LocalDateTime expectedDeliveryDate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "purchase_order_id")
    private List<PurchaseOrderLineItem> lineItems;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal discountAmount;
    private BigDecimal netAmount;

    @Column(nullable = false)
    private String status; // DRAFT, SENT, ACKNOWLEDGED, RECEIVED, COMPLETED, CANCELLED

    private String shippingAddress;
    private String specialInstructions;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
