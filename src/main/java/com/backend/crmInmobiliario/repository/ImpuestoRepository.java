package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.Impuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImpuestoRepository extends JpaRepository<Impuesto, Long> {
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Impuesto i where i.recibo.id in (select r.id from Recibo r where r.contrato.id = :idContrato)")
    void deleteByContratoId(@Param("idContrato") Long idContrato);
}
