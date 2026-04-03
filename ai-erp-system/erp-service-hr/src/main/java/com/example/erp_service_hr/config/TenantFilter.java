package com.example.erp_service_hr.config;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TenantFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        // 1. Look for a header called "X-Tenant-ID"
        String tenantId = req.getHeader("X-Tenant-ID");

        if (tenantId != null) {
            // 2. Put it in the backpack so Hibernate can find it later
            TenantContext.setTenantId(tenantId);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // 3. ALWAYS clear the backpack when the request is done!
            // If we don't, the next user might accidentally get the previous user's data.
            TenantContext.clear();
        }
    }
}
