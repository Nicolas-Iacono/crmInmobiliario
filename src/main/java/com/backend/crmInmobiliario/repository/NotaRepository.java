package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.DTO.salida.NotaSalidaDto;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Nota;
import com.backend.crmInmobiliario.entity.VisibilidadNota;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotaRepository extends JpaRepository<Nota,Long> {


    @Modifying
    @Transactional
    @Query("DELETE FROM Nota n WHERE n.contrato.id = :idContrato")
    void deleteByContratoId(@Param("idContrato") Long idContrato);

    @EntityGraph(attributePaths = {"imagenes"})
    @Query("""
    select n from Nota n
    where n.contrato.id = :contratoId
    order by n.fechaCreacion desc
""")
    List<Nota> findByContratoId(@Param("contratoId") Long contratoId);


    // ✅ Bandeja inmobiliaria/admin
    @EntityGraph(attributePaths = {"imagenes"})
    @Query("""
        select n from Nota n
        join n.contrato c
        where c.usuario.id = :userId
        order by n.fechaCreacion desc
    """)
    List<Nota> findNotasParaInmobiliaria(@Param("userId") Long userId);

    // ✅ Bandeja propietario
    @EntityGraph(attributePaths = {"imagenes"})
    @Query("""
        select n from Nota n
        join n.contrato c
        join c.propietario p
        join p.usuarioCuentaPropietario up
        where up.id = :userId
        order by n.fechaCreacion desc
    """)
    List<Nota> findNotasParaPropietario(@Param("userId") Long userId);

    // ✅ Bandeja inquilino
    @EntityGraph(attributePaths = {"imagenes"})
    @Query("""
        select n from Nota n
        join n.contrato c
        join c.inquilino i
        join i.usuarioCuentaInquilino ui
        where ui.id = :userId
        order by n.fechaCreacion desc
    """)
    List<Nota> findNotasParaInquilino(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"imagenes"})
    @Query("select n from Nota n where n.id = :id")
    Optional<Nota> findByIdWithImagenes(@Param("id") Long id);


    List<Nota> findByContratoIdAndVisibilidad(Long contratoId, VisibilidadNota visibilidad);

    // si permitís SOLO_INMOBILIARIA del inquilino:
    List<Nota> findByContratoIdAndVisibilidadAndAutorUsuarioId(Long contratoId, VisibilidadNota visibilidad, Long autorId);



    List<Nota> findByContratoIdOrderByFechaCreacionDesc(Long contratoId);

    List<Nota> findByContratoIdAndVisibilidadOrderByFechaCreacionDesc(
            Long contratoId, VisibilidadNota visibilidad
    );

    @Query("""
        select n from Nota n
        where n.contrato.id = :contratoId
          and (
                n.visibilidad = com.backend.crmInmobiliario.entity.VisibilidadNota.PUBLICA
                or (n.visibilidad = com.backend.crmInmobiliario.entity.VisibilidadNota.SOLO_INMOBILIARIA
                    and n.autorUsuario.id = :autorId)
              )
        order by n.fechaCreacion desc
    """)
    List<Nota> findVisiblesParaInquilino(
            @Param("contratoId") Long contratoId,
            @Param("autorId") Long autorId
    );

}
