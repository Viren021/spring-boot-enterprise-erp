package com.example.erp_service_procurement.dto;

import com.example.erp_service_procurement.entity.PurchaseRequest;
import com.example.erp_service_procurement.entity.PurchaseRequestLineItem;
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
public class PurchaseRequestDTO {
    private String description;
    private String departmentId;
    private String createdBy;
    private PurchaseRequest.PriorityLevel priority;
    private LocalDateTime requiredDate;
    private List<PurchaseRequestLineItem> lineItems;
}
