package com.example.erp_tenant_service.service;


import com.example.erp_tenant_service.entity.Tenant;
import com.example.erp_tenant_service.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate; // This lets us run raw SQL commands!

    @Transactional // If the schema creation fails, the database save rolls back automatically
    public Tenant createTenant(String companyName) {

        // 1. Generate a database-safe name (e.g., "Tata Motors" -> "tenant_tata_motors")
        String safeName = companyName.toLowerCase().replaceAll("[^a-z0-9]", "_");
        String schemaName = "tenant_" + safeName;

        if (tenantRepository.existsById(safeName)) {
            throw new RuntimeException("A tenant with this name already exists!");
        }

        // 2. Save the company to our public master list
        Tenant tenant = new Tenant();
        tenant.setTenantId(safeName);
        tenant.setCompanyName(companyName);
        tenant.setDbSchemaName(schemaName);
        Tenant savedTenant = tenantRepository.save(tenant);

        // 3. The Magic: Execute raw SQL to build the isolated schema in PostgreSQL
        String sql = "CREATE SCHEMA " + schemaName;
        jdbcTemplate.execute(sql);

        return savedTenant;
    }
    public java.util.Optional<Tenant> getTenantById(String tenantId) {
        return tenantRepository.findById(tenantId);
    }
}
