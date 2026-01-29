package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.ContratoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContratoAlertaRepository extends JpaRepository<ContratoAlerta, Long> {
    Optional<ContratoAlerta> findByContratoIdAndUsuarioId(Long contratoId, Long usuarioId);

    List<ContratoAlerta> findByUsuarioIdAndVistoFalseAndNoMostrarFalse(Long usuarioId);
}
