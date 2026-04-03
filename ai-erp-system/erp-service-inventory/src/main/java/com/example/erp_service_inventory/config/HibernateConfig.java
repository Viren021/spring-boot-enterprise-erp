package com.example.erp_service_inventory.config;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class HibernateConfig {

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            MultiTenantConnectionProvider<String> multiTenantConnectionProvider,
            CurrentTenantIdentifierResolver currentTenantIdentifierResolver,
            JpaProperties jpaProperties) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.erp_service_inventory"); // <--- Scans for your Entities
        em.setJpaVendorAdapter(jpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>(jpaProperties.getProperties());

        // --- THE FIX: USING RAW STRINGS INSTEAD OF CONSTANTS ---

        // 1. Tell Hibernate to use SCHEMA-based multi-tenancy
        properties.put("hibernate.multiTenancy", "SCHEMA");

        // 2. Plug in our custom Connection Provider (The Switcher)
        properties.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider);

        // 3. Plug in our custom Resolver (The Identifier)
        properties.put("hibernate.tenant_identifier_resolver", currentTenantIdentifierResolver);

        // 4. Standard Hibernate settings
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.hbm2ddl.auto", "update"); // HR Service should NOT create tables

        em.setJpaPropertyMap(properties);
        return em;
    }
}