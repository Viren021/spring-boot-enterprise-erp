package com.example.erp_service_inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ErpServiceInventoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErpServiceInventoryApplication.class, args);
	}

}
