# Security, Testing & CI/CD Best Practices Guide

## 🔐 Security Implementation Checklist

### 1. API Security

#### Request Signing (Prevent tampering)
```java
package com.example.security;

import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class RequestSigningService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private final String secretKey = "your-secret-key";

    public String generateSignature(String payload) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(secretKey.getBytes(), 0, 
                 secretKey.getBytes().length, HMAC_SHA256));
        return Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes()));
    }

    public boolean verifySignature(String payload, String signature) throws Exception {
        return generateSignature(payload).equals(signature);
    }
}
```

#### Rate Limiting Implementation
```java
package com.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitingConfig implements WebMvcConfigurer {

    @Bean
    public RateLimitingInterceptor rateLimitingInterceptor() {
        return new RateLimitingInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor())
                .addPathPatterns("/api/**");
    }

    static class RateLimitingInterceptor implements HandlerInterceptor {
        private static final int REQUESTS_PER_MINUTE = 100;
        private final Map<String, List<Long>> requestTimestamps = new ConcurrentHashMap<>();

        @Override
        public boolean preHandle(HttpServletRequest request, 
                                HttpServletResponse response, 
                                Object handler) throws Exception {
            String clientId = request.getRemoteUser() != null 
                    ? request.getRemoteUser() 
                    : request.getRemoteAddr();

            List<Long> timestamps = requestTimestamps.computeIfAbsent(
                    clientId, k -> Collections.synchronizedList(new ArrayList<>()));

            long now = System.currentTimeMillis();
            timestamps.removeIf(timestamp -> now - timestamp > 60000);

            if (timestamps.size() >= REQUESTS_PER_MINUTE) {
                response.setStatus(429); // Too Many Requests
                response.getWriter().write("Rate limit exceeded");
                return false;
            }

            timestamps.add(now);
            return true;
        }
    }
}
```

#### OWASP Security Headers
```java
package com.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class SecurityHeadersConfig implements WebMvcConfigurer {

    @Bean
    public SecurityHeadersInterceptor securityHeadersInterceptor() {
        return new SecurityHeadersInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityHeadersInterceptor());
    }

    static class SecurityHeadersInterceptor implements HandlerInterceptor {
        @Override
        public void postHandle(HttpServletRequest request, 
                              HttpServletResponse response, 
                              Object handler, 
                              org.springframework.web.servlet.ModelAndView modelAndView) {
            // Prevent Clickjacking
            response.setHeader("X-Frame-Options", "DENY");
            
            // Prevent MIME sniffing
            response.setHeader("X-Content-Type-Options", "nosniff");
            
            // Enable XSS Protection
            response.setHeader("X-XSS-Protection", "1; mode=block");
            
            // Content Security Policy
            response.setHeader("Content-Security-Policy", 
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self' data:;");
            
            // Referrer Policy
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            
            // Feature Policy
            response.setHeader("Permissions-Policy", 
                "geolocation=(), microphone=(), camera=()");
        }
    }
}
```

#### Encrypted Field Support
```java
package com.example.security;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.StandardPBEStringEncryptor;
import org.springframework.stereotype.Component;

@Component
public class FieldEncryptionService {

    private final StandardPBEStringEncryptor encryptor;

    public FieldEncryptionService() {
        this.encryptor = Encryptors.standard("myPassword", "5c0744940b5c369b");
    }

    public String encrypt(String data) {
        return encryptor.encrypt(data);
    }

    public String decrypt(String encryptedData) {
        return encryptor.decrypt(encryptedData);
    }
}
```

#### SQL Injection Prevention (Using Parameterized Queries)
```java
// ✅ CORRECT - Using JPA (automatically prevents SQL injection)
@Query("SELECT e FROM Employee e WHERE e.email = ?1")
Employee findByEmail(String email);

// ✅ CORRECT - Using parameterized native query
@Query(value = "SELECT * FROM employees WHERE email = :email", 
       nativeQuery = true)
Employee findByEmailNative(@Param("email") String email);

// ❌ WRONG - String concatenation (vulnerable)
// Query query = em.createQuery("SELECT e FROM Employee e WHERE e.email = '" + email + "'");
```

---

### 2. Secrets Management

```yaml
# application.yml - Never hardcode secrets!
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}  # Read from environment
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI}
          jwk-set-uri: ${KEYCLOAK_JWK_SET_URI}

# Use HashiCorp Vault for production
vault:
  enabled: true
  host: ${VAULT_HOST}
  port: ${VAULT_PORT}
  token: ${VAULT_TOKEN}
  scheme: https
```

```powershell
# Set environment variables (Windows PowerShell)
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5433/erp_db"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "secure_password"
$env:KEYCLOAK_ISSUER_URI = "http://localhost:8180/realms/erp-realm"
```

---

## 🧪 Testing Strategy

### 1. Unit Testing Template

```java
package com.example.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VendorServiceTest {

    @Mock
    private VendorRepository vendorRepository;

    @InjectMocks
    private VendorService vendorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateVendor_Success() {
        // Arrange
        VendorDTO dto = VendorDTO.builder()
                .vendorName("ACME Corp")
                .contactPerson("John Doe")
                .build();

        Vendor expectedVendor = Vendor.builder()
                .id(1L)
                .vendorName("ACME Corp")
                .build();

        when(vendorRepository.save(any(Vendor.class)))
                .thenReturn(expectedVendor);

        // Act
        Vendor result = vendorService.createVendor(dto);

        // Assert
        assertNotNull(result);
        assertEquals("ACME Corp", result.getVendorName());
        verify(vendorRepository, times(1)).save(any(Vendor.class));
    }

    @Test
    void testCreateVendor_InvalidInput() {
        // Arrange
        VendorDTO dto = VendorDTO.builder()
                .vendorName("") // Invalid: empty name
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                     () -> vendorService.createVendor(dto));
    }

    @Test
    void testFindVendor_NotFound() {
        // Arrange
        when(vendorRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, 
                     () -> vendorService.getVendor(999L));
    }
}
```

### 2. Integration Testing

```java
package com.example.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class ProcurementServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = 
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProcurementService procurementService;

    @Autowired
    private PurchaseOrderRepository poRepository;

    @Test
    void testCreatePurchaseOrderEnd2End() {
        // Arrange
        PurchaseRequestDTO prDto = PurchaseRequestDTO.builder()
                .description("Test PR")
                .build();

        PurchaseOrderDTO poDto = PurchaseOrderDTO.builder()
                .vendorId(1L)
                .build();

        // Act
        PurchaseRequest pr = procurementService.createPurchaseRequest(prDto, "tenant1");
        poDto.setPurchaseRequestId(pr.getId());
        PurchaseOrder po = procurementService.createPurchaseOrder(poDto, "tenant1");

        // Assert
        assertNotNull(po.getId());
        assertTrue(po.getPoNumber().startsWith("PO-"));
        
        PurchaseOrder found = poRepository.findById(po.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("DRAFT", found.getStatus());
    }
}
```

### 3. API Testing (Rest Assured)

```java
package com.example.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProcurementApiTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    @Test
    void testCreatePurchaseOrder_ReturnsCreated() {
        String requestBody = """
            {
                "vendorId": 1,
                "purchaseRequestId": 1,
                "dueDate": "2024-12-31"
            }
            """;

        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + getValidToken())
            .body(requestBody)
        .when()
            .post("/api/v1/procurement/purchase-orders")
        .then()
            .statusCode(200)
            .body("poNumber", notNullValue())
            .body("status", equalTo("DRAFT"));
    }

    @Test
    void testGetPurchaseOrder_NotFound() {
        given()
            .header("Authorization", "Bearer " + getValidToken())
        .when()
            .get("/api/v1/procurement/purchase-orders/999")
        .then()
            .statusCode(404);
    }

    private String getValidToken() {
        // Obtain token from Keycloak or mock
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
    }
}
```

### 4. Performance Testing (Gatling)

```java
package com.example.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class ProcurementPerformanceTest extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8087")
            .acceptHeader("application/json")
            .authorizationHeader("Bearer ${token}");

    ScenarioBuilder scenario = scenario("Procurement Scenario")
            .exec(http("Create PO")
                    .post("/api/v1/procurement/purchase-orders")
                    .body(StringBody("""
                        {
                            "vendorId": 1,
                            "purchaseRequestId": 1
                        }
                        """))
                    .check(status().is(200)))
            .pause(1)
            .exec(http("Get PO")
                    .get("/api/v1/procurement/purchase-orders/${poId}")
                    .check(status().is(200)));

    {
        setUp(
            scenario.injectOpen(rampUsers(10).during(Duration.ofSeconds(10)))
        ).protocols(httpProtocol)
         .assertions(
            global().responseTime().max().lt(500),
            global().successfulRequests().percent().gt(95.0)
         );
    }
}
```

---

## 🚀 CI/CD Pipeline with GitHub Actions

Create `.github/workflows/build-and-deploy.yml`:

```yaml
name: Build & Deploy ERP Services

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v2

    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Build all services
      run: |
        cd ai-erp-system
        mvn clean package -DskipTests

    - name: Run unit tests
      run: |
        cd ai-erp-system
        mvn test

    - name: Run integration tests
      run: |
        cd ai-erp-system
        mvn verify

    - name: SonarQube Analysis
      uses: SonarSource/sonarcloud-github-action@master
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}

    - name: Security scanning (Dependabot)
      uses: dependabot/fetch-metadata@v1

    - name: Build Docker images
      run: |
        docker build -t erp-api-gateway:latest ai-erp-system/erp-api-gateway/
        docker build -t erp-service-procurement:latest ai-erp-system/erp-service-procurement/

    - name: Push to Docker Hub
      if: github.ref == 'refs/heads/main'
      run: |
        echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
        docker tag erp-api-gateway:latest ${{ secrets.DOCKER_USERNAME }}/erp-api-gateway:latest
        docker push ${{ secrets.DOCKER_USERNAME }}/erp-api-gateway:latest

    - name: Deploy to staging
      if: github.ref == 'refs/heads/develop'
      run: |
        # Deploy to staging environment
        kubectl apply -f k8s/staging/ --kubeconfig=${{ secrets.KUBE_CONFIG_STAGING }}

    - name: Deploy to production
      if: github.ref == 'refs/heads/main' && github.event_name == 'push'
      run: |
        # Blue-Green deployment
        kubectl apply -f k8s/production/green/ --kubeconfig=${{ secrets.KUBE_CONFIG_PROD }}
        kubectl set selector service/erp-api-gateway version=green

    - name: Notify Slack
      uses: slackapi/slack-github-action@v1.24.0
      if: always()
      with:
        webhook-url: ${{ secrets.SLACK_WEBHOOK }}
        payload: |
          {
            "text": "Build ${{ job.status }} for ERP Services",
            "blocks": [
              {
                "type": "section",
                "text": {
                  "type": "mrkdwn",
                  "text": "*Build ${{ job.status }}*\nRepository: ${{ github.repository }}\nBranch: ${{ github.ref }}"
                }
              }
            ]
          }

  security-scan:
    runs-on: ubuntu-latest
    needs: build

    steps:
    - uses: actions/checkout@v2

    - name: Run Trivy vulnerability scan
      uses: aquasecurity/trivy-action@master
      with:
        image-ref: 'erp-api-gateway:latest'
        format: 'sarif'
        output: 'trivy-results.sarif'

    - name: Upload Trivy results to GitHub Security
      uses: github/codeql-action/upload-sarif@v2
      with:
        sarif_file: 'trivy-results.sarif'
```

---

## 📊 Code Quality Metrics

Add to pom.xml:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

## 🎯 Security Testing Checklist

- [ ] OWASP Top 10 vulnerability assessment
- [ ] Penetration testing
- [ ] SQL injection testing
- [ ] XSS testing
- [ ] CSRF protection verification
- [ ] Authentication bypass attempts
- [ ] Authorization bypass attempts
- [ ] Sensitive data exposure check
- [ ] XML External Entity (XXE) attacks
- [ ] Broken access control tests
- [ ] Cryptographic failures assessment
- [ ] Dependencies vulnerability scan

---

## Production Deployment Checklist

- [ ] All secrets moved to vault/environment variables
- [ ] HTTPS/TLS enabled
- [ ] Database backups configured
- [ ] Monitoring & alerting active
- [ ] Logging retention policy set
- [ ] Rate limiting configured
- [ ] CORS properly restricted
- [ ] API keys rotated
- [ ] Security patches applied
- [ ] Load testing completed
- [ ] Disaster recovery plan tested
- [ ] Rollback procedure documented


