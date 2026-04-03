package com.example.erp_service_ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
public class ErpServiceAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErpServiceAiApplication.class, args);
	}

}
