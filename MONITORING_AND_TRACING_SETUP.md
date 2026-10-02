# Distributed Tracing & Monitoring Setup Guide

## Overview
This guide implements centralized logging, distributed tracing, and metrics collection for your microservices.

## Architecture
```
Your Services (HR, Finance, Inventory, Compliance, AI)
         ↓ (emit events)
    Sleuth (tags requests with trace IDs)
         ↓
    Jaeger/Zipkin (collects traces)
         ↓
    ELK Stack (logs storage & analysis)
         ↓
    Prometheus (metrics collection)
         ↓
    Grafana (visualization & dashboards)
```

## Docker Compose Addition

Add these services to your `docker-compose.yml`:

```yaml
  # ==========================================
  # 📊 OBSERVABILITY STACK
  # ==========================================

  # 1. Elasticsearch (for ELK)
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.10.0
    container_name: erp-elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data
    networks:
      - erp-network

  # 2. Logstash (for log processing)
  logstash:
    image: docker.elastic.co/logstash/logstash:8.10.0
    container_name: erp-logstash
    volumes:
      - ./monitoring/logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    ports:
      - "5000:5000"
    environment:
      - "LS_JAVA_OPTS=-Xmx256m -Xms256m"
    depends_on:
      - elasticsearch
    networks:
      - erp-network

  # 3. Kibana (visualization for ELK)
  kibana:
    image: docker.elastic.co/kibana/kibana:8.10.0
    container_name: erp-kibana
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch
    networks:
      - erp-network

  # 4. Jaeger (distributed tracing)
  jaeger:
    image: jaegertracing/all-in-one:latest
    container_name: erp-jaeger
    ports:
      - "16686:16686"  # Jaeger UI
      - "14268:14268"  # Jaeger collector
      - "6831:6831/udp"  # Jaeger agent
    environment:
      - COLLECTOR_OTLP_ENABLED=true
    networks:
      - erp-network

  # 5. Prometheus (metrics collection)
  prometheus:
    image: prom/prometheus:latest
    container_name: erp-prometheus
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    ports:
      - "9090:9090"
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
    networks:
      - erp-network

  # 6. Grafana (dashboards)
  grafana:
    image: grafana/grafana:latest
    container_name: erp-grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_SECURITY_ADMIN_USER=admin
    volumes:
      - grafana_data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources
    depends_on:
      - prometheus
    networks:
      - erp-network

volumes:
  elasticsearch_data:
  prometheus_data:
  grafana_data:
```

## Configuration Files

### 1. monitoring/prometheus.yml
```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  # API Gateway metrics
  - job_name: 'api-gateway'
    static_configs:
      - targets: ['api-gateway:8080']
    metrics_path: '/actuator/prometheus'

  # HR Service metrics
  - job_name: 'hr-service'
    static_configs:
      - targets: ['hr-service:8082']
    metrics_path: '/actuator/prometheus'

  # Finance Service metrics
  - job_name: 'finance-service'
    static_configs:
      - targets: ['finance-service:8084']
    metrics_path: '/actuator/prometheus'

  # Inventory Service metrics
  - job_name: 'inventory-service'
    static_configs:
      - targets: ['inventory-service:8083']
    metrics_path: '/actuator/prometheus'

  # Compliance Service metrics
  - job_name: 'compliance-service'
    static_configs:
      - targets: ['compliance-service:8086']
    metrics_path: '/actuator/prometheus'

  # AI Service metrics
  - job_name: 'ai-service'
    static_configs:
      - targets: ['ai-service:8085']
    metrics_path: '/actuator/prometheus'

  # Prometheus self-monitoring
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
```

### 2. monitoring/logstash.conf
```
input {
  tcp {
    port => 5000
    codec => json
  }
}

filter {
  if [type] == "java-springboot" {
    mutate {
      add_field => { "[@metadata][index_name]" => "erp-logs-%{+YYYY.MM.dd}" }
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "%{[@metadata][index_name]}"
  }
}
```

### 3. monitoring/grafana/datasources/prometheus.yml
```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
```

## Updated Service pom.xml

Add these dependencies to each microservice (e.g., erp-service-hr):

```xml
<!-- Add to pom.xml dependencies section -->

<!-- Spring Cloud Sleuth (Distributed Tracing) -->
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>

<!-- OpenTelemetry (Modern tracing) -->
<dependency>
  <groupId>io.opentelemetry</groupId>
  <artifactId>opentelemetry-exporter-jaeger</artifactId>
  <version>1.30.0</version>
</dependency>

<!-- Micrometer (Metrics) -->
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- Logstash Logback Encoder (for structured logging) -->
<dependency>
  <groupId>net.logstash.logback</groupId>
  <artifactId>logstash-logback-encoder</artifactId>
  <version>7.4</version>
</dependency>

<!-- Spring Boot Actuator (health checks & metrics) -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

## Updated application.yml for Services

Add to each service's `application.yml`:

```yaml
spring:
  application:
    name: erp-service-hr  # Change service name for each service
  
  # Sleuth Configuration
  sleuth:
    enabled: true
    trace-id-128: true  # Use 128-bit trace IDs
    sampler:
      probability: 1.0  # 1.0 = trace all requests (adjust for production)

  # Logging Configuration
  cloud:
    stream:
      kafka:
        binder:
          brokers: localhost:9092
        producer:
          compression: snappy

# Actuator endpoints for metrics
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,info
  
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
  
  metrics:
    export:
      prometheus:
        enabled: true

# Logging Configuration
logging:
  level:
    root: INFO
    com.example: DEBUG
    org.springframework.cloud.sleuth: DEBUG
  
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n [%thread] %level %logger{36}%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  
  file:
    name: logs/${spring.application.name}.log
    max-size: 10MB
    max-history: 10
```

## Custom Metrics Example

Create a service to track business metrics:

```java
package com.example.service.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class BusinessMetricsService {

    private final MeterRegistry meterRegistry;
    private final Counter employeeCreatedCounter;
    private final Timer transactionProcessingTimer;

    public BusinessMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Create custom counters
        this.employeeCreatedCounter = Counter.builder("erp.employees.created")
                .description("Total employees created")
                .register(meterRegistry);
        
        // Create custom timers
        this.transactionProcessingTimer = Timer.builder("erp.transaction.processing.time")
                .description("Time taken to process transactions")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    public void recordEmployeeCreation() {
        employeeCreatedCounter.increment();
    }

    public void recordTransactionProcessing(Runnable runnable) {
        transactionProcessingTimer.record(runnable);
    }
}
```

## Integration in Your Controllers

```java
package com.example.service.controller;

import com.example.service.metrics.BusinessMetricsService;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/hr")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final BusinessMetricsService metricsService;

    public EmployeeController(BusinessMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @PostMapping("/employees")
    public void addEmployee(@RequestBody EmployeeDTO employee) {
        logger.info("Adding new employee: {}", employee.getName());
        
        // Track this action
        metricsService.recordEmployeeCreation();
        
        // Your business logic here
        logger.info("Employee added successfully with ID: {}", employee.getId());
    }
}
```

## Access Points After Setup

Once everything is running:

1. **Jaeger UI** - `http://localhost:16686`
   - View all distributed traces
   - Search by service name
   - Analyze latency

2. **Prometheus** - `http://localhost:9090`
   - Query metrics directly
   - View targets
   - Set up alerts

3. **Kibana** - `http://localhost:5601`
   - Centralized log analysis
   - Create log dashboards
   - Search logs by trace ID

4. **Grafana** - `http://localhost:3000` (admin/admin)
   - Create beautiful dashboards
   - Combine metrics from Prometheus
   - Set up alerts

## Sample Grafana Dashboard JSON

Create `monitoring/grafana/dashboards/erp-overview.json`:

```json
{
  "dashboard": {
    "title": "ERP System Overview",
    "panels": [
      {
        "title": "API Gateway Request Rate",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count[5m])"
          }
        ]
      },
      {
        "title": "Service Response Time (p95)",
        "targets": [
          {
            "expr": "http_server_requests_seconds{quantile=\"0.95\"}"
          }
        ]
      },
      {
        "title": "Active Database Connections",
        "targets": [
          {
            "expr": "db_connections_active"
          }
        ]
      },
      {
        "title": "Kafka Message Processing Rate",
        "targets": [
          {
            "expr": "kafka_messages_processed_rate"
          }
        ]
      }
    ]
  }
}
```

## Alerts Configuration

Create `monitoring/prometheus/alerts.yml`:

```yaml
groups:
  - name: erp_alerts
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
        for: 5m
        annotations:
          summary: "High error rate detected"
          description: "Error rate is above 5%"

      - alert: ServiceDown
        expr: up{job=~".*-service"} == 0
        for: 2m
        annotations:
          summary: "Service is down"
          description: "{{ $labels.job }} is down"

      - alert: HighLatency
        expr: histogram_quantile(0.95, http_server_requests_seconds) > 1
        for: 5m
        annotations:
          summary: "High latency detected"
          description: "95th percentile latency is above 1 second"
```

## Best Practices

1. **Sampling**: In production, reduce sampling probability (e.g., 0.1 = 10%)
2. **Retention**: Set log retention policies based on storage capacity
3. **Alerts**: Create alerts for business KPIs, not just technical metrics
4. **Dashboards**: Create separate dashboards for different roles (DevOps, Business, Support)
5. **Indexing**: Use Elasticsearch index patterns for log partitioning

## Troubleshooting

If Jaeger traces don't appear:
- Check if Sleuth is enabled in application.yml
- Verify Jaeger collector is running: `curl http://localhost:14268/api/traces`
- Check if services are sending traces: `curl http://localhost:16686/api/services`

If Prometheus can't scrape metrics:
- Verify `/actuator/prometheus` endpoint is accessible
- Check if `spring-boot-starter-actuator` is added to pom.xml
- Ensure `management.endpoints.web.exposure.include` includes prometheus


