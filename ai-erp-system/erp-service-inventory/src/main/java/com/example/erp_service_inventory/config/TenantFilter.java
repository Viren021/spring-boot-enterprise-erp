package com.example.erp_service_inventory.config;

// 1. Notice the HR import is gone!
import jakarta.servlet.Filter; // 2. We added this so it knows what "Filter" is!
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TenantFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        // Look for a header called "X-Tenant-ID"
        String tenantId = req.getHeader("X-Tenant-ID");

        if (tenantId != null) {
            // Put it in the backpack so Hibernate can find it later
            // (It automatically uses the TenantContext in this exact same package)
            TenantContext.setTenantId(tenantId);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // ALWAYS clear the backpack when the request is done!
            TenantContext.clear();
        }
    }
}