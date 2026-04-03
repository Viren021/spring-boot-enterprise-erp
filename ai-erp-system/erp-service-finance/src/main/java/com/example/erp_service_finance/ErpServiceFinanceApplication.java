package com.example.erp_service_finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.example.erp_service_finance.model")
public class ErpServiceFinanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErpServiceFinanceApplication.class, args);
	}

}
