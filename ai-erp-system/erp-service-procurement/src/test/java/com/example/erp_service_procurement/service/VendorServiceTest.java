package com.example.erp_service_procurement.service;

import com.example.erp_service_procurement.dto.VendorDTO;
import com.example.erp_service_procurement.entity.Vendor;
import com.example.erp_service_procurement.repository.VendorRepository;
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
import static org.mockito.Mockito.*;

class VendorServiceTest {

    @Mock
    private VendorRepository vendorRepository;

    @InjectMocks
    private VendorService vendorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateVendor_Success() {
        // Arrange
        VendorDTO dto = VendorDTO.builder()
                .vendorCode("ACME-001")
                .vendorName("ACME Corp")
                .contactPerson("John Doe")
                .email("john@acme.com")
                .phone("555-0100")
                .address("123 Main St")
                .paymentTerms("NET30")
                .build();

        Vendor expectedVendor = Vendor.builder()
                .id(1L)
                .vendorCode("ACME-001")
                .vendorName("ACME Corp")
                .status("ACTIVE")
                .tenantId("tenant1")
                .build();

        when(vendorRepository.save(any(Vendor.class))).thenReturn(expectedVendor);

        // Act
        Vendor result = vendorService.createVendor(dto, "tenant1");

        // Assert
        assertNotNull(result);
        assertEquals("ACME Corp", result.getVendorName());
        assertEquals("ACTIVE", result.getStatus());
        verify(vendorRepository, times(1)).save(any(Vendor.class));
    }

    @Test
    void testGetAllVendors() {
        // Arrange
        Vendor v1 = Vendor.builder().id(1L).vendorName("ACME").tenantId("t1").build();
        Vendor v2 = Vendor.builder().id(2L).vendorName("Globex").tenantId("t1").build();
        when(vendorRepository.findByTenantId("t1")).thenReturn(Arrays.asList(v1, v2));

        // Act
        List<Vendor> result = vendorService.getAllVendors("t1");

        // Assert
        assertEquals(2, result.size());
        verify(vendorRepository, times(1)).findByTenantId("t1");
    }

    @Test
    void testGetVendorById_Found() {
        // Arrange
        Vendor vendor = Vendor.builder().id(1L).vendorName("ACME").build();
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(vendor));

        // Act
        Vendor result = vendorService.getVendorById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("ACME", result.getVendorName());
    }

    @Test
    void testGetVendorById_NotFound() {
        // Arrange
        when(vendorRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> vendorService.getVendorById(999L));
    }

    @Test
    void testUpdateVendor() {
        // Arrange
        Vendor existing = Vendor.builder()
                .id(1L).vendorName("Old Name").email("old@test.com").build();
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(inv -> inv.getArgument(0));

        VendorDTO dto = VendorDTO.builder().vendorName("New Name").email("new@test.com").build();

        // Act
        Vendor result = vendorService.updateVendor(1L, dto);

        // Assert
        assertEquals("New Name", result.getVendorName());
        assertEquals("new@test.com", result.getEmail());
    }

    @Test
    void testGetActiveVendors() {
        // Arrange
        Vendor active = Vendor.builder().id(1L).status("ACTIVE").tenantId("t1").build();
        when(vendorRepository.findByStatusAndTenantId("ACTIVE", "t1"))
                .thenReturn(List.of(active));

        // Act
        List<Vendor> result = vendorService.getActiveVendors("t1");

        // Assert
        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }
}
