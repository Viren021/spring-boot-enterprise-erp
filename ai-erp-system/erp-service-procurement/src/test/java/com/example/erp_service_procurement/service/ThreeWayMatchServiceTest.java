package com.example.erp_service_procurement.service;

import com.example.erp_service_procurement.entity.Invoice;
import com.example.erp_service_procurement.entity.PurchaseOrder;
import com.example.erp_service_procurement.entity.Receipt;
import com.example.erp_service_procurement.repository.InvoiceRepository;
import com.example.erp_service_procurement.repository.PurchaseOrderRepository;
import com.example.erp_service_procurement.repository.ReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ThreeWayMatchServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PurchaseOrderRepository poRepository;
    @Mock
    private ReceiptRepository receiptRepository;

    @InjectMocks
    private ThreeWayMatchService threeWayMatchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testThreeWayMatch_Pass() {
        // Arrange
        Invoice invoice = Invoice.builder()
                .id(1L).invoiceNumber("INV-001")
                .purchaseOrderId(1L).receiptId(1L)
                .netAmount(new BigDecimal("1000.00"))
                .build();

        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L).poNumber("PO-001")
                .netAmount(new BigDecimal("1000.00"))
                .lineItems(Collections.emptyList())
                .build();

        Receipt receipt = Receipt.builder()
                .id(1L).receiptNumber("REC-001")
                .qualityInspection("OK")
                .lineItems(Collections.emptyList())
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(poRepository.findById(1L)).thenReturn(Optional.of(po));
        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Invoice result = threeWayMatchService.performThreeWayMatch(1L, "tenant1");

        // Assert
        assertEquals("THREE_WAY_PASS", result.getMatchingStatus());
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void testThreeWayMatch_FailAmountMismatch() {
        // Arrange
        Invoice invoice = Invoice.builder()
                .id(1L).invoiceNumber("INV-002")
                .purchaseOrderId(1L).receiptId(1L)
                .netAmount(new BigDecimal("2000.00")) // Double the PO amount!
                .build();

        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L).poNumber("PO-001")
                .netAmount(new BigDecimal("1000.00"))
                .lineItems(Collections.emptyList())
                .build();

        Receipt receipt = Receipt.builder()
                .id(1L).receiptNumber("REC-001")
                .qualityInspection("OK")
                .lineItems(Collections.emptyList())
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(poRepository.findById(1L)).thenReturn(Optional.of(po));
        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Invoice result = threeWayMatchService.performThreeWayMatch(1L, "tenant1");

        // Assert
        assertEquals("THREE_WAY_FAIL", result.getMatchingStatus());
        assertEquals("REJECTED", result.getStatus());
        assertTrue(result.getDiscrepancyNotes().contains("Amount mismatch"));
    }

    @Test
    void testThreeWayMatch_FailQualityInspection() {
        // Arrange
        Invoice invoice = Invoice.builder()
                .id(1L).invoiceNumber("INV-003")
                .purchaseOrderId(1L).receiptId(1L)
                .netAmount(new BigDecimal("1000.00"))
                .build();

        PurchaseOrder po = PurchaseOrder.builder()
                .id(1L).netAmount(new BigDecimal("1000.00"))
                .lineItems(Collections.emptyList())
                .build();

        Receipt receipt = Receipt.builder()
                .id(1L).qualityInspection("DEFECTIVE") // Failed quality!
                .lineItems(Collections.emptyList())
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(poRepository.findById(1L)).thenReturn(Optional.of(po));
        when(receiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Invoice result = threeWayMatchService.performThreeWayMatch(1L, "tenant1");

        // Assert
        assertEquals("THREE_WAY_FAIL", result.getMatchingStatus());
        assertTrue(result.getDiscrepancyNotes().contains("Quality inspection failed"));
    }

    @Test
    void testThreeWayMatch_InvoiceNotFound() {
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> threeWayMatchService.performThreeWayMatch(999L, "tenant1"));
    }
}
