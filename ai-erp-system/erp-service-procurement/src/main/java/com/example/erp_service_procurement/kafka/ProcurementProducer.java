package com.example.erp_service_procurement.kafka;

import com.example.erp_service_procurement.entity.PurchaseOrder;
import com.example.erp_service_procurement.entity.PurchaseRequest;
import com.example.erp_service_procurement.entity.Receipt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import com.example.erp_service_procurement.service.OutboxEventService;

@Slf4j
@Service
public class ProcurementProducer {

    private final OutboxEventService outboxEventService;

    public ProcurementProducer(OutboxEventService outboxEventService) {
        this.outboxEventService = outboxEventService;
    }

    public void sendPurchaseRequestEvent(String eventType, PurchaseRequest pr) {
        log.info("📦 PROCUREMENT: Publishing PR Event -> {} | PR: {}", eventType, pr.getPrNumber());
        outboxEventService.enqueue("procurement-pr-events", eventType, "PURCHASE_REQUEST", Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", eventType,
                "prNumber", pr.getPrNumber(),
                "status", pr.getStatus(),
                "tenantId", pr.getTenantId()
        ));
    }

    public void sendPurchaseOrderEvent(String eventType, PurchaseOrder po) {
        log.info("📦 PROCUREMENT: Publishing PO Event -> {} | PO: {}", eventType, po.getPoNumber());
        outboxEventService.enqueue("procurement-po-events", eventType, "PURCHASE_ORDER", Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", eventType,
                "poNumber", po.getPoNumber(),
                "vendorId", po.getVendorId().toString(),
                "totalAmount", po.getTotalAmount().toString(),
                "status", po.getStatus(),
                "tenantId", po.getTenantId()
        ));
    }

    public void sendReceiptEvent(String eventType, Receipt receipt) {
        log.info("📦 PROCUREMENT: Publishing Receipt Event -> {} | Receipt: {}", eventType, receipt.getReceiptNumber());
        outboxEventService.enqueue("procurement-receipt-events", eventType, "RECEIPT", Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", eventType,
                "receiptNumber", receipt.getReceiptNumber(),
                "poId", receipt.getPurchaseOrderId().toString(),
                "status", receipt.getStatus(),
                "tenantId", receipt.getTenantId()
        ));
    }

    public void sendInventoryUpdate(Long poId, Long receiptId) {
        log.info("📦 PROCUREMENT: Requesting inventory update for PO: {} Receipt: {}", poId, receiptId);
        outboxEventService.enqueue("inventory-update-events", "INVENTORY_UPDATE_REQUESTED", "RECEIPT", Map.of(
                "eventId", UUID.randomUUID().toString(),
                "source", "procurement",
                "poId", poId.toString(),
                "receiptId", receiptId.toString()
        ));
    }
}
