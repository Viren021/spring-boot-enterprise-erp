package com.example.erp_service_master_data.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class TenantFilter implements Filter {
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String tenant = ((HttpServletRequest) request).getHeader("X-Tenant-ID");
        if (tenant != null) TenantContext.set(tenant);
        try { chain.doFilter(request, response); } finally { TenantContext.clear(); }
    }
}
