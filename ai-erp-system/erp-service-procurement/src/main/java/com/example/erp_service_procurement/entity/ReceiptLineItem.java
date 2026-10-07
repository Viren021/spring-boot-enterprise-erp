package com.example.erp_service_procurement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "receipt_line_items")
public class ReceiptLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long receiptId;
    private Long poLineItemId;

    @Column(nullable = false)
    private Long receivedQuantity;

    @Column(precision = 19, scale = 6)
    private java.math.BigDecimal unitCost;

    private String serialNumber;
    private String batchNumber;
    private String qualityStatus; // OK, DEFECTIVE, PARTIAL
    private String notes;
}
