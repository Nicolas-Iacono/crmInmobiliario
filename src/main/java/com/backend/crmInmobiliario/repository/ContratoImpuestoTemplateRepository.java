package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.impuestos.ContratoImpuestoTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContratoImpuestoTemplateRepository extends JpaRepository<ContratoImpuestoTemplate, Long> {
    List<ContratoImpuestoTemplate> findByContratoId(Long contratoId);
    List<ContratoImpuestoTemplate> findByContratoIdAndActivoTrue(Long contratoId);
    @Modifying
    @Query("delete from ContratoImpuestoTemplate t where t.contrato.id = :contratoId")
    void deleteByContratoId(@Param("contratoId") Long contratoId);
}
