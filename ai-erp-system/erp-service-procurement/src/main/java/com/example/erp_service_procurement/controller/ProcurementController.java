package com.example.erp_service_procurement.controller;

import com.example.erp_service_procurement.config.TenantContext;
import com.example.erp_service_procurement.dto.PurchaseOrderDTO;
import com.example.erp_service_procurement.dto.PurchaseRequestDTO;
import com.example.erp_service_procurement.entity.Invoice;
import com.example.erp_service_procurement.entity.PurchaseOrder;
import com.example.erp_service_procurement.entity.PurchaseRequest;
import com.example.erp_service_procurement.entity.Receipt;
import com.example.erp_service_procurement.entity.ProcurementAuditEvent;
import com.example.erp_service_procurement.service.ProcurementService;
import com.example.erp_service_procurement.service.ThreeWayMatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/procurement")
@CrossOrigin(origins = "http://localhost:4200")
public class ProcurementController {

    private final ProcurementService procurementService;
    private final ThreeWayMatchService threeWayMatchService;

    public ProcurementController(ProcurementService procurementService,
                                  ThreeWayMatchService threeWayMatchService) {
        this.procurementService = procurementService;
        this.threeWayMatchService = threeWayMatchService;
    }

    // === Purchase Requests ===

    @PostMapping("/purchase-requests")
    public ResponseEntity<PurchaseRequest> createPurchaseRequest(@RequestBody PurchaseRequestDTO dto) {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(procurementService.createPurchaseRequest(dto, tenantId));
    }

    @GetMapping("/purchase-requests")
    public ResponseEntity<List<PurchaseRequest>> getPurchaseRequests() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(procurementService.getPurchaseRequests(tenantId));
    }

    @GetMapping("/purchase-requests/{id}")
    public ResponseEntity<PurchaseRequest> getPurchaseRequest(@PathVariable Long id) {
        return ResponseEntity.ok(procurementService.getPurchaseRequestById(id, TenantContext.getTenantId()));
    }

    @PutMapping("/purchase-requests/{id}/submit")
    public ResponseEntity<PurchaseRequest> submitPurchaseRequest(@PathVariable Long id) {
        return ResponseEntity.ok(procurementService.submitPurchaseRequest(id, TenantContext.getTenantId()));
    }

    @PutMapping("/purchase-requests/{id}/approve")
    public ResponseEntity<PurchaseRequest> approvePurchaseRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String comments,
            Principal principal) {
        return ResponseEntity.ok(procurementService.approvePurchaseRequest(
                id, comments, TenantContext.getTenantId(), principal == null ? "system" : principal.getName()));
    }

    @PutMapping("/purchase-requests/{id}/reject")
    public ResponseEntity<PurchaseRequest> rejectPurchaseRequest(
            @PathVariable Long id, @RequestParam(required = false) String comments) {
        return ResponseEntity.ok(procurementService.rejectPurchaseRequest(
                id, comments, TenantContext.getTenantId()));
    }

    // === Purchase Orders ===

    @PostMapping("/purchase-orders")
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@RequestBody PurchaseOrderDTO dto) {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(procurementService.createPurchaseOrder(dto, tenantId));
    }

    @GetMapping("/purchase-orders")
    public ResponseEntity<List<PurchaseOrder>> getPurchaseOrders() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(procurementService.getPurchaseOrders(tenantId));
    }

    @PutMapping("/purchase-orders/{id}/approve")
    public ResponseEntity<PurchaseOrder> approvePurchaseOrder(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(procurementService.approvePurchaseOrder(
                id, TenantContext.getTenantId(), principal == null ? "system" : principal.getName()));
    }

    // === Receipts ===

    @PostMapping("/receipts/{poId}")
    public ResponseEntity<Receipt> receiveGoods(
            @PathVariable Long poId,
            @RequestBody Receipt receipt) {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(procurementService.receiveGoods(poId, receipt, tenantId));
    }

    // === Invoices ===

    @PostMapping("/invoices")
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(procurementService.createInvoice(invoice, tenantId));
    }

    @PostMapping("/invoices/{invoiceId}/three-way-match")
    public ResponseEntity<Invoice> performThreeWayMatch(@PathVariable Long invoiceId) {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(threeWayMatchService.performThreeWayMatch(invoiceId, tenantId));
    }

    @GetMapping("/{documentType}/{documentId}/audit-history")
    public ResponseEntity<List<ProcurementAuditEvent>> getAuditHistory(
            @PathVariable String documentType, @PathVariable Long documentId) {
        return ResponseEntity.ok(procurementService.getAuditHistory(
                documentType.toUpperCase(), documentId, TenantContext.getTenantId()));
    }
}
