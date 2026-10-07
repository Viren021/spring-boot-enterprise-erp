package com.example.erp_service_inventory.kafka;

import com.example.erp_service_inventory.config.TenantContext;
import com.example.erp_service_inventory.entity.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
public class FinanceIntegrationProducer {
    private final KafkaTemplate<String, String> kafka; private final ObjectMapper mapper;
    public FinanceIntegrationProducer(KafkaTemplate<String, String> kafka, ObjectMapper mapper) { this.kafka=kafka; this.mapper=mapper; }
    public void publish(String type, String document, BigDecimal quantity, BigDecimal unitCost, String direction) {
        if (unitCost == null || unitCost.signum() <= 0)
            throw new IllegalStateException("Cannot publish " + type + ": valuation cost is unavailable");
        if (quantity == null || quantity.signum() <= 0) throw new IllegalStateException("Inventory quantity is unavailable");
        BigDecimal amount = quantity.multiply(unitCost);
        String tenant=TenantContext.getTenantId(); if(tenant==null||tenant.isBlank()) throw new IllegalStateException("Tenant context is required");
        String id=UUID.nameUUIDFromBytes((tenant+":"+type+":"+document).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
        try { kafka.send("finance-integration-events", mapper.writeValueAsString(Map.of("eventId",id,"tenantId",tenant,
            "eventType",type,"sourceDocumentId",document,"amount",amount,"eventDate",LocalDate.now(),
            "description","Inventory "+type.toLowerCase()+" "+document,"direction",direction))); }
        catch(Exception e){throw new IllegalStateException("Unable to publish inventory finance event",e);}
    }
}
