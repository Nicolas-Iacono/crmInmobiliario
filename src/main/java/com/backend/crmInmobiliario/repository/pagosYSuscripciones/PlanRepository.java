package com.backend.crmInmobiliario.repository.pagosYSuscripciones;

import com.backend.crmInmobiliario.entity.planesYSuscripciones.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByCodeAndActiveTrue(String code);
    Optional<Plan> findByCode(String code);
    List<Plan> findAllByActiveTrueOrderByPriceArsAsc();
    Optional<Plan> findByExternalPlanId(String externalPlanId);
}
