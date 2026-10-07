package com.example.erp_service_procurement.service;

import com.example.erp_service_procurement.dto.PurchaseOrderDTO;
import com.example.erp_service_procurement.dto.PurchaseRequestDTO;
import com.example.erp_service_procurement.entity.*;
import com.example.erp_service_procurement.kafka.ProcurementProducer;
import com.example.erp_service_procurement.config.TenantContext;
import com.example.erp_service_procurement.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
public class ProcurementService {

    private final PurchaseRequestRepository prRepository;
    private final PurchaseOrderRepository poRepository;
    private final ReceiptRepository receiptRepository;
    private final InvoiceRepository invoiceRepository;
    private final VendorRepository vendorRepository;
    private final ProcurementAuditEventRepository auditRepository;
    private final ProcurementProducer producer;
    private final ApprovalPolicyService approvalPolicies;

    public ProcurementService(PurchaseRequestRepository prRepository,
                              PurchaseOrderRepository poRepository,
                              ReceiptRepository receiptRepository,
                              InvoiceRepository invoiceRepository,
                              VendorRepository vendorRepository,
                              ProcurementProducer producer,
                              ProcurementAuditEventRepository auditRepository,
                              ApprovalPolicyService approvalPolicies) {
        this.prRepository = prRepository;
        this.poRepository = poRepository;
        this.receiptRepository = receiptRepository;
        this.invoiceRepository = invoiceRepository;
        this.vendorRepository = vendorRepository;
        this.producer = producer;
        this.auditRepository = auditRepository;
        this.approvalPolicies = approvalPolicies;
    }

    // === Purchase Requests ===

    public PurchaseRequest createPurchaseRequest(PurchaseRequestDTO dto, String tenantId) {
        log.info("Creating purchase request for tenant: {}", tenantId);

        PurchaseRequest pr = PurchaseRequest.builder()
                .prNumber(generatePRNumber(tenantId))
                .description(dto.getDescription())
                .departmentId(dto.getDepartmentId())
                .createdBy(dto.getCreatedBy())
                .status("DRAFT")
                .priority(dto.getPriority())
                .requiredDate(dto.getRequiredDate())
                .tenantId(tenantId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (dto.getLineItems() != null) {
            pr.setLineItems(dto.getLineItems());
        }

        PurchaseRequest saved = prRepository.save(pr);
        audit("PURCHASE_REQUEST", saved.getId(), "CREATED", saved.getCreatedBy(), tenantId, "Status=DRAFT");
        log.info("Purchase request created: {}", saved.getPrNumber());
        producer.sendPurchaseRequestEvent("PR_CREATED", saved);
        return saved;
    }

    public List<PurchaseRequest> getPurchaseRequests(String tenantId) {
        return prRepository.findByTenantId(tenantId);
    }

    public PurchaseRequest getPurchaseRequestById(Long id, String tenantId) {
        return prRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Purchase Request not found with id: " + id));
    }

    @Deprecated
    public PurchaseRequest getPurchaseRequestById(Long id) {
        return getPurchaseRequestById(id, requireTenantContext());
    }

    public PurchaseRequest submitPurchaseRequest(Long id, String tenantId) {
        PurchaseRequest pr = getPurchaseRequestById(id, tenantId);
        requireStatus(pr.getStatus(), "DRAFT");
        pr.setStatus("PENDING_APPROVAL");
        pr.setUpdatedAt(LocalDateTime.now());
        PurchaseRequest saved = prRepository.save(pr);
        audit("PURCHASE_REQUEST", id, "SUBMITTED", pr.getCreatedBy(), tenantId, "Status=PENDING_APPROVAL");
        producer.sendPurchaseRequestEvent("PR_SUBMITTED", saved);
        return saved;
    }

    public PurchaseRequest approvePurchaseRequest(Long id, String comments, String tenantId) {
        return approvePurchaseRequest(id, comments, tenantId, "system");
    }

    @Deprecated
    public PurchaseRequest approvePurchaseRequest(Long id, String comments) {
        return approvePurchaseRequest(id, comments, requireTenantContext());
    }

    private String requireTenantContext() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("Tenant context is required");
        }
        return tenantId;
    }

    public PurchaseRequest approvePurchaseRequest(Long id, String comments, String tenantId, String actor) {
        PurchaseRequest pr = getPurchaseRequestById(id, tenantId);
        requireStatus(pr.getStatus(), "PENDING_APPROVAL");
        if (actor != null && !"system".equalsIgnoreCase(actor)
                && actor.equalsIgnoreCase(pr.getCreatedBy())) {
            throw new IllegalStateException("The requester cannot approve their own purchase request");
        }
        if (approvalPolicies != null && approvalPolicies.requiresApproval(
                tenantId, "PURCHASE_REQUEST", BigDecimal.ZERO, pr.getDepartmentId(), pr.getCreatedBy())
                && actor != null && actor.equalsIgnoreCase(pr.getCreatedBy())) {
            throw new IllegalStateException("Maker-checker policy prevents self approval");
        }
        pr.setStatus("APPROVED");
        pr.setApproverComments(comments);
        pr.setUpdatedAt(LocalDateTime.now());
        PurchaseRequest saved = prRepository.save(pr);
        audit("PURCHASE_REQUEST", id, "APPROVED", actor, tenantId, comments);
        producer.sendPurchaseRequestEvent("PR_APPROVED", saved);
        return saved;
    }

    public PurchaseRequest rejectPurchaseRequest(Long id, String comments, String tenantId) {
        PurchaseRequest pr = getPurchaseRequestById(id, tenantId);
        requireStatus(pr.getStatus(), "PENDING_APPROVAL");
        pr.setStatus("REJECTED");
        pr.setApproverComments(comments);
        pr.setUpdatedAt(LocalDateTime.now());
        PurchaseRequest saved = prRepository.save(pr);
        audit("PURCHASE_REQUEST", id, "REJECTED", "approver", tenantId, comments);
        producer.sendPurchaseRequestEvent("PR_REJECTED", saved);
        return saved;
    }

    // === Purchase Orders ===

    public PurchaseOrder createPurchaseOrder(PurchaseOrderDTO dto, String tenantId) {
        log.info("Creating purchase order for PR ID: {}", dto.getPurchaseRequestId());

        PurchaseRequest pr = prRepository.findByIdAndTenantId(dto.getPurchaseRequestId(), tenantId)
                .orElseThrow(() -> new RuntimeException("PR not found"));
        requireStatus(pr.getStatus(), "APPROVED");

        Vendor vendor = vendorRepository.findByIdAndTenantId(dto.getVendorId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        if (!"ACTIVE".equals(vendor.getStatus())) {
            throw new IllegalStateException("Only active vendors can receive purchase orders");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getLineItems() != null) {
            for (PurchaseOrderLineItem item : dto.getLineItems()) {
                if (item.getLineAmount() != null) {
                    totalAmount = totalAmount.add(item.getLineAmount());
                }
            }
        }

        BigDecimal tax = dto.getTaxAmount() != null ? dto.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal shipping = dto.getShippingAmount() != null ? dto.getShippingAmount() : BigDecimal.ZERO;
        BigDecimal discount = dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO;

        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(generatePONumber(tenantId))
                .vendorId(dto.getVendorId())
                .purchaseRequestId(dto.getPurchaseRequestId())
                .poDate(LocalDateTime.now())
                .dueDate(dto.getDueDate())
                .expectedDeliveryDate(dto.getExpectedDeliveryDate())
                .lineItems(dto.getLineItems())
                .totalAmount(totalAmount)
                .taxAmount(tax)
                .shippingAmount(shipping)
                .discountAmount(discount)
                .netAmount(totalAmount.add(tax).add(shipping).subtract(discount))
                .status("DRAFT")
                .tenantId(tenantId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PurchaseOrder saved = poRepository.save(po);

        // Update PR status
        pr.setStatus("CONVERTED_TO_PO");
        pr.setUpdatedAt(LocalDateTime.now());
        prRepository.save(pr);

        producer.sendPurchaseOrderEvent("PO_CREATED", saved);
        audit("PURCHASE_ORDER", saved.getId(), "CREATED", "buyer", tenantId, "Status=DRAFT");
        log.info("Purchase order created: {}", saved.getPoNumber());
        return saved;
    }

    public List<PurchaseOrder> getPurchaseOrders(String tenantId) {
        return poRepository.findByTenantId(tenantId);
    }

    // === Goods Receipt ===

    public Receipt receiveGoods(Long poId, Receipt receipt, String tenantId) {
        log.info("Receiving goods for PO ID: {}", poId);

        PurchaseOrder po = poRepository.findByIdAndTenantId(poId, tenantId)
                .orElseThrow(() -> new RuntimeException("PO not found"));
        if (!List.of("APPROVED", "SENT", "ACKNOWLEDGED", "PARTIALLY_RECEIVED").contains(po.getStatus())) {
            throw new IllegalStateException("Goods can only be received for an approved or acknowledged PO");
        }
        if (receipt.getLineItems() == null || receipt.getLineItems().isEmpty()) {
            throw new IllegalArgumentException("At least one receipt line is required");
        }

        receipt.setReceiptNumber(generateReceiptNumber(tenantId));
        receipt.setPurchaseOrderId(poId);
        receipt.setReceiptDate(LocalDateTime.now());
        receipt.setStatus("INSPECTED".equals(receipt.getQualityInspection()) ? "ACCEPTED" : "RECEIVED");
        if (po.getLineItems() != null) {
            for (ReceiptLineItem receivedLine : receipt.getLineItems()) {
                po.getLineItems().stream()
                        .filter(poLine -> Objects.equals(poLine.getId(), receivedLine.getPoLineItemId()))
                        .findFirst()
                        .ifPresent(poLine -> {
                            poLine.setReceivedQuantity(
                                    (poLine.getReceivedQuantity() == null ? 0 : poLine.getReceivedQuantity())
                                            + receivedLine.getReceivedQuantity());
                            if (receivedLine.getUnitCost() == null || receivedLine.getUnitCost().signum() <= 0) {
                                receivedLine.setUnitCost(poLine.getUnitPrice());
                            }
                        });
            }
        }
        receipt.setTenantId(tenantId);
        receipt.setCreatedAt(LocalDateTime.now());

        Receipt saved = receiptRepository.save(receipt);

        // Update PO status
        boolean fullyReceived = po.getLineItems() != null && po.getLineItems().stream()
                .allMatch(item -> item.getReceivedQuantity() != null
                        && item.getOrderedQuantity() != null
                        && item.getReceivedQuantity() >= item.getOrderedQuantity());
        po.setStatus(fullyReceived ? "FULLY_RECEIVED" : "PARTIALLY_RECEIVED");
        po.setUpdatedAt(LocalDateTime.now());
        poRepository.save(po);

        BigDecimal receivedValue = saved.getLineItems().stream()
                .map(line -> line.getUnitCost().multiply(BigDecimal.valueOf(line.getReceivedQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (receivedValue.signum() <= 0)
            throw new IllegalStateException("Cannot account for receipt: actual receipt costs are unavailable");
        producer.sendReceiptEvent("RECEIPT_CREATED", saved, receivedValue);
        producer.sendInventoryUpdate(po.getId(), saved.getId());
        audit("RECEIPT", saved.getId(), "CREATED", receipt.getReceivedBy(), tenantId,
                "PO=" + poId + ", Status=" + saved.getStatus());

        log.info("Receipt created: {}", saved.getReceiptNumber());
        return saved;
    }

    // === Invoices ===

    public Invoice createInvoice(Invoice invoice, String tenantId) {
        if (invoiceRepository.findByInvoiceNumberAndTenantId(invoice.getInvoiceNumber(), tenantId).isPresent()) {
            throw new IllegalArgumentException("Duplicate invoice number for this tenant");
        }
        PurchaseOrder po = poRepository.findByIdAndTenantId(invoice.getPurchaseOrderId(), tenantId)
                .orElseThrow(() -> new RuntimeException("PO not found"));
        if (!List.of("PARTIALLY_RECEIVED", "FULLY_RECEIVED", "RECEIVED").contains(po.getStatus())) {
            throw new IllegalStateException("An invoice cannot be recorded before goods receipt");
        }
        invoice.setTenantId(tenantId);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        if (invoice.getStatus() == null) {
            invoice.setStatus("RECEIVED");
        }
        Invoice saved = invoiceRepository.save(invoice);
        audit("INVOICE", saved.getId(), "RECEIVED", "accounts-payable", tenantId, "Status=RECEIVED");
        producer.sendFinanceInvoiceEvent(saved);
        return saved;
    }

    public List<ProcurementAuditEvent> getAuditHistory(String documentType, Long documentId, String tenantId) {
        return auditRepository.findByTenantIdAndDocumentTypeAndDocumentIdOrderByOccurredAtAsc(
                tenantId, documentType, documentId);
    }

    public PurchaseOrder approvePurchaseOrder(Long id, String tenantId) {
        return approvePurchaseOrder(id, tenantId, "system");
    }

    public PurchaseOrder approvePurchaseOrder(Long id, String tenantId, String actor) {
        PurchaseOrder po = poRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));
        requireStatus(po.getStatus(), "DRAFT");
        PurchaseRequest request = prRepository.findByIdAndTenantId(po.getPurchaseRequestId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Purchase Request not found"));
        if (actor != null && !"system".equalsIgnoreCase(actor)
                && actor.equalsIgnoreCase(request.getCreatedBy())) {
            throw new IllegalStateException("The requester cannot approve the purchase order");
        }
        if (approvalPolicies != null && approvalPolicies.requiresApproval(
                tenantId, "PURCHASE_ORDER", po.getNetAmount() == null ? BigDecimal.ZERO : po.getNetAmount(),
                request.getDepartmentId(), request.getCreatedBy())
                && actor != null && actor.equalsIgnoreCase(request.getCreatedBy())) {
            throw new IllegalStateException("Maker-checker policy prevents self approval");
        }
        po.setStatus("APPROVED");
        po.setUpdatedAt(LocalDateTime.now());
        PurchaseOrder saved = poRepository.save(po);
        audit("PURCHASE_ORDER", id, "APPROVED", actor, tenantId, "Status=APPROVED");
        producer.sendPurchaseOrderEvent("PO_APPROVED", saved);
        return saved;
    }

    private void requireStatus(String actual, String expected) {
        if (!Objects.equals(actual, expected)) {
            throw new IllegalStateException("Invalid status transition: expected " + expected + " but was " + actual);
        }
    }

    private void audit(String documentType, Long documentId, String action, String actor,
                       String tenantId, String details) {
        ProcurementAuditEvent event = new ProcurementAuditEvent();
        event.setDocumentType(documentType);
        event.setDocumentId(documentId);
        event.setAction(action);
        event.setActor(actor);
        event.setTenantId(tenantId);
        event.setDetails(details);
        event.setOccurredAt(LocalDateTime.now());
        auditRepository.save(event);
    }

    // === Helpers ===

    private String generatePRNumber(String tenantId) {
        return "PR-" + tenantId + "-" + System.currentTimeMillis();
    }

    private String generatePONumber(String tenantId) {
        return "PO-" + tenantId + "-" + System.currentTimeMillis();
    }

    private String generateReceiptNumber(String tenantId) {
        return "REC-" + tenantId + "-" + System.currentTimeMillis();
    }
}
