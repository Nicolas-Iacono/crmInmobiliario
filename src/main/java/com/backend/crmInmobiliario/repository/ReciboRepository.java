package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Recibo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReciboRepository extends JpaRepository<Recibo,Long> {

    @Query("SELECT r FROM Recibo r LEFT JOIN FETCH r.impuestos WHERE r.id = :id")
    Optional<Recibo> findReciboByIdWithImpuestos(@Param("id") Long id);


    @Modifying
    @Transactional
    @Query("DELETE FROM Recibo r WHERE r.contrato.id = :idContrato")
    void deleteByContratoId(@Param("idContrato") Long idContrato);

//    List<Recibo> findByContratoId(Long contratoId);
}
