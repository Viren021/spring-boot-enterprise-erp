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
@Table(name = "purchase_request_line_items")
public class PurchaseRequestLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long purchaseRequestId;

    @Column(nullable = false)
    private String itemCode;

    @Column(nullable = false)
    private String itemDescription;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private String uom; // Unit of Measure

    @Column(nullable = false)
    private BigDecimal estimatedUnitPrice;

    private String accountCode;
    private String notes;
}
