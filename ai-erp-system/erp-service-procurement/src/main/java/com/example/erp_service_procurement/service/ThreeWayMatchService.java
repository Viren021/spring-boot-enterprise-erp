package com.example.erp_service_procurement.service;

import com.example.erp_service_procurement.entity.Invoice;
import com.example.erp_service_procurement.entity.InvoiceLineItem;
import com.example.erp_service_procurement.entity.PurchaseOrder;
import com.example.erp_service_procurement.entity.PurchaseOrderLineItem;
import com.example.erp_service_procurement.entity.Receipt;
import com.example.erp_service_procurement.repository.InvoiceRepository;
import com.example.erp_service_procurement.repository.PurchaseOrderRepository;
import com.example.erp_service_procurement.repository.ReceiptRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
public class ThreeWayMatchService {

    private final InvoiceRepository invoiceRepository;
    private final PurchaseOrderRepository poRepository;
    private final ReceiptRepository receiptRepository;

    public ThreeWayMatchService(InvoiceRepository invoiceRepository,
                                PurchaseOrderRepository poRepository,
                                ReceiptRepository receiptRepository) {
        this.invoiceRepository = invoiceRepository;
        this.poRepository = poRepository;
        this.receiptRepository = receiptRepository;
    }

    public Invoice performThreeWayMatch(Long invoiceId, String tenantId) {
        log.info("Performing three-way match for invoice ID: {}", invoiceId);

        Invoice invoice = invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        PurchaseOrder po = poRepository.findByIdAndTenantId(invoice.getPurchaseOrderId(), tenantId)
                .orElseThrow(() -> new RuntimeException("PO not found"));

        Receipt receipt = receiptRepository.findByIdAndTenantId(invoice.getReceiptId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        boolean match = true;
        StringBuilder discrepancies = new StringBuilder();

        // Check 1: PO Amount vs Invoice Amount (allow 2% variance)
        BigDecimal poAmount = po.getNetAmount();
        BigDecimal invoiceAmount = invoice.getNetAmount();
        if (poAmount != null && invoiceAmount != null) {
            BigDecimal variance = poAmount.multiply(BigDecimal.valueOf(0.02));
            if (invoiceAmount.compareTo(poAmount.subtract(variance)) < 0 ||
                invoiceAmount.compareTo(poAmount.add(variance)) > 0) {
                match = false;
                discrepancies.append("Amount mismatch: PO=").append(poAmount)
                        .append(", Invoice=").append(invoiceAmount).append("; ");
            }
        }

        // Check 2: line-level quantity and price matching.
        if (invoice.getLineItems() == null || invoice.getLineItems().isEmpty()) {
            match = false;
            discrepancies.append("Invoice has no line items; ");
        } else {
            for (InvoiceLineItem invoiceLine : invoice.getLineItems()) {
                PurchaseOrderLineItem poLine = po.getLineItems().stream()
                        .filter(line -> line.getId().equals(invoiceLine.getPurchaseOrderLineItemId()))
                        .findFirst()
                        .orElse(null);
                if (poLine == null) {
                    match = false;
                    discrepancies.append("Invoice line has no matching PO line: ")
                            .append(invoiceLine.getItemCode()).append("; ");
                    continue;
                }
                if (!poLine.getItemCode().equals(invoiceLine.getItemCode())
                        || invoiceLine.getInvoicedQuantity() > poLine.getOrderedQuantity()) {
                    match = false;
                    discrepancies.append("Quantity/item mismatch for line: ")
                            .append(invoiceLine.getItemCode()).append("; ");
                }
                if (invoiceLine.getUnitPrice() == null
                        || poLine.getUnitPrice().compareTo(invoiceLine.getUnitPrice()) != 0) {
                    match = false;
                    discrepancies.append("Unit price mismatch for line: ")
                            .append(invoiceLine.getItemCode()).append("; ");
                }
            }
        }

        // Check 3: Receipt Quality Status
        if ("DEFECTIVE".equals(receipt.getQualityInspection())) {
            match = false;
            discrepancies.append("Quality inspection failed; ");
        }

        if (match) {
            invoice.setMatchingStatus("THREE_WAY_PASS");
            invoice.setStatus("APPROVED_FOR_PAYMENT");
            log.info("Three-way match PASSED for invoice: {}", invoice.getInvoiceNumber());
        } else {
            invoice.setMatchingStatus("THREE_WAY_FAIL");
            invoice.setStatus("EXCEPTION");
            invoice.setDiscrepancyNotes(discrepancies.toString());
            log.warn("Three-way match FAILED for invoice: {} - {}",
                    invoice.getInvoiceNumber(), discrepancies);
        }

        invoice.setUpdatedAt(LocalDateTime.now());
        return invoiceRepository.save(invoice);
    }
}
