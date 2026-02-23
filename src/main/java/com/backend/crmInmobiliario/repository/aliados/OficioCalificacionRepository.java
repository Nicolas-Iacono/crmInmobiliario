package com.backend.crmInmobiliario.repository.aliados;

import com.backend.crmInmobiliario.entity.OficioCalificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OficioCalificacionRepository extends JpaRepository<OficioCalificacion, Long> {
    Optional<OficioCalificacion> findByProveedorIdAndInmobiliariaId(Long proveedorId, Long inmobiliariaId);
    long countByProveedorId(Long proveedorId);

    @Query("select avg(c.estrellas) from OficioCalificacion c where c.proveedor.id = :proveedorId")
    Double avgEstrellas(@Param("proveedorId") Long proveedorId);
}
