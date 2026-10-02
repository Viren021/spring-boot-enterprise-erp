package com.example.erp_service_procurement.service;

import com.example.erp_service_procurement.dto.VendorDTO;
import com.example.erp_service_procurement.entity.Vendor;
import com.example.erp_service_procurement.repository.VendorRepository;
import com.example.erp_service_procurement.config.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class VendorService {

    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public Vendor createVendor(VendorDTO dto, String tenantId) {
        log.info("Creating vendor: {} for tenant: {}", dto.getVendorName(), tenantId);

        Vendor vendor = Vendor.builder()
                .vendorCode(dto.getVendorCode())
                .vendorName(dto.getVendorName())
                .contactPerson(dto.getContactPerson())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .postalCode(dto.getPostalCode())
                .country(dto.getCountry())
                .paymentTerms(dto.getPaymentTerms())
                .performanceRating(0.0)
                .deliveryPerformanceScore(0)
                .qualityScore(0)
                .responseScore(0)
                .status(dto.getStatus() != null ? dto.getStatus() : "ACTIVE")
                .tenantId(tenantId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return vendorRepository.save(vendor);
    }

    public List<Vendor> getAllVendors(String tenantId) {
        return vendorRepository.findByTenantId(tenantId);
    }

    public List<Vendor> getActiveVendors(String tenantId) {
        return vendorRepository.findByStatusAndTenantId("ACTIVE", tenantId);
    }

    public Vendor getVendorById(Long id, String tenantId) {
        return vendorRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + id));
    }

    @Deprecated
    public Vendor getVendorById(Long id) {
        return getVendorById(id, requireTenantContext());
    }

    public Vendor updateVendor(Long id, VendorDTO dto, String tenantId) {
        Vendor vendor = getVendorById(id, tenantId);
        if (dto.getVendorName() != null) vendor.setVendorName(dto.getVendorName());
        if (dto.getContactPerson() != null) vendor.setContactPerson(dto.getContactPerson());
        if (dto.getEmail() != null) vendor.setEmail(dto.getEmail());
        if (dto.getPhone() != null) vendor.setPhone(dto.getPhone());
        if (dto.getAddress() != null) vendor.setAddress(dto.getAddress());
        if (dto.getCity() != null) vendor.setCity(dto.getCity());
        if (dto.getState() != null) vendor.setState(dto.getState());
        if (dto.getPostalCode() != null) vendor.setPostalCode(dto.getPostalCode());
        if (dto.getCountry() != null) vendor.setCountry(dto.getCountry());
        if (dto.getPaymentTerms() != null) vendor.setPaymentTerms(dto.getPaymentTerms());
        if (dto.getStatus() != null) vendor.setStatus(dto.getStatus());
        vendor.setUpdatedAt(LocalDateTime.now());
        return vendorRepository.save(vendor);
    }

    @Deprecated
    public Vendor updateVendor(Long id, VendorDTO dto) {
        return updateVendor(id, dto, requireTenantContext());
    }

    private String requireTenantContext() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("Tenant context is required");
        }
        return tenantId;
    }

    public Vendor updatePerformanceRating(Long id, Double rating, Integer deliveryScore,
                                           Integer qualityScore, Integer responseScore) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + id));
        vendor.setPerformanceRating(rating);
        vendor.setDeliveryPerformanceScore(deliveryScore);
        vendor.setQualityScore(qualityScore);
        vendor.setResponseScore(responseScore);
        vendor.setUpdatedAt(LocalDateTime.now());
        return vendorRepository.save(vendor);
    }
}
