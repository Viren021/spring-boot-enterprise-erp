package com.example.erp_service_procurement;

import com.example.erp_service_procurement.entity.ApprovalPolicy;
import com.example.erp_service_procurement.repository.ApprovalPolicyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
class ApprovalPolicyIntegrationTest {
    @Autowired ApprovalPolicyRepository policies;

    @Test
    void persistsAmountAndRolePolicy() {
        ApprovalPolicy policy = new ApprovalPolicy();
        policy.setTenantId("test-tenant"); policy.setDocumentType("PURCHASE_ORDER");
        policy.setMinAmount(BigDecimal.TEN); policy.setRole("MANAGER");
        assertThat(policies.save(policy).getId()).isNotNull();
    }
}
