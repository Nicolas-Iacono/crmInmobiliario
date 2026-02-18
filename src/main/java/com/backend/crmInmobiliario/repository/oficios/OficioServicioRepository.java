package com.backend.crmInmobiliario.repository.oficios;

import com.backend.crmInmobiliario.entity.oficios.OficioServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OficioServicioRepository extends JpaRepository<OficioServicio, Long> {
    List<OficioServicio> findByProveedorId(Long proveedorId);
}
