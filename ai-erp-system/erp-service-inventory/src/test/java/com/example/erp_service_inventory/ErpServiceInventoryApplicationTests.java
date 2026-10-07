package com.example.erp_service_inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
class ErpServiceInventoryApplicationTests {

	@Test
	void contextLoads() {
	}

}
