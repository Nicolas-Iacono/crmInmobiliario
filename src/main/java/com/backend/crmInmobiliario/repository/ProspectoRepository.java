package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.Prospecto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProspectoRepository extends JpaRepository<Prospecto, Long> {
    List<Prospecto> findByUsuarioId(Long usuarioId);

    List<Prospecto> findByUsuarioIdNot(Long usuarioId);

    Optional<Prospecto> findByIdAndUsuarioId(Long id, Long usuarioId);
}
