# Procurement Service - Complete Implementation Guide

## Overview
The Procurement service handles the entire purchase-to-pay workflow:
- Purchase Requests (PR)
- Purchase Orders (PO)
- Receipts & Invoicing
- Vendor Management
- Three-way Matching

## Project Structure

```
erp-service-procurement/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/procurement/
│   │   │   ├── ProcurementApplication.java
│   │   │   ├── config/
│   │   │   │   ├── KafkaConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── entity/
│   │   │   │   ├── PurchaseRequest.java
│   │   │   │   ├── PurchaseOrder.java
│   │   │   │   ├── Receipt.java
│   │   │   │   ├── Invoice.java
│   │   │   │   └── Vendor.java
│   │   │   ├── repository/
│   │   │   │   ├── PurchaseRequestRepository.java
│   │   │   │   ├── PurchaseOrderRepository.java
│   │   │   │   ├── ReceiptRepository.java
│   │   │   │   ├── InvoiceRepository.java
│   │   │   │   └── VendorRepository.java
│   │   │   ├── service/
│   │   │   │   ├── ProcurementService.java
│   │   │   │   ├── VendorService.java
│   │   │   │   └── ThreeWayMatchService.java
│   │   │   ├── controller/
│   │   │   │   ├── ProcurementController.java
│   │   │   │   └── VendorController.java
│   │   │   ├── dto/
│   │   │   │   ├── PurchaseRequestDTO.java
│   │   │   │   ├── PurchaseOrderDTO.java
│   │   │   │   ├── VendorDTO.java
│   │   │   │   └── ThreeWayMatchDTO.java
│   │   │   └── kafka/
│   │   │       ├── ProcurementProducer.java
│   │   │       └── ProcurementConsumer.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │           ├── V1__create_procurement_schema.sql
│   │           └── V2__add_vendor_ratings.sql
│   └── test/
│       └── java/com/example/procurement/
│           ├── ProcurementServiceTest.java
│           └── ThreeWayMatchServiceTest.java
├── Dockerfile
└── README.md
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
    <artifactId>erp-service-procurement</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>erp-service-procurement</name>
    <description>Procurement Microservice for ERP System</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-sleuth</artifactId>
        </dependency>

        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>

        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

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

### 2. ProcurementApplication.java

```java
package com.example.procurement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProcurementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcurementApplication.class, args);
    }

}
```

### 3. Entity Classes

#### Vendor.java
```java
package com.example.procurement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vendors")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String vendorCode;

    @Column(nullable = false)
    private String vendorName;

    @Column(nullable = false)
    private String contactPerson;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    @Column
    private String city;

    @Column
    private String state;

    @Column
    private String postalCode;

    @Column
    private String country;

    @Column(nullable = false)
    private String paymentTerms; // e.g., "Net 30", "COD"

    @Column
    private Double performanceRating; // 0.0 to 5.0

    @Column
    private Integer deliveryPerformanceScore; // 0-100

    @Column
    private Integer qualityScore; // 0-100

    @Column
    private Integer responseScore; // 0-100

    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE, SUSPENDED

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

}
```

#### PurchaseRequest.java
```java
package com.example.procurement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "purchase_requests")
public class PurchaseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String prNumber;

    @Column(nullable = false)
    private String description;

    @Column
    private String departmentId; // Reference to HR/Inventory/etc

    @Column
    private String createdBy; // User ID who created this

    @Column(nullable = false)
    private String status; // DRAFT, SUBMITTED, APPROVED, REJECTED, CONVERTED_TO_PO

    @Enumerated(EnumType.STRING)
    private PriorityLevel priority; // LOW, MEDIUM, HIGH, URGENT

    @Column
    private LocalDateTime requiredDate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "purchase_request_id")
    private List<PurchaseRequestLineItem> lineItems;

    @Column
    private String approverComments;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum PriorityLevel {
        LOW, MEDIUM, HIGH, URGENT
    }

}
```

#### PurchaseRequestLineItem.java
```java
package com.example.procurement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "purchase_request_line_items")
public class PurchaseRequestLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long purchaseRequestId;

    @Column(nullable = false)
    private String itemCode;

    @Column(nullable = false)
    private String itemDescription;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private String uom; // Unit of Measure

    @Column(nullable = false)
    private BigDecimal estimatedUnitPrice;

    @Column
    private String accountCode; // Cost center/GL account

    @Column
    private String notes;

}
```

#### PurchaseOrder.java
```java
package com.example.procurement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String poNumber;

    @Column(nullable = false)
    private Long vendorId;

    @Column(nullable = false)
    private Long purchaseRequestId;

    @Column
    private LocalDateTime poDate;

    @Column
    private LocalDateTime dueDate;

    @Column
    private LocalDateTime expectedDeliveryDate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "purchase_order_id")
    private List<PurchaseOrderLineItem> lineItems;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column
    private BigDecimal taxAmount;

    @Column
    private BigDecimal shippingAmount;

    @Column
    private BigDecimal discountAmount;

    @Column
    private BigDecimal netAmount;

    @Column(nullable = false)
    private String status; // DRAFT, SENT, ACKNOWLEDGED, RECEIVED, COMPLETED, CANCELLED

    @Column
    private String shippingAddress;

    @Column
    private String specialInstructions;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

}
```

#### PurchaseOrderLineItem.java
```java
package com.example.procurement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "purchase_order_line_items")
public class PurchaseOrderLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long purchaseOrderId;

    @Column(nullable = false)
    private String itemCode;

    @Column(nullable = false)
    private String itemDescription;

    @Column(nullable = false)
    private Long orderedQuantity;

    @Column
    private Long receivedQuantity;

    @Column
    private Long invoicedQuantity;

    @Column(nullable = false)
    private String uom;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column
    private BigDecimal lineAmount;

    @Column
    private String status; // PENDING, PARTIAL, COMPLETE

}
```

#### Receipt.java
```java
package com.example.procurement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "receipts")
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String receiptNumber;

    @Column(nullable = false)
    private Long purchaseOrderId;

    @Column(nullable = false)
    private LocalDateTime receiptDate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "receipt_id")
    private List<ReceiptLineItem> lineItems;

    @Column
    private String receivedBy; // User who received

    @Column
    private String qualityInspection; // PASSED, FAILED, PARTIAL

    @Column
    private String inspectionNotes;

    @Column(nullable = false)
    private String status; // RECEIVED, INSPECTED, REJECTED, ACCEPTED

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

}
```

#### ReceiptLineItem.java
```java
package com.example.procurement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "receipt_line_items")
public class ReceiptLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long receiptId;

    @Column
    private Long poLineItemId;

    @Column(nullable = false)
    private Long receivedQuantity;

    @Column
    private String serialNumber;

    @Column
    private String batchNumber;

    @Column
    private String qualityStatus; // OK, DEFECTIVE, PARTIAL

    @Column
    private String notes;

}
```

#### Invoice.java
```java
package com.example.procurement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private Long vendorId;

    @Column(nullable = false)
    private Long purchaseOrderId;

    @Column
    private Long receiptId;

    @Column(nullable = false)
    private LocalDateTime invoiceDate;

    @Column
    private LocalDateTime dueDate;

    @Column(nullable = false)
    private BigDecimal invoiceAmount;

    @Column
    private BigDecimal taxAmount;

    @Column
    private BigDecimal netAmount;

    @Column
    private BigDecimal amountPaid;

    @Column
    private BigDecimal amountDue;

    @Column(nullable = false)
    private String status; // RECEIVED, MATCHED, APPROVED, PAID, PARTIAL, REJECTED

    @Column
    private String matchingStatus; // THREE_WAY_PASS, THREE_WAY_FAIL, DISCREPANCIES

    @Column
    private String discrepancyNotes;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

}
```

### 4. Repository Interfaces

```java
// VendorRepository.java
package com.example.procurement.repository;

import com.example.procurement.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByVendorCodeAndTenantId(String vendorCode, String tenantId);
    List<Vendor> findByStatusAndTenantId(String status, String tenantId);
    List<Vendor> findByTenantId(String tenantId);
}

// PurchaseRequestRepository.java
package com.example.procurement.repository;

import com.example.procurement.entity.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {
    Optional<PurchaseRequest> findByPrNumberAndTenantId(String prNumber, String tenantId);
    List<PurchaseRequest> findByStatusAndTenantId(String status, String tenantId);
    List<PurchaseRequest> findByTenantId(String tenantId);
}

// PurchaseOrderRepository.java
package com.example.procurement.repository;

import com.example.procurement.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByPoNumberAndTenantId(String poNumber, String tenantId);
    List<PurchaseOrder> findByVendorIdAndTenantId(Long vendorId, String tenantId);
    List<PurchaseOrder> findByStatusAndTenantId(String status, String tenantId);
    List<PurchaseOrder> findByTenantId(String tenantId);
}

// ReceiptRepository.java
package com.example.procurement.repository;

import com.example.procurement.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findByPurchaseOrderIdAndTenantId(Long poId, String tenantId);
    List<Receipt> findByTenantId(String tenantId);
}

// InvoiceRepository.java
package com.example.procurement.repository;

import com.example.procurement.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumberAndTenantId(String invoiceNumber, String tenantId);
    List<Invoice> findByVendorIdAndTenantId(Long vendorId, String tenantId);
    List<Invoice> findByStatusAndTenantId(String status, String tenantId);
    List<Invoice> findByTenantId(String tenantId);
}
```

### 5. Service Layer

```java
// ProcurementService.java
package com.example.procurement.service;

import com.example.procurement.dto.PurchaseOrderDTO;
import com.example.procurement.dto.PurchaseRequestDTO;
import com.example.procurement.entity.*;
import com.example.procurement.kafka.ProcurementProducer;
import com.example.procurement.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class ProcurementService {

    private final PurchaseRequestRepository prRepository;
    private final PurchaseOrderRepository poRepository;
    private final ReceiptRepository receiptRepository;
    private final InvoiceRepository invoiceRepository;
    private final VendorRepository vendorRepository;
    private final ProcurementProducer producer;

    public ProcurementService(PurchaseRequestRepository prRepository,
                            PurchaseOrderRepository poRepository,
                            ReceiptRepository receiptRepository,
                            InvoiceRepository invoiceRepository,
                            VendorRepository vendorRepository,
                            ProcurementProducer producer) {
        this.prRepository = prRepository;
        this.poRepository = poRepository;
        this.receiptRepository = receiptRepository;
        this.invoiceRepository = invoiceRepository;
        this.vendorRepository = vendorRepository;
        this.producer = producer;
    }

    public PurchaseRequest createPurchaseRequest(PurchaseRequestDTO dto, String tenantId) {
        log.info("Creating purchase request for tenant: {}", tenantId);

        PurchaseRequest pr = PurchaseRequest.builder()
                .prNumber(generatePRNumber(tenantId))
                .description(dto.getDescription())
                .departmentId(dto.getDepartmentId())
                .createdBy(dto.getCreatedBy())
                .status("DRAFT")
                .priority(dto.getPriority())
                .requiredDate(dto.getRequiredDate())
                .tenantId(tenantId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (dto.getLineItems() != null) {
            pr.setLineItems(dto.getLineItems());
        }

        PurchaseRequest saved = prRepository.save(pr);
        log.info("Purchase request created with ID: {}", saved.getId());

        // Emit event for other services
        producer.sendPurchaseRequestEvent("PR_CREATED", saved);

        return saved;
    }

    public PurchaseOrder createPurchaseOrder(PurchaseOrderDTO dto, String tenantId) {
        log.info("Creating purchase order for PR ID: {}", dto.getPurchaseRequestId());

        // Fetch the PR and validate
        PurchaseRequest pr = prRepository.findById(dto.getPurchaseRequestId())
                .orElseThrow(() -> new RuntimeException("PR not found"));

        // Fetch vendor
        Vendor vendor = vendorRepository.findById(dto.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        // Calculate totals
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderLineItem item : dto.getLineItems()) {
            totalAmount = totalAmount.add(item.getLineAmount());
        }

        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(generatePONumber(tenantId))
                .vendorId(dto.getVendorId())
                .purchaseRequestId(dto.getPurchaseRequestId())
                .poDate(LocalDateTime.now())
                .dueDate(dto.getDueDate())
                .expectedDeliveryDate(dto.getExpectedDeliveryDate())
                .lineItems(dto.getLineItems())
                .totalAmount(totalAmount)
                .taxAmount(dto.getTaxAmount())
                .shippingAmount(dto.getShippingAmount())
                .discountAmount(dto.getDiscountAmount())
                .netAmount(totalAmount.add(dto.getTaxAmount())
                        .add(dto.getShippingAmount())
                        .subtract(dto.getDiscountAmount()))
                .status("DRAFT")
                .tenantId(tenantId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PurchaseOrder saved = poRepository.save(po);

        // Update PR status
        pr.setStatus("CONVERTED_TO_PO");
        prRepository.save(pr);

        // Emit event
        producer.sendPurchaseOrderEvent("PO_CREATED", saved);

        log.info("Purchase order created with number: {}", saved.getPoNumber());
        return saved;
    }

    public Receipt receiveGoods(Long poId, Receipt receipt, String tenantId) {
        log.info("Receiving goods for PO ID: {}", poId);

        PurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("PO not found"));

        receipt.setReceiptNumber(generateReceiptNumber(tenantId));
        receipt.setPurchaseOrderId(poId);
        receipt.setReceiptDate(LocalDateTime.now());
        receipt.setStatus("RECEIVED");
        receipt.setTenantId(tenantId);

        Receipt saved = receiptRepository.save(receipt);

        // Update PO status
        po.setStatus("RECEIVED");
        poRepository.save(po);

        // Emit event
        producer.sendReceiptEvent("RECEIPT_CREATED", saved);

        // Trigger inventory update via Kafka
        producer.sendInventoryUpdate(po.getId(), saved.getId());

        log.info("Receipt created with number: {}", saved.getReceiptNumber());
        return saved;
    }

    private String generatePRNumber(String tenantId) {
        return "PR-" + tenantId + "-" + System.currentTimeMillis();
    }

    private String generatePONumber(String tenantId) {
        return "PO-" + tenantId + "-" + System.currentTimeMillis();
    }

    private String generateReceiptNumber(String tenantId) {
        return "REC-" + tenantId + "-" + System.currentTimeMillis();
    }

}
```

### 6. Three-Way Match Service

```java
// ThreeWayMatchService.java
package com.example.procurement.service;

import com.example.procurement.entity.Invoice;
import com.example.procurement.entity.PurchaseOrder;
import com.example.procurement.entity.Receipt;
import com.example.procurement.repository.InvoiceRepository;
import com.example.procurement.repository.PurchaseOrderRepository;
import com.example.procurement.repository.ReceiptRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Slf4j
@Service
@Transactional
public class ThreeWayMatchService {

    private final InvoiceRepository invoiceRepository;
    private final PurchaseOrderRepository poRepository;
    private final ReceiptRepository receiptRepository;

    public ThreeWayMatchService(InvoiceRepository invoiceRepository,
                               PurchaseOrderRepository poRepository,
                               ReceiptRepository receiptRepository) {
        this.invoiceRepository = invoiceRepository;
        this.poRepository = poRepository;
        this.receiptRepository = receiptRepository;
    }

    public Invoice performThreeWayMatch(Long invoiceId, String tenantId) {
        log.info("Performing three-way match for invoice ID: {}", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        PurchaseOrder po = poRepository.findById(invoice.getPurchaseOrderId())
                .orElseThrow(() -> new RuntimeException("PO not found"));

        Receipt receipt = receiptRepository.findById(invoice.getReceiptId())
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        boolean match = true;
        StringBuilder discrepancies = new StringBuilder();

        // Check 1: PO Amount vs Invoice Amount (allow 2% variance)
        BigDecimal poAmount = po.getNetAmount();
        BigDecimal invoiceAmount = invoice.getNetAmount();
        BigDecimal variance = poAmount.multiply(BigDecimal.valueOf(0.02));

        if (invoiceAmount.compareTo(poAmount.subtract(variance)) < 0 ||
            invoiceAmount.compareTo(poAmount.add(variance)) > 0) {
            match = false;
            discrepancies.append("Amount mismatch: PO=").append(poAmount)
                    .append(", Invoice=").append(invoiceAmount).append("; ");
        }

        // Check 2: PO Quantity vs Receipt Quantity
        long poQuantity = po.getLineItems().stream()
                .mapToLong(item -> item.getOrderedQuantity()).sum();
        long receivedQuantity = receipt.getLineItems().stream()
                .mapToLong(item -> item.getReceivedQuantity()).sum();

        if (poQuantity != receivedQuantity) {
            match = false;
            discrepancies.append("Quantity mismatch: PO=").append(poQuantity)
                    .append(", Received=").append(receivedQuantity).append("; ");
        }

        // Check 3: Receipt Quality Status
        if ("DEFECTIVE".equals(receipt.getQualityInspection())) {
            match = false;
            discrepancies.append("Quality inspection failed; ");
        }

        if (match) {
            invoice.setMatchingStatus("THREE_WAY_PASS");
            invoice.setStatus("APPROVED");
            log.info("Three-way match PASSED for invoice: {}", invoice.getInvoiceNumber());
        } else {
            invoice.setMatchingStatus("THREE_WAY_FAIL");
            invoice.setStatus("REJECTED");
            invoice.setDiscrepancyNotes(discrepancies.toString());
            log.warn("Three-way match FAILED for invoice: {} - {}", 
                    invoice.getInvoiceNumber(), discrepancies);
        }

        return invoiceRepository.save(invoice);
    }

}
```

### 7. Controller

```java
// ProcurementController.java
package com.example.procurement.controller;

import com.example.procurement.dto.PurchaseOrderDTO;
import com.example.procurement.dto.PurchaseRequestDTO;
import com.example.procurement.entity.Invoice;
import com.example.procurement.entity.PurchaseOrder;
import com.example.procurement.entity.PurchaseRequest;
import com.example.procurement.entity.Receipt;
import com.example.procurement.service.ProcurementService;
import com.example.procurement.service.ThreeWayMatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/procurement")
public class ProcurementController {

    private final ProcurementService procurementService;
    private final ThreeWayMatchService threeWayMatchService;

    public ProcurementController(ProcurementService procurementService,
                                ThreeWayMatchService threeWayMatchService) {
        this.procurementService = procurementService;
        this.threeWayMatchService = threeWayMatchService;
    }

    @PostMapping("/purchase-requests")
    public ResponseEntity<PurchaseRequest> createPurchaseRequest(
            @RequestBody PurchaseRequestDTO dto,
            Authentication auth) {
        String tenantId = auth.getPrincipal().toString();
        return ResponseEntity.ok(procurementService.createPurchaseRequest(dto, tenantId));
    }

    @PostMapping("/purchase-orders")
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(
            @RequestBody PurchaseOrderDTO dto,
            Authentication auth) {
        String tenantId = auth.getPrincipal().toString();
        return ResponseEntity.ok(procurementService.createPurchaseOrder(dto, tenantId));
    }

    @PostMapping("/receipts/{poId}")
    public ResponseEntity<Receipt> receiveGoods(
            @PathVariable Long poId,
            @RequestBody Receipt receipt,
            Authentication auth) {
        String tenantId = auth.getPrincipal().toString();
        return ResponseEntity.ok(procurementService.receiveGoods(poId, receipt, tenantId));
    }

    @PostMapping("/invoices/{invoiceId}/three-way-match")
    public ResponseEntity<Invoice> performThreeWayMatch(
            @PathVariable Long invoiceId,
            Authentication auth) {
        String tenantId = auth.getPrincipal().toString();
        return ResponseEntity.ok(threeWayMatchService.performThreeWayMatch(invoiceId, tenantId));
    }

}
```

### 8. application.yml

```yaml
spring:
  application:
    name: erp-service-procurement
  datasource:
    url: jdbc:postgresql://localhost:5433/erp_db
    username: ${POSTGRES_USER:postgres}
    password: ${POSTGRES_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

server:
  port: 8087

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

### 9. Database Migration (V1__create_procurement_schema.sql)

```sql
CREATE TABLE vendors (
    id SERIAL PRIMARY KEY,
    vendor_code VARCHAR(50) UNIQUE NOT NULL,
    vendor_name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    payment_terms VARCHAR(50) NOT NULL,
    performance_rating DECIMAL(3,2),
    delivery_performance_score INT,
    quality_score INT,
    response_score INT,
    status VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE purchase_requests (
    id SERIAL PRIMARY KEY,
    pr_number VARCHAR(100) UNIQUE NOT NULL,
    description TEXT NOT NULL,
    department_id VARCHAR(100),
    created_by VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50),
    required_date TIMESTAMP,
    approver_comments TEXT,
    tenant_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE purchase_request_line_items (
    id SERIAL PRIMARY KEY,
    purchase_request_id BIGINT,
    item_code VARCHAR(100) NOT NULL,
    item_description TEXT NOT NULL,
    quantity BIGINT NOT NULL,
    uom VARCHAR(20) NOT NULL,
    estimated_unit_price DECIMAL(10,2) NOT NULL,
    account_code VARCHAR(50),
    notes TEXT
);

CREATE TABLE purchase_orders (
    id SERIAL PRIMARY KEY,
    po_number VARCHAR(100) UNIQUE NOT NULL,
    vendor_id BIGINT NOT NULL,
    purchase_request_id BIGINT NOT NULL,
    po_date TIMESTAMP,
    due_date TIMESTAMP,
    expected_delivery_date TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL,
    tax_amount DECIMAL(12,2),
    shipping_amount DECIMAL(12,2),
    discount_amount DECIMAL(12,2),
    net_amount DECIMAL(12,2),
    status VARCHAR(50) NOT NULL,
    shipping_address TEXT,
    special_instructions TEXT,
    tenant_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    FOREIGN KEY (purchase_request_id) REFERENCES purchase_requests(id)
);

CREATE TABLE purchase_order_line_items (
    id SERIAL PRIMARY KEY,
    purchase_order_id BIGINT,
    item_code VARCHAR(100) NOT NULL,
    item_description TEXT NOT NULL,
    ordered_quantity BIGINT NOT NULL,
    received_quantity BIGINT,
    invoiced_quantity BIGINT,
    uom VARCHAR(20) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_amount DECIMAL(12,2),
    status VARCHAR(50)
);

CREATE TABLE receipts (
    id SERIAL PRIMARY KEY,
    receipt_number VARCHAR(100) UNIQUE NOT NULL,
    purchase_order_id BIGINT NOT NULL,
    receipt_date TIMESTAMP NOT NULL,
    received_by VARCHAR(100),
    quality_inspection VARCHAR(50),
    inspection_notes TEXT,
    status VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id)
);

CREATE TABLE receipt_line_items (
    id SERIAL PRIMARY KEY,
    receipt_id BIGINT,
    po_line_item_id BIGINT,
    received_quantity BIGINT NOT NULL,
    serial_number VARCHAR(100),
    batch_number VARCHAR(100),
    quality_status VARCHAR(50),
    notes TEXT
);

CREATE TABLE invoices (
    id SERIAL PRIMARY KEY,
    invoice_number VARCHAR(100) UNIQUE NOT NULL,
    vendor_id BIGINT NOT NULL,
    purchase_order_id BIGINT NOT NULL,
    receipt_id BIGINT,
    invoice_date TIMESTAMP NOT NULL,
    due_date TIMESTAMP,
    invoice_amount DECIMAL(12,2) NOT NULL,
    tax_amount DECIMAL(12,2),
    net_amount DECIMAL(12,2),
    amount_paid DECIMAL(12,2),
    amount_due DECIMAL(12,2),
    status VARCHAR(50) NOT NULL,
    matching_status VARCHAR(50),
    discrepancy_notes TEXT,
    tenant_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
    FOREIGN KEY (receipt_id) REFERENCES receipts(id)
);

-- Indexes
CREATE INDEX idx_vendors_tenant ON vendors(tenant_id);
CREATE INDEX idx_pr_tenant ON purchase_requests(tenant_id);
CREATE INDEX idx_po_tenant ON purchase_orders(tenant_id);
CREATE INDEX idx_po_vendor ON purchase_orders(vendor_id);
CREATE INDEX idx_receipt_tenant ON receipts(tenant_id);
CREATE INDEX idx_invoice_tenant ON invoices(tenant_id);
CREATE INDEX idx_invoice_vendor ON invoices(vendor_id);
```

## Integration with docker-compose.yml

```yaml
  # Procurement Service
  procurement-service:
    build: ../erp-service-procurement
    container_name: erp-procurement-service
    ports:
      - "8087:8087"
    environment:
      - POSTGRES_USER=${POSTGRES_USER}
      - POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/erp_db
    depends_on:
      - postgres
      - kafka
    networks:
      - erp-network
```

## Key Features Implemented

1. **Purchase Request Management** - Create, approve, and convert PRs to POs
2. **Purchase Order Processing** - Create POs, track status, manage line items
3. **Goods Receipt** - Record received items with quality inspection
4. **Invoice Processing** - Receive vendor invoices
5. **Three-Way Matching** - Automatic validation of PO-Receipt-Invoice match
6. **Vendor Management** - Maintain vendor master with performance ratings
7. **Multi-tenancy** - Full support for multiple organizations
8. **Event-driven** - Kafka integration for inter-service communication
9. **Audit Trail** - Created/Updated timestamps for compliance
10. **Discrepancy Management** - Track and manage mismatches

## Next Steps

1. Create the folder structure
2. Add all Java files following the package structure
3. Add database migration scripts
4. Build: `mvn clean install`
5. Update docker-compose.yml with Procurement service
6. Run: `docker-compose up`

