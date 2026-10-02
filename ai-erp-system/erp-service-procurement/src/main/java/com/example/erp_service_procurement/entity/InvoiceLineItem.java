package com.example.erp_service_procurement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "invoice_line_items")
public class InvoiceLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long invoiceId;
    private Long purchaseOrderLineItemId;

    @Column(nullable = false)
    private String itemCode;

    @Column(nullable = false)
    private Long invoicedQuantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    private BigDecimal lineAmount;
}
