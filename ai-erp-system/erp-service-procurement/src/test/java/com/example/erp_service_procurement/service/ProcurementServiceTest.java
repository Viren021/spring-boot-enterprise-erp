package com.example.erp_service_procurement.service;

import com.example.erp_service_procurement.dto.PurchaseRequestDTO;
import com.example.erp_service_procurement.entity.PurchaseRequest;
import com.example.erp_service_procurement.kafka.ProcurementProducer;
import com.example.erp_service_procurement.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ProcurementServiceTest {

    @Mock
    private PurchaseRequestRepository prRepository;
    @Mock
    private PurchaseOrderRepository poRepository;
    @Mock
    private ReceiptRepository receiptRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private VendorRepository vendorRepository;
    @Mock
    private ProcurementProducer producer;

    @InjectMocks
    private ProcurementService procurementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreatePurchaseRequest_Success() {
        // Arrange
        PurchaseRequestDTO dto = PurchaseRequestDTO.builder()
                .description("Office Supplies")
                .departmentId("IT")
                .createdBy("john.doe")
                .build();

        PurchaseRequest saved = PurchaseRequest.builder()
                .id(1L)
                .prNumber("PR-tenant1-123")
                .description("Office Supplies")
                .status("DRAFT")
                .tenantId("tenant1")
                .build();

        when(prRepository.save(any(PurchaseRequest.class))).thenReturn(saved);
        doNothing().when(producer).sendPurchaseRequestEvent(anyString(), any(PurchaseRequest.class));

        // Act
        PurchaseRequest result = procurementService.createPurchaseRequest(dto, "tenant1");

        // Assert
        assertNotNull(result);
        assertEquals("DRAFT", result.getStatus());
        assertEquals("Office Supplies", result.getDescription());
        verify(prRepository, times(1)).save(any(PurchaseRequest.class));
        verify(producer, times(1)).sendPurchaseRequestEvent(eq("PR_CREATED"), any());
    }

    @Test
    void testGetPurchaseRequests() {
        // Arrange
        PurchaseRequest pr1 = PurchaseRequest.builder().id(1L).tenantId("t1").build();
        PurchaseRequest pr2 = PurchaseRequest.builder().id(2L).tenantId("t1").build();
        when(prRepository.findByTenantId("t1")).thenReturn(Arrays.asList(pr1, pr2));

        // Act
        List<PurchaseRequest> result = procurementService.getPurchaseRequests("t1");

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void testGetPurchaseRequestById_Found() {
        // Arrange
        PurchaseRequest pr = PurchaseRequest.builder().id(1L).prNumber("PR-001").build();
        when(prRepository.findById(1L)).thenReturn(Optional.of(pr));

        // Act
        PurchaseRequest result = procurementService.getPurchaseRequestById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("PR-001", result.getPrNumber());
    }

    @Test
    void testGetPurchaseRequestById_NotFound() {
        // Arrange
        when(prRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> procurementService.getPurchaseRequestById(999L));
    }

    @Test
    void testApprovePurchaseRequest() {
        // Arrange
        PurchaseRequest pr = PurchaseRequest.builder()
                .id(1L).prNumber("PR-001").status("DRAFT").build();
        when(prRepository.findById(1L)).thenReturn(Optional.of(pr));
        when(prRepository.save(any(PurchaseRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(producer).sendPurchaseRequestEvent(anyString(), any());

        // Act
        PurchaseRequest result = procurementService.approvePurchaseRequest(1L, "Approved by manager");

        // Assert
        assertEquals("APPROVED", result.getStatus());
        assertEquals("Approved by manager", result.getApproverComments());
        verify(producer, times(1)).sendPurchaseRequestEvent(eq("PR_APPROVED"), any());
    }
}
