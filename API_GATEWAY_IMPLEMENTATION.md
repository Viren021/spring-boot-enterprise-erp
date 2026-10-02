# API Gateway Implementation Guide

## Overview
The API Gateway will serve as a single entry point for all microservices, handling routing, load balancing, rate limiting, and cross-cutting concerns.

## Project Structure
```
erp-api-gateway/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/gateway/
│   │   │       ├── GatewayApplication.java
│   │   │       ├── config/
│   │   │       │   ├── GatewayConfig.java
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   └── CircuitBreakerConfig.java
│   │   │       ├── filter/
│   │   │       │   ├── RequestTrackingFilter.java
│   │   │       │   └── TenantHeaderFilter.java
│   │   │       └── controller/
│   │   │           └── HealthController.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/
└── Dockerfile
```

## Files to Create

### 1. pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.3</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>erp-api-gateway</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>erp-api-gateway</name>
    <description>API Gateway for ERP System</description>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>

    <dependencies>
        <!-- Spring Cloud Gateway -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>

        <!-- Service Discovery -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!-- Circuit Breaker -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
        </dependency>

        <!-- Rate Limiting -->
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-ratelimiter</artifactId>
        </dependency>

        <!-- Distributed Tracing -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-sleuth</artifactId>
        </dependency>

        <!-- Keycloak Integration -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>

        <!-- Metrics -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <!-- WebFlux (required by Gateway) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <!-- Logging -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

### 2. GatewayApplication.java
```java
package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
```

### 3. application.yml
```yaml
spring:
  application:
    name: erp-api-gateway
  cloud:
    gateway:
      routes:
        # HR Service Route
        - id: hr-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/v1/hr/**
          filters:
            - StripPrefix=2
            - RequestRateLimiter=10,20
            - CircuitBreaker=hr-service-breaker

        # Finance Service Route
        - id: finance-service
          uri: http://localhost:8084
          predicates:
            - Path=/api/v1/finance/**
          filters:
            - StripPrefix=2
            - RequestRateLimiter=10,20
            - CircuitBreaker=finance-service-breaker

        # Inventory Service Route
        - id: inventory-service
          uri: http://localhost:8083
          predicates:
            - Path=/api/v1/inventory/**
          filters:
            - StripPrefix=2
            - RequestRateLimiter=10,20
            - CircuitBreaker=inventory-service-breaker

        # Compliance Service Route
        - id: compliance-service
          uri: http://localhost:8086
          predicates:
            - Path=/api/v1/compliance/**
          filters:
            - StripPrefix=2
            - RequestRateLimiter=10,20
            - CircuitBreaker=compliance-service-breaker

        # AI Service Route
        - id: ai-service
          uri: http://localhost:8085
          predicates:
            - Path=/api/v1/analytics/**,/api/v1/ai/**
          filters:
            - StripPrefix=2
            - RequestRateLimiter=5,10
            - CircuitBreaker=ai-service-breaker

      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "http://localhost:4200"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/realms/erp-realm

resilience4j:
  circuitbreaker:
    instances:
      hr-service-breaker:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
      finance-service-breaker:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
      inventory-service-breaker:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
      compliance-service-breaker:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
      ai-service-breaker:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10000

  ratelimiter:
    instances:
      hr-service:
        limitRefreshPeriod: 1m
        limitForPeriod: 100
      finance-service:
        limitRefreshPeriod: 1m
        limitForPeriod: 100
      inventory-service:
        limitRefreshPeriod: 1m
        limitForPeriod: 100
      compliance-service:
        limitRefreshPeriod: 1m
        limitForPeriod: 100
      ai-service:
        limitRefreshPeriod: 1m
        limitForPeriod: 50

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: always

logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    com.example.gateway: DEBUG
```

### 4. SecurityConfig.java
```java
package com.example.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/health").permitAll()
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt()
                .and()
                .and()
                .build();
    }

}
```

### 5. GatewayConfig.java
```java
package com.example.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    public GlobalFilter customGlobalFilter() {
        return (exchange, chain) -> {
            // Extract user information and add to headers
            return ReactiveSecurityContextHolder.getContext()
                    .map(securityContext -> securityContext.getAuthentication())
                    .doOnNext(auth -> {
                        exchange.getRequest().mutate()
                                .header("X-User-ID", auth.getName())
                                .header("X-User-Roles", String.join(",", auth.getAuthorities().stream()
                                        .map(Object::toString)
                                        .toArray(String[]::new)))
                                .build();
                    })
                    .then(chain.filter(exchange))
                    .switchIfEmpty(chain.filter(exchange));
        };
    }

}
```

### 6. CircuitBreakerConfig.java
```java
package com.example.gateway.config;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfiguration {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JCircuitBreakerFactory.Resilience4jCircuitBreakerCustomizer() {
            @Override
            public void customize(CircuitBreaker circuitBreaker) {
                // Customize as needed
            }
        });
    }

}
```

### 7. HealthController.java
```java
package com.example.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "ERP API Gateway");
        return ResponseEntity.ok(response);
    }

}
```

### 8. Dockerfile
```dockerfile
FROM maven:3.9.0-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/erp-api-gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Integration with docker-compose.yml

Add this to your existing docker-compose.yml:

```yaml
  api-gateway:
    build: ../erp-api-gateway
    container_name: erp-api-gateway
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://keycloak:8080/realms/erp-realm
    depends_on:
      - keycloak
      - postgres
    networks:
      - erp-network
```

## Updated Frontend API Calls

Update your `api.service.ts` to use the gateway:

```typescript
const API_BASE_URL = 'http://localhost:8080/api/v1';

async getCeoDashboard() {
  const headers = await this.getStandardHeaders();
  const request = this.http.get(`${API_BASE_URL}/analytics/dashboard`, { headers });
  return await lastValueFrom(request);
}

async getEmployees() {
  const headers = await this.getStandardHeaders();
  const request = this.http.get(`${API_BASE_URL}/hr/employees`, { headers });
  return await lastValueFrom(request);
}
```

## Benefits

1. **Single Entry Point** - All traffic goes through gateway
2. **Centralized Security** - JWT validation at gateway level
3. **Rate Limiting** - Prevents service overload
4. **Circuit Breaking** - Handles service failures gracefully
5. **Cross-cutting Concerns** - Logging, tracing, metrics
6. **Service Scaling** - Easy to add/remove services
7. **Monitoring** - Prometheus metrics exposed

## Next Steps

1. Create the `erp-api-gateway` folder in your project
2. Add these files following the structure
3. Build with Maven: `mvn clean install`
4. Run with Docker or locally: `java -jar target/erp-api-gateway-0.0.1-SNAPSHOT.jar`
5. Test at: `http://localhost:8080/health`


