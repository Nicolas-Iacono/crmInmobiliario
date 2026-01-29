package com.backend.crmInmobiliario.repository;


import com.backend.crmInmobiliario.entity.ContratoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContratoAlertaRepository extends JpaRepository<ContratoAlerta, Long> {
    Optional<ContratoAlerta> findByContratoIdAndUsuarioId(Long contratoId, Long usuarioId);

    List<ContratoAlerta> findByUsuarioIdAndVistoFalseAndNoMostrarFalse(Long usuarioId);


    @Modifying
    @Query("delete from ContratoAlerta ca where ca.contrato.id = :contratoId")
    void deleteByContratoId(@Param("contratoId") Long contratoId);
}
