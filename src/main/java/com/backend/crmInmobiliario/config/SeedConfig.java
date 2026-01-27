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
            upsert(repo, "PLAN-BARATO", "planBarato", new BigDecimal("20"), 5, true);
            upsert(repo, "PLAN-PRO", "Pro", new BigDecimal("30000"), 10, true);
            upsert(repo, "PLAN-PROF", "Pro+", new BigDecimal("35000"), 20, true);
            upsert(repo, "PLAN-SUP", "Superior", new BigDecimal("45000"), 30, true);
        };
    }

    private void upsert(PlanRepository repo, String code, String name, BigDecimal price, int limit, boolean active){
        var p = repo.findByCode(code).orElseGet(Plan::new);
        p.setCode(code);
        p.setName(name);
        p.setPriceArs(price);
        p.setContractLimit(limit);
        p.setActive(active);
        repo.save(p);
    }
}
