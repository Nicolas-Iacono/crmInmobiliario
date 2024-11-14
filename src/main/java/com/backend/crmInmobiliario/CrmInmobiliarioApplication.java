package com.backend.crmInmobiliario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CrmInmobiliarioApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrmInmobiliarioApplication.class, args);
	}

}
