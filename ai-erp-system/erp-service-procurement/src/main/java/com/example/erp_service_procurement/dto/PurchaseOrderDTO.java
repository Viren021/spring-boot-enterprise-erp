package com.example.erp_service_procurement.dto;

import com.example.erp_service_procurement.entity.PurchaseOrderLineItem;
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
public class PurchaseOrderDTO {
    private Long vendorId;
    private Long purchaseRequestId;
    private LocalDateTime dueDate;
    private LocalDateTime expectedDeliveryDate;
    private List<PurchaseOrderLineItem> lineItems;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal discountAmount;
}
