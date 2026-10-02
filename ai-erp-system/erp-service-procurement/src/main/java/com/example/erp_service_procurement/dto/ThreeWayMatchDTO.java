package com.example.erp_service_procurement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreeWayMatchDTO {
    private Long invoiceId;
    private Long purchaseOrderId;
    private Long receiptId;
    private String matchingStatus;
    private String discrepancyNotes;
    private boolean matched;
}
