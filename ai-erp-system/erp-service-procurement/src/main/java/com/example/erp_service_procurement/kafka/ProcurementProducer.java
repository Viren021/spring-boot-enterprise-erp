package com.example.erp_service_procurement.kafka;

import com.example.erp_service_procurement.entity.PurchaseOrder;
import com.example.erp_service_procurement.entity.PurchaseRequest;
import com.example.erp_service_procurement.entity.Receipt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;
import java.math.BigDecimal;
import com.example.erp_service_procurement.service.OutboxEventService;
import com.example.erp_service_procurement.config.TenantContext;

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

    public void sendReceiptEvent(String eventType, Receipt receipt, BigDecimal receiptAmount) {
        log.info("📦 PROCUREMENT: Publishing Receipt Event -> {} | Receipt: {}", eventType, receipt.getReceiptNumber());
        outboxEventService.enqueue("procurement-receipt-events", eventType, "RECEIPT", Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", eventType,
                "receiptNumber", receipt.getReceiptNumber(),
                "poId", receipt.getPurchaseOrderId().toString(),
                "status", receipt.getStatus(),
                "tenantId", receipt.getTenantId()
        ));
        if (receiptAmount == null || receiptAmount.signum() <= 0)
            throw new IllegalStateException("Cannot publish GOODS_RECEIPT without a positive receipt value");
        sendFinanceEvent(eventType.equals("RECEIPT_CREATED") ? "GOODS_RECEIPT" : eventType,
                receipt.getReceiptNumber(), receiptAmount, receipt.getReceiptDate().toLocalDate(),
                "Goods receipt " + receipt.getReceiptNumber(), "DEBIT");
    }

    /** Retained for callers that have not yet supplied the receipt valuation. */
    @Deprecated
    public void sendReceiptEvent(String eventType, Receipt receipt) {
        sendReceiptEvent(eventType, receipt, null);
    }

    public void sendFinanceInvoiceEvent(com.example.erp_service_procurement.entity.Invoice invoice) {
        if (invoice.getInvoiceAmount() == null || invoice.getInvoiceAmount().signum() <= 0)
            throw new IllegalStateException("Cannot publish PROCUREMENT_INVOICE without a positive invoiceAmount");
        sendFinanceEvent("PROCUREMENT_INVOICE", invoice.getInvoiceNumber(), invoice.getInvoiceAmount(),
                invoice.getInvoiceDate().toLocalDate(), "Procurement invoice " + invoice.getInvoiceNumber(), "DEBIT");
    }

    private void sendFinanceEvent(String type, String documentId, BigDecimal amount, LocalDate date,
                                  String description, String direction) {
        if (amount == null || amount.signum() <= 0) throw new IllegalStateException("Finance event amount is unavailable");
        String tenant = TenantContext.getTenantId();
        if (tenant == null || tenant.isBlank()) throw new IllegalStateException("Tenant context is required");
        String eventId = UUID.nameUUIDFromBytes((tenant + ":" + type + ":" + documentId)
                .getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
        outboxEventService.enqueue("finance-integration-events", type, "FINANCE_INTEGRATION", Map.of(
                "eventId", eventId, "tenantId", tenant, "eventType", type,
                "sourceDocumentId", documentId, "amount", amount, "eventDate", date,
                "description", description, "direction", direction));
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
