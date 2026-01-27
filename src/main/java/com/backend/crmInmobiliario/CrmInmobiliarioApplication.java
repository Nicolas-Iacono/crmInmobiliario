package com.backend.crmInmobiliario;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class CrmInmobiliarioApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrmInmobiliarioApplication.class, args);
	}

//	@PostConstruct
//	public void init() {
//		// ✅ ESTA ES LA CORRECTA
//		// Al usar UTC, 00:00 siempre es 00:00, no hay restas de horas.
//		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//	}

}
