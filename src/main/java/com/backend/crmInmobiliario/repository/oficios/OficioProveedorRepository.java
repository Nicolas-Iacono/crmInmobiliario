package com.backend.crmInmobiliario.repository.oficios;

import com.backend.crmInmobiliario.entity.oficios.OficioProveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OficioProveedorRepository extends JpaRepository<OficioProveedor, Long> {
    Optional<OficioProveedor> findByUsuarioId(Long usuarioId);
    boolean existsByUsuarioId(Long usuarioId);
}

