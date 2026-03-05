package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.Visita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VisitaRepository extends JpaRepository<Visita, Long> {
    List<Visita> findByPropiedadId_propiedadOrderByFechaDescHoraDesc(Long propiedadId);

    Optional<Visita> findByIdAndPropiedadUsuarioId(Long visitaId, Long usuarioId);
}
