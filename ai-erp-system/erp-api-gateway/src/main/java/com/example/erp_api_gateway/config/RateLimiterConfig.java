package com.example.erp_api_gateway.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {
    // Rate limiting is configured via application.yml resilience4j properties
    // This class can be extended for custom rate limiting logic
}
