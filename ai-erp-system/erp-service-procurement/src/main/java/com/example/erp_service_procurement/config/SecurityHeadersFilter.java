package com.example.erp_service_procurement.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OWASP Security Headers Filter.
 * Adds security headers to all HTTP responses.
 */
@Component
@Order(1)
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Prevent Clickjacking
        httpResponse.setHeader("X-Frame-Options", "DENY");

        // Prevent MIME sniffing
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        // Enable XSS Protection
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // Content Security Policy
        httpResponse.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self' data:;");

        // Referrer Policy
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Permissions Policy
        httpResponse.setHeader("Permissions-Policy",
                "geolocation=(), microphone=(), camera=()");

        // Strict Transport Security (for HTTPS)
        httpResponse.setHeader("Strict-Transport-Security",
                "max-age=31536000; includeSubDomains");

        chain.doFilter(request, response);
    }
}
