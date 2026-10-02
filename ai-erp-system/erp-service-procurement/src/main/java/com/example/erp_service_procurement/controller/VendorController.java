package com.example.erp_service_procurement.controller;

import com.example.erp_service_procurement.config.TenantContext;
import com.example.erp_service_procurement.dto.VendorDTO;
import com.example.erp_service_procurement.entity.Vendor;
import com.example.erp_service_procurement.service.VendorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procurement/vendors")
@CrossOrigin(origins = "http://localhost:4200")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @PostMapping
    public ResponseEntity<Vendor> createVendor(@RequestBody VendorDTO dto) {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(vendorService.createVendor(dto, tenantId));
    }

    @GetMapping
    public ResponseEntity<List<Vendor>> getAllVendors() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(vendorService.getAllVendors(tenantId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Vendor>> getActiveVendors() {
        String tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(vendorService.getActiveVendors(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vendor> getVendor(@PathVariable Long id) {
        return ResponseEntity.ok(vendorService.getVendorById(id, TenantContext.getTenantId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vendor> updateVendor(@PathVariable Long id, @RequestBody VendorDTO dto) {
        return ResponseEntity.ok(vendorService.updateVendor(id, dto, TenantContext.getTenantId()));
    }

    @PutMapping("/{id}/performance")
    public ResponseEntity<Vendor> updatePerformance(
            @PathVariable Long id,
            @RequestParam Double rating,
            @RequestParam Integer deliveryScore,
            @RequestParam Integer qualityScore,
            @RequestParam Integer responseScore) {
        return ResponseEntity.ok(vendorService.updatePerformanceRating(id, rating, deliveryScore, qualityScore, responseScore));
    }
}
