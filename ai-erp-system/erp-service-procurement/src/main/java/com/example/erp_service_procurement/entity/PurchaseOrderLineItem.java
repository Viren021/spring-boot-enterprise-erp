package com.example.erp_service_procurement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "purchase_order_line_items")
public class PurchaseOrderLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long purchaseOrderId;

    @Column(nullable = false)
    private String itemCode;

    @Column(nullable = false)
    private String itemDescription;

    @Column(nullable = false)
    private Long orderedQuantity;

    private Long receivedQuantity;
    private Long invoicedQuantity;

    @Column(nullable = false)
    private String uom;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    private BigDecimal lineAmount;
    private String status; // PENDING, PARTIAL, COMPLETE
}
