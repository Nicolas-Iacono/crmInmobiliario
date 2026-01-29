package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.Prospecto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProspectoRepository extends JpaRepository<Prospecto, Long> {
    List<Prospecto> findByUsuarioId(Long usuarioId);

    List<Prospecto> findByUsuarioIdNot(Long usuarioId);
}
