package com.backend.crmInmobiliario.repository.oficios;

import com.backend.crmInmobiliario.entity.oficios.OficioCalificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OficioCalificacionRepository extends JpaRepository<OficioCalificacion, Long> {
    Optional<OficioCalificacion> findByProveedorIdAndInmobiliariaId(Long proveedorId, Long inmobiliariaId);
    List<OficioCalificacion> findByProveedorId(Long proveedorId);
}
