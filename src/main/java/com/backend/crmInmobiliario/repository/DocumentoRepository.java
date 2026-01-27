package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    @Query("SELECT d FROM Documento d WHERE d.contrato.id = :contratoId")
    List<Documento> buscarPorContrato(@Param("contratoId") Long contratoId);

    List<Documento> findByInquilinoId(Long inquilinoId);
    List<Documento> findByPropietarioId(Long propietarioId);
    List<Documento> findByGaranteId(Long garanteId);
}