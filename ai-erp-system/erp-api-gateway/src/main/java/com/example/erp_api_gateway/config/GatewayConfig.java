package com.example.erp_api_gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

@Configuration
public class GatewayConfig {

    @Bean
    public GlobalFilter authHeaderRelayFilter() {
        return (exchange, chain) -> ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .flatMap(jwtAuth -> {
                    Jwt jwt = jwtAuth.getToken();
                    Object tenantClaim = jwt.getClaims().get("tenant_id");
                    String tenantId = tenantClaim == null ? null : tenantClaim.toString();
                    if (tenantId == null || !tenantId.matches("[A-Za-z0-9_]{1,63}")) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                    ServerHttpRequest request = exchange.getRequest().mutate()
                            .headers(headers -> headers.remove("X-Tenant-ID"))
                            .header("X-Tenant-ID", tenantId)
                            .header("X-User-ID", jwt.getSubject())
                            .header("X-User-Roles", String.join(",",
                                    jwtAuth.getAuthorities().stream()
                                            .map(Object::toString)
                                            .toArray(String[]::new)))
                            .build();
                    return chain.filter(exchange.mutate().request(request).build());
                })
                .switchIfEmpty(chain.filter(exchange));
    }

}
