package com.backend.crmInmobiliario.repository.aliados;

import com.backend.crmInmobiliario.entity.OficioProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OficioProveedorRepository extends JpaRepository<OficioProveedor, Long>,
        JpaSpecificationExecutor<OficioProveedor> {
    Optional<OficioProveedor> findByUsuarioId(Long userId);

    @Query("""
        select distinct p
        from OficioProveedor p
        left join fetch p.imagenPerfil
        """)
    List<OficioProveedor> findAllConImagenPerfil();


    @Query("""
        select distinct p
        from OficioProveedor p
        left join fetch p.categorias
        left join fetch p.imagenPerfil
    """)
    List<OficioProveedor> findAllWithCategoriasAndImagen();

    @Query("""
   select distinct p
   from OficioProveedor p
   left join fetch p.resenas r
   left join fetch r.usuario
   where p.id = :id
""")
    Optional<OficioProveedor> findByIdWithResenas(@Param("id") Long id);
}
