package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.impuestos.ContratoImpuestoTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratoImpuestoTemplateRepository extends JpaRepository<ContratoImpuestoTemplate, Long> {
    List<ContratoImpuestoTemplate> findByContratoIdAndActivoTrue(Long contratoId);
}
