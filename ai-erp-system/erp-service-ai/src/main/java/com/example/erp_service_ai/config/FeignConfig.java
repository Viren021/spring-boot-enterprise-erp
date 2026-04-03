package com.example.erp_service_ai.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 1. Grab the Keycloak Authorization token and pass it along
            if (request.getHeader("Authorization") != null) {
                template.header("Authorization", request.getHeader("Authorization"));
            }

            // 2. Grab the Tenant ID and pass it along
            if (request.getHeader("tenant_id") != null) {
                template.header("tenant_id", request.getHeader("tenant_id"));
            }
        }
    }
}