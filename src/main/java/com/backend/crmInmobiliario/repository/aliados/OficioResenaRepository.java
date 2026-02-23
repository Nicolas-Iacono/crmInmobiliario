package com.backend.crmInmobiliario.repository.aliados;

import com.backend.crmInmobiliario.entity.OficioResena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OficioResenaRepository extends JpaRepository<OficioResena, Long> {

    Optional<OficioResena> findByProveedorIdAndUsuarioId(Long proveedorId, Long usuarioId);

    @Query("""
     select r from OficioResena r
     join fetch r.usuario u
     where r.proveedor.id = :proveedorId
     order by r.fechaCreacion desc
  """)
    List<OficioResena> findAllByProveedorIdConUsuario(Long proveedorId);

    @Query("select avg(r.calificacion) from OficioResena r where r.proveedor.id = :proveedorId")
    Double avgByProveedor(Long proveedorId);

    @Query("select count(r) from OficioResena r where r.proveedor.id = :proveedorId")
    Long countByProveedor(Long proveedorId);
}
