package com.example.erp_service_finance.config;

// Notice: The HR import is completely removed!
import com.example.erp_service_finance.config.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantSchemaResolver implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        // It uses the TenantContext from this very same package
        String tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return "tenant_" + tenantId; // Transforms "tata_motors" -> "tenant_tata_motors"
        }
        return "public"; // Default to public if no tenant is found (e.g. during startup)
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}