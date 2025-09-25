package com.backend.crmInmobiliario.config;

import com.backend.crmInmobiliario.entity.planesYSuscripciones.Plan;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.PlanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class SeedConfig {

    @Bean
    CommandLineRunner seedPlans(PlanRepository repo) {
        return args -> {
            upsert(repo, "FREE", "Free", BigDecimal.ZERO, 3, true);
            upsert(repo, "STARTER", "Starter", new BigDecimal("9"), 20, true);
            upsert(repo, "PRO", "Pro", new BigDecimal("24"), 45, true);
        };
    }

    private void upsert(PlanRepository repo, String code, String name, BigDecimal price, int limit, boolean active){
        var p = repo.findByCode(code).orElseGet(Plan::new);
        p.setCode(code);
        p.setName(name);
        p.setPriceUsd(price);
        p.setContractLimit(limit);
        p.setActive(active);
        repo.save(p);
    }
}
