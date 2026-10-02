# ERP System Architecture Diagrams & Visual Guide

## Current Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (Angular 21)                     │
│              Dashboard | HR | Finance | Inventory            │
│                   Compliance | AI                            │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ↓ (Direct calls to ports)
         ┌───────────────────┴──────────────────┬──────────────┐
         ↓                    ↓                  ↓              ↓
    ┌─────────┐          ┌─────────┐      ┌──────────┐    ┌────────┐
    │   HR    │          │ Finance │      │Inventory │    │Compliance
    │ Service │          │ Service │      │ Service  │    │ Service
    │ (8082)  │          │ (8084)  │      │ (8083)   │    │ (8086)
    └────┬────┘          └────┬────┘      └────┬─────┘    └────┬────┘
         │                    │                 │               │
         └────────────────────┴─────────────────┴───────────────┘
                              ↓
                        ┌─────────────┐
                        │  Keycloak   │
                        │  (Auth)     │
                        │ (8180)      │
                        └─────────────┘
                              ↓
                        ┌─────────────┐
                        │ PostgreSQL  │
                        │ (Database)  │
                        │ (5433)      │
                        └─────────────┘

Event Bus:
    ┌──────────────┐
    │  Kafka       │
    │ (9092)       │
    └──────────────┘
         ↑
         │ (Events)
         ↓
    All Services (emit events)

Cache:
    ┌──────────────┐
    │  Redis       │
    │ (6379)       │
    └──────────────┘

Document Store:
    ┌──────────────┐
    │  MongoDB     │
    │ (27017)      │
    └──────────────┘
```

---

## Recommended Architecture (With Improvements)

```
┌─────────────────────────────────────────────────────────────────────┐
│                      Frontend (Angular 21)                          │
│          Dashboard | HR | Finance | Inventory | Procurement         │
│                   Compliance | AI                                   │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                ↓ (All requests through gateway)
                    ┌──────────────────────┐
                    │   API Gateway        │  ← NEW
                    │   (Spring Cloud)     │
                    │   - Routing          │
                    │   - Rate Limiting    │
                    │   - Security         │
                    │   - Circuit Breaker  │
                    │   (8080)             │
                    └──────────┬───────────┘
                               │
        ┌──────────────────────┼──────────────────────┬─────────────┐
        ↓                      ↓                      ↓             ↓
   ┌─────────┐            ┌─────────┐          ┌───────────┐   ┌──────────┐
   │   HR    │            │ Finance │          │Inventory  │   │Compliance
   │ Service │            │ Service │          │ Service   │   │ Service
   │ (8082)  │            │ (8084)  │          │ (8083)    │   │ (8086)
   └────┬────┘            └────┬────┘          └─────┬─────┘   └────┬─────┘
        │                      │                    │               │
        └──────────────────────┴────────────────────┴───────────────┘
                               ↓
                        ┌─────────────┐
                        │  Keycloak   │
                        │  (OAuth2)   │
                        │ (8180)      │
                        └─────────────┘

INFRASTRUCTURE LAYER:

    Database:
    ┌──────────────┐
    │ PostgreSQL   │
    │ (5433)       │
    └──────────────┘

    Event Bus:
    ┌──────────────┐
    │  Kafka       │
    │ (9092)       │
    │  + UI        │
    │ (8080)       │
    └──────────────┘

    Cache:
    ┌──────────────┐
    │  Redis       │
    │ (6379)       │
    └──────────────┘

    Document Store:
    ┌──────────────┐
    │  MongoDB     │
    │ (27017)      │
    └──────────────┘

OBSERVABILITY LAYER (NEW):

    Logging:
    ┌──────────────┐         ┌──────────────┐         ┌──────────────┐
    │ Elasticsearch│ ←─────  │  Logstash    │ ←─────  │  Services    │
    │ (9200)       │         │ (5000)       │         │ (logs)       │
    └──────────────┘         └──────────────┘         └──────────────┘
           ↓
    ┌──────────────┐
    │   Kibana     │
    │ (5601)       │
    └──────────────┘

    Tracing:
    ┌──────────────┐
    │   Jaeger     │ ←───── All Services (emit traces)
    │ (16686)      │
    └──────────────┘

    Metrics:
    ┌──────────────┐         ┌──────────────┐         ┌──────────────┐
    │ Prometheus   │ ←─────  │  Services    │         │   Grafana    │
    │ (9090)       │         │ /prometheus  │ ←─────  │ (3000)       │
    └──────────────┘         └──────────────┘         └──────────────┘
```

---

## Data Flow Diagrams

### Purchase Order Processing Flow

```
Frontend
   │
   ├─ Create PR
   │    │
   │    ↓
   │  API Gateway (validates JWT, applies rate limiting)
   │    │
   │    ↓
   │  Procurement Service
   │    │
   │    ├─ Validate PR (check department, cost center)
   │    │
   │    ├─ Save to PostgreSQL
   │    │
   │    ├─ Emit Event: "PR_CREATED"
   │    │
   │    └─ Return PR Details
   │
   ├─ Create PO from PR
   │    │
   │    ↓
   │  API Gateway
   │    │
   │    ↓
   │  Procurement Service
   │    │
   │    ├─ Fetch PR from DB
   │    │
   │    ├─ Validate Vendor
   │    │
   │    ├─ Calculate totals
   │    │
   │    ├─ Save PO to PostgreSQL
   │    │
   │    ├─ Update PR status
   │    │
   │    ├─ Emit Event: "PO_CREATED"
   │    │
   │    └─ Return PO Details
   │
   ├─ Receive Goods
   │    │
   │    ↓
   │  API Gateway
   │    │
   │    ↓
   │  Procurement Service
   │    │
   │    ├─ Create Receipt
   │    │
   │    ├─ Save to PostgreSQL
   │    │
   │    ├─ Emit Event: "RECEIPT_CREATED"
   │    │
   │    ├─ Trigger Inventory Service via Kafka
   │    │    │
   │    │    ↓
   │    │  Inventory Service (updates stock)
   │    │
   │    └─ Return Receipt Details
   │
   └─ Process Invoice
        │
        ↓
      API Gateway
        │
        ↓
      Procurement Service
        │
        ├─ Receive Invoice
        │
        ├─ Perform Three-Way Match
        │    (PO Amount vs Receipt Qty vs Invoice Amount)
        │
        ├─ If PASS:
        │    └─ Mark as "APPROVED"
        │        └─ Emit Event: "INVOICE_APPROVED"
        │           └─ Finance Service: Create GL Entry
        │
        └─ If FAIL:
           └─ Mark as "REJECTED"
              └─ Flag discrepancies for review
```

---

## Multi-Tenancy Flow

```
Keycloak
   │
   ├─ User logs in with credentials
   │
   ├─ Issues JWT with tenant_id claim
   │    (e.g., { "sub": "user1", "tenant_id": "tata_motors" })
   │
   └─ Frontend stores token
        │
        ↓
   API Gateway
        │
        ├─ Extracts tenant_id from JWT
        │
        ├─ Adds X-Tenant-ID header
        │
        └─ Routes to service
             │
             ↓
        Service Handler
             │
             ├─ Receives request with X-Tenant-ID
             │
             ├─ Filters all queries by tenant_id
             │
             ├─ Ensures data isolation
             │
             └─ Returns only tenant-specific data

Database (PostgreSQL)
   │
   ├─ Tenant A data (tenant_id = 'tata_motors')
   ├─ Tenant B data (tenant_id = 'mahindra')
   ├─ Tenant C data (tenant_id = 'hyundai')
   │
   └─ All queries filtered by tenant_id
```

---

## Event-Driven Architecture

```
Service A                Service B               Service C
  │                        │                       │
  ├─ Event: PR_CREATED     │                       │
  │         ↓              │                       │
  │      Kafka Topic 1     │                       │
  │         │              │                       │
  │         ├─────→ Service B Consumer             │
  │         │        (Updates forecasts)           │
  │         │                                      │
  │         └─────→ Service C Consumer             │
  │                (Updates vendor metrics)        │
  │
  ├─ Event: PO_CREATED
  │         ↓
  │      Kafka Topic 2
  │         │
  │         ├─────→ Inventory Service
  │         │        (Reserves stock)
  │         │
  │         ├─────→ Finance Service
  │         │        (Creates commitment)
  │         │
  │         └─────→ Compliance Service
  │                (Logs audit entry)
  │
  └─ Event: RECEIPT_CREATED
            ↓
         Kafka Topic 3
            │
            ├─────→ Inventory Service
            │        (Updates actual stock)
            │
            ├─────→ Procurement Service
            │        (Matches with PO)
            │
            └─────→ Finance Service
                   (Records goods receipt)

Kafka Broker
   │
   ├─ Topic: erp.procurement.events
   ├─ Topic: erp.inventory.events
   ├─ Topic: erp.finance.events
   │
   └─ All events with headers:
      - timestamp
      - trace_id (for tracing)
      - tenant_id (for multi-tenancy)
```

---

## Observability Stack Integration

```
All Microservices
   │
   ├─ Application Logs
   │    │
   │    ├─ Emitted in JSON format
   │    │
   │    └─ Sent to Logstash (TCP 5000)
   │         │
   │         ├─ Parsed and enriched
   │         │
   │         └─ Stored in Elasticsearch
   │              │
   │              └─ Visualized in Kibana (5601)
   │
   ├─ Distributed Traces
   │    │
   │    ├─ Tagged with trace_id by Sleuth
   │    │
   │    └─ Sent to Jaeger (port 14268)
   │         │
   │         ├─ Stored in Elasticsearch
   │         │
   │         └─ Visualized in Jaeger UI (16686)
   │
   └─ Metrics
        │
        ├─ Exposed on /actuator/prometheus
        │
        ├─ Scraped by Prometheus (9090)
        │
        ├─ Stored time-series data
        │
        └─ Visualized in Grafana (3000)
             │
             └─ Custom dashboards created
                (System Health, Performance, Business KPIs)
```

---

## Security Layers

```
┌─────────────────────────────────────────────────────────┐
│                       Internet                          │
└────────────────────────────┬────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────┐
│                    WAF / DDoS Protection                │
└────────────────────────────┬────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────┐
│                    Load Balancer (HTTPS)               │
└────────────────────────────┬────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────┐
│              API Gateway (Spring Cloud)                │
│                                                        │
│  ├─ CORS Validation                                   │
│  ├─ JWT Validation                                    │
│  ├─ Rate Limiting (per user)                          │
│  ├─ Request Signing Verification                      │
│  ├─ Security Headers Injection                        │
│  └─ Request/Response Logging                          │
└────────────────────────────┬────────────────────────────┘
                             │
         ┌───────────────────┼───────────────────┐
         ↓                   ↓                   ↓
    ┌─────────┐          ┌─────────┐       ┌──────────┐
    │ HR Svc  │          │ Finance │       │Inventory │
    │         │          │ Service │       │ Service  │
    │ ├─OAuth2│          │ ├─OAuth2│       │ ├─OAuth2 │
    │ ├─Audit │          │ ├─Audit │       │ ├─Audit  │
    │ └─Encrypt          │ └─Encrypt       │ └─Encrypt
    │   Fields           │   Fields        │   Fields
    └─────────┘          └─────────┘       └──────────┘
         │                   │                   │
         └───────────────────┼───────────────────┘
                             │
                             ↓
                    ┌─────────────────┐
                    │ PostgreSQL      │
                    │ - Encrypted PII │
                    │ - Row Security  │
                    │ - SSL/TLS conn  │
                    └─────────────────┘

Secrets Management (Vault)
   │
   ├─ Database passwords
   ├─ API keys
   ├─ JWT signing keys
   ├─ Encryption keys
   │
   └─ Rotated automatically
```

---

## Deployment Architecture (Kubernetes)

```
┌──────────────────────────────────────────────────────┐
│              Kubernetes Cluster                      │
│                                                      │
│  ┌──────────────────────────────────────────────┐  │
│  │          Ingress (nginx/traefik)            │  │
│  │  - Handles external traffic                 │  │
│  │  - SSL/TLS termination                      │  │
│  │  - Routing to services                      │  │
│  └──────────────────────────────────────────────┘  │
│                      │                              │
│  ┌──────────────────┼──────────────────────────┐  │
│  │                  ↓                          │  │
│  │  ┌──────────────────────────────────┐      │  │
│  │  │    API Gateway Pod               │      │  │
│  │  │  ├─ 3 replicas (HA)             │      │  │
│  │  │  └─ Load balanced               │      │  │
│  │  └──────────────────────────────────┘      │  │
│  │                  │                          │  │
│  │  ┌──────┬────────┼────────┬──────────┐     │  │
│  │  ↓      ↓        ↓        ↓          ↓     │  │
│  │ ┌────┐┌────┐┌────────┐┌────────┐┌────────┐│  │
│  │ │HR  ││Fin ││Inv.   ││Procure ││Compl. ││  │
│  │ │Pod ││Pod ││Pod    ││Pod    ││Pod   ││  │
│  │ │(3x)││(3x)││(2x)   ││(3x)   ││(2x)  ││  │
│  │ └────┘└────┘└────────┘└────────┘└────────┘│  │
│  │                  │                        │  │
│  └──────────────────┼────────────────────────┘  │
│                     │                           │
│  ┌──────────────────┼────────────────────────┐  │
│  │                  ↓                        │  │
│  │  StatefulSets:                           │  │
│  │  ├─ PostgreSQL (1 master, 2 replicas)   │  │
│  │  ├─ MongoDB (1 master, 2 replicas)      │  │
│  │  ├─ Redis (1 master, 1 replica)         │  │
│  │  └─ Kafka (3 brokers + Zookeeper)       │  │
│  │                                          │  │
│  │  ConfigMaps/Secrets:                    │  │
│  │  ├─ Environment configs                 │  │
│  │  ├─ Database credentials                │  │
│  │  ├─ API keys                            │  │
│  │  └─ Keycloak configs                    │  │
│  │                                          │  │
│  │  PersistentVolumes:                     │  │
│  │  ├─ Database storage                    │  │
│  │  ├─ Elasticsearch storage               │  │
│  │  └─ Prometheus storage                  │  │
│  │                                          │  │
│  └──────────────────────────────────────────┘  │
│                                                │
│  Monitoring (kube-prometheus):                 │
│  ├─ Prometheus                                │
│  ├─ Grafana                                   │
│  ├─ AlertManager                              │
│  └─ Node Exporter                             │
│                                                │
└──────────────────────────────────────────────────┘
```

---

## CI/CD Pipeline Flow

```
Developer
   │
   └─ Pushes code to GitHub
        │
        ↓
   GitHub Actions Triggered
        │
        ├─ Checkout code
        │
        ├─ Build & Compile
        │    └─ Maven clean package
        │
        ├─ Run Unit Tests
        │    └─ JUnit 5 + Mockito (80%+ coverage)
        │
        ├─ Run Integration Tests
        │    └─ TestContainers (PostgreSQL, Kafka)
        │
        ├─ Static Analysis
        │    ├─ SonarQube scan
        │    └─ Code quality check
        │
        ├─ Security Scan
        │    ├─ Dependency check (CVE)
        │    └─ SAST scan
        │
        ├─ Build Docker Image
        │    ├─ Multi-stage build
        │    └─ Alpine base image
        │
        ├─ Push to Registry
        │    └─ Docker Hub / ECR
        │
        ├─ Deploy to Staging
        │    ├─ Update Kubernetes manifests
        │    └─ Run smoke tests
        │
        └─ On Main Branch: Deploy to Production
            ├─ Blue-Green Deployment
            │  ├─ Deploy to Green environment
            │  ├─ Run acceptance tests
            │  └─ Switch traffic: Blue → Green
            │
            └─ Notify Slack / Teams

Failure at any stage:
   └─ Rollback
   └─ Notify team
   └─ Create issue
```

---

## Database Architecture

```
PostgreSQL (Primary Database)
   │
   ├─ Tenant A (Schema)
   │    ├─ employees
   │    ├─ vendors
   │    ├─ purchase_orders
   │    ├─ invoices
   │    └─ [other tables]
   │
   ├─ Tenant B (Schema)
   │    ├─ employees
   │    ├─ vendors
   │    └─ [other tables]
   │
   ├─ Tenant C (Schema)
   │    └─ [other tables]
   │
   └─ Replication
        │
        ├─ Read Replica 1 (for reporting)
        │
        └─ Read Replica 2 (for analytics)

MongoDB (Document Store - Compliance)
   │
   ├─ Database: erp_audit_db
   │    │
   │    ├─ Collection: audit_logs
   │    │    └─ {_id, tenant_id, timestamp, action, user, data}
   │    │
   │    └─ Collection: compliance_records
   │         └─ {_id, tenant_id, checklist_id, status}
   │
   └─ Sharded by tenant_id (for scale)

Redis (Cache)
   │
   ├─ Key format: tenant:entity:id
   │    └─ Example: tata_motors:vendor:123
   │
   ├─ TTL: 1 hour (configurable)
   │
   └─ Eviction policy: LRU (Least Recently Used)

Elasticsearch (Logging)
   │
   ├─ Index pattern: erp-logs-*
   │    └─ erp-logs-2024.04.29
   │    └─ erp-logs-2024.04.30
   │
   ├─ Mapping:
   │    ├─ timestamp
   │    ├─ level
   │    ├─ logger_name
   │    ├─ message
   │    ├─ trace_id
   │    ├─ span_id
   │    └─ tenant_id
   │
   └─ Retention: 30 days
```

---

## Performance Optimization Strategy

```
Load on Frontend (Angular)
   │
   ├─ Asset optimization
   │    ├─ Minification (JS, CSS)
   │    ├─ Gzip compression
   │    ├─ Lazy loading modules
   │    └─ CDN for static assets
   │
   └─ API optimization
        │
        └─ Implement caching
             │
             └─ GET /employees
                  ├─ Browser cache (5 min)
                  ├─ API Gateway cache (2 min)
                  └─ Redis cache (1 hour)

API Gateway Layer
   │
   ├─ Request batching
   │
   ├─ Response compression
   │
   └─ Connection pooling

Service Layer (Business Logic)
   │
   ├─ Database query optimization
   │    ├─ Proper indexing
   │    ├─ N+1 query prevention
   │    └─ Query result pagination
   │
   ├─ Lazy loading relationships
   │
   └─ Async processing for heavy operations

Database Layer (PostgreSQL)
   │
   ├─ Indexes on frequently queried columns
   │    └─ Examples: tenant_id, status, date_range
   │
   ├─ Partitioning by date/tenant
   │
   ├─ Connection pooling (HikariCP)
   │
   ├─ Read replicas for reporting
   │
   └─ Query optimization (EXPLAIN ANALYZE)

Cache Layer (Redis)
   │
   ├─ Cache-aside pattern
   │    └─ Check cache first, then DB
   │
   ├─ Time-based expiration
   │    └─ 1 hour for most data
   │
   ├─ Event-based invalidation
   │    └─ Invalidate on data change
   │
   └─ Clustering for HA
        └─ Master-slave replication

Message Queue (Kafka)
   │
   ├─ Async processing
   │
   ├─ Batch processing
   │
   └─ Off-peak processing
        └─ Analytics, reports run at night
```

---

## Scaling Strategy

```
Horizontal Scaling (Add more instances)
   │
   ├─ API Gateway
   │    └─ 3+ replicas behind load balancer
   │
   ├─ Microservices
   │    ├─ HR Service: 2-5 replicas
   │    ├─ Finance Service: 2-5 replicas
   │    ├─ Inventory Service: 2-5 replicas
   │    ├─ Procurement Service: 3-10 replicas (heavy load)
   │    └─ AI Service: 1-3 replicas
   │
   └─ Stateless design
        └─ Session stored in Redis, not in-memory

Vertical Scaling (Increase resources)
   │
   ├─ Database (PostgreSQL)
   │    └─ Upgrade CPU, RAM
   │
   ├─ Cache (Redis)
   │    └─ Increase memory
   │
   ├─ Message Queue (Kafka)
   │    └─ Add more brokers
   │
   └─ Search (Elasticsearch)
        └─ Increase nodes and shards

Database Scaling
   │
   ├─ Read replicas (for reporting queries)
   │
   ├─ Sharding by tenant (multi-database approach)
   │
   ├─ Partitioning by date
   │    └─ Separate tables for each month
   │
   └─ Archive old data
        └─ Move to cold storage (S3)

Caching Strategy
   │
   ├─ L1 Cache (Browser/Frontend)
   │    └─ HTTP cache headers
   │
   ├─ L2 Cache (API Gateway)
   │    └─ Redis for popular endpoints
   │
   ├─ L3 Cache (Application)
   │    └─ Spring Cache abstraction
   │
   └─ L4 Cache (Database)
        └─ Query result caching
```

---

**These diagrams represent the complete, production-ready ERP system architecture with all improvements implemented.**


