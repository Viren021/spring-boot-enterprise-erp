package com.example.erp_service_sales.kafka;

import com.example.erp_service_sales.config.TenantContext;
import com.example.erp_service_sales.entity.Invoice;
import com.example.erp_service_sales.entity.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
public class FinanceIntegrationProducer {
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;
    public FinanceIntegrationProducer(KafkaTemplate<String, String> kafka, ObjectMapper mapper) {
        this.kafka = kafka; this.mapper = mapper;
    }
    public void invoice(Invoice invoice) { publish("SALES_INVOICE", invoice.getInvoiceNumber(), invoice.getTotal(),
            invoice.getDueDate(), "Sales invoice " + invoice.getInvoiceNumber(), "CREDIT"); }
    public void payment(Payment payment) { publish("SALES_PAYMENT", String.valueOf(payment.getId()), payment.getAmount(),
            LocalDate.now(), "Sales payment " + payment.getId(), "CREDIT"); }
    private void publish(String type, String document, BigDecimal amount, LocalDate date, String description, String direction) {
        if (amount == null || amount.signum() <= 0) throw new IllegalStateException("Cannot publish " + type + " without a positive amount");
        String tenant = TenantContext.required();
        String id = UUID.nameUUIDFromBytes((tenant + ":" + type + ":" + document).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
        try {
            kafka.send("finance-integration-events", mapper.writeValueAsString(Map.of("eventId", id, "tenantId", tenant,
                    "eventType", type, "sourceDocumentId", document, "amount", amount, "eventDate", date,
                    "description", description, "direction", direction)));
        } catch (Exception e) { throw new IllegalStateException("Unable to serialize finance integration event", e); }
    }
}
