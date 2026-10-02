package com.example.erp_service_inventory.service;

import com.example.erp_service_inventory.entity.Product;
import com.example.erp_service_inventory.repository.ProductRepository;
import com.example.erp_service_inventory.config.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // This is the magic! It checks Redis for a key like "productStock::101"
    @Cacheable(value = "productStock", key = "#productId")
    public Integer getStockLevel(Long productId) {
        System.out.println("⏳ REDIS MISS! Querying PostgreSQL database for Product: " + productId);

        String tenant = TenantContext.getTenantId();
        if (tenant == null || tenant.isBlank()) {
            throw new IllegalStateException("X-Tenant-ID is required");
        }
        Product product = productRepository.findByIdAndTenantId(productId, tenant)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return product.getStockQuantity();
    }
}