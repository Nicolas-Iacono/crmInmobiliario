package com.backend.crmInmobiliario.repository.aliados;

import com.backend.crmInmobiliario.entity.OficioServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OficioServicioRepository extends JpaRepository<OficioServicio, Long> {
    List<OficioServicio> findByProveedorId(Long proveedorId);

    Optional<OficioServicio> findByIdAndProveedorId(Long id, Long proveedorId);

    @Query("""
        select s from OficioServicio s
        join fetch s.proveedor p
        join fetch p.usuario u
        where s.id = :servicioId
    """)
    Optional<OficioServicio> findByIdWithProveedorUsuario(@Param("servicioId") Long servicioId);


    @Query("""
    select os
    from OficioServicio os
    join os.proveedor p
    join p.usuario u
    where u.id = :userId
      and os.activo = true
""")
    Optional<OficioServicio> findServicioActivoByUsuarioId(Long userId);

    @Query("""
        select s from OficioServicio s
        left join fetch s.imagenes
        where s.id = :servicioId
    """)
    Optional<OficioServicio> findByIdWithImagenes(@Param("servicioId") Long servicioId);


    @Query("""
        select distinct s
        from OficioServicio s
        join fetch s.proveedor p
        left join fetch s.imagenes
        where p.id = :proveedorId
        order by s.id desc
        """)
    List<OficioServicio> findServiciosByProveedorIdConImagenes(@Param("proveedorId") Long proveedorId);

}

