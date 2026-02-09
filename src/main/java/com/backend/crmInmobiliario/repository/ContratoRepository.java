package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.DTO.entrada.contrato.ContratoEventoRow;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContractEventDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoCardDto;
import com.backend.crmInmobiliario.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {


    Optional<Contrato> findByInquilinoAndActivoTrue(Inquilino inquilino);

//    @Query("SELECT c FROM Contrato c WHERE c.usuario.username = :username")
//    List<Contrato> findContratosByUsername(@Param("username") String username);

    @EntityGraph(attributePaths = {"inquilino", "propiedad", "propiedad.propietario", "garantes", "usuario"})
    List<Contrato> findContratosByUsuarioUsername(String username);

    List<Contrato> findByUsuario(Usuario usuario);

    @Query("SELECT c FROM Contrato c ORDER BY c.publicDate DESC")
    Page<Contrato> findLatestContratos(Pageable pageable);

    @Query("SELECT c FROM Contrato c LEFT JOIN FETCH c.recibos")
    List<Contrato> findAllWithRecibos();

    @Query("SELECT c FROM Contrato c LEFT JOIN FETCH c.garantes WHERE c.id = :id")
    Contrato findContratoByIdWithGarantes(@Param("id") Long id);



    @Query("select count(c) from Contrato c where c.usuario.id=:usuarioId and c.activo=true")
    long countActivosByUsuario(@Param("usuarioId") Long usuarioId);

    Optional<Contrato> findByInquilinoId(Long inquilinoId);


    @Query("""
    SELECT DISTINCT c FROM Contrato c
    LEFT JOIN FETCH c.propietario
    LEFT JOIN FETCH c.inquilino
    LEFT JOIN FETCH c.propiedad
    LEFT JOIN FETCH c.usuario
    WHERE LOWER(c.nombreContrato) = LOWER(:nombre)
""")
    Optional<Contrato> findByNombreContratoCompleto(@Param("nombre") String nombre);



    @Query("SELECT DISTINCT c FROM Contrato c " +
            "JOIN FETCH c.propietario pr " +
            "JOIN FETCH c.propiedad p " +
            "LEFT JOIN FETCH p.imagenes " +
            "WHERE pr.email = :email")
    List<Contrato> findByPropietarioEmail(@Param("email") String email);

    @Query("SELECT c FROM Contrato c WHERE c.usuario.id = :userId ORDER BY c.id DESC")
    Page<Contrato> findLatestContratosByUsuarioId(@Param("userId") Long userId, Pageable pageable);

    List<Contrato> findByUsuarioId(Long userId);

    @Query("""
    SELECT c
    FROM Contrato c
    JOIN FETCH c.inquilino i
    JOIN FETCH c.propiedad pr
    WHERE c.usuario.id = :userId
""")
    List<Contrato> findByUsuarioIdConDetalle(@Param("userId") Long userId);

    int countByUsuarioId(Long userId);


    @Query("""
    SELECT c
    FROM Contrato c
    JOIN FETCH c.inquilino i
    JOIN FETCH c.propietario p
    JOIN FETCH c.propiedad pr
    WHERE c.usuario.id = :userId
""")
    List<Contrato> listarContratoCards(@Param("userId") Long userId);


    List<Contrato> findByNombreContratoContainingIgnoreCaseAndUsuarioId(String nombre, Long userId);

    Optional<Contrato> findByIdAndUsuarioId(Long id, Long usuarioId);



    @Query("""
SELECT c FROM Contrato c
WHERE c.usuario.id = :userId
  AND c.fecha_fin BETWEEN :hoy AND :limite
ORDER BY c.fecha_fin ASC
""")
    List<Contrato> findVencimientosProximos(
            @Param("userId") Long userId,
            @Param("hoy") LocalDate hoy,
            @Param("limite") LocalDate limite
    );


    @Query("SELECT c.usuario FROM Contrato c WHERE c.id = :idContrato")
    Usuario findUsuarioByContratoId(@Param("idContrato") Long idContrato);


    @Query("""
       SELECT c FROM Contrato c
       LEFT JOIN FETCH c.propietario
       LEFT JOIN FETCH c.inquilino
       LEFT JOIN FETCH c.garantes
       LEFT JOIN FETCH c.recibos r
       LEFT JOIN FETCH r.impuestos
       LEFT JOIN FETCH c.notas
       LEFT JOIN FETCH c.propiedad
       WHERE c.id = :id
       """)
    Optional<Contrato> findByIdWithAllRelations(@Param("id") Long id);


    @Query("""
    SELECT c FROM Contrato c
    LEFT JOIN FETCH c.inquilino i
    LEFT JOIN FETCH c.propietario p
    LEFT JOIN FETCH c.propiedad pr
    LEFT JOIN FETCH pr.imagenes img
    LEFT JOIN FETCH c.garantes g
    LEFT JOIN FETCH c.recibos r
    LEFT JOIN FETCH r.impuestos imp
    LEFT JOIN FETCH c.notas n
    WHERE c.id = :id
""")
    Contrato findContratoCompletoById(Long id);


    @Query("""
    SELECT c FROM Contrato c
    LEFT JOIN FETCH c.inquilino i
    LEFT JOIN FETCH c.propietario p
    LEFT JOIN FETCH c.propiedad pr
    WHERE c.id = :id
""")
    Contrato findContratoBase(@Param("id") Long id);


    @Query("""
    SELECT c 
    FROM Contrato c
    JOIN FETCH c.propietario p
    JOIN FETCH c.inquilino i
    JOIN FETCH c.propiedad pr
    JOIN FETCH c.usuario u
    WHERE c.id = :id
""")
    Contrato findContratoPdfBase(@Param("id") Long id);


    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);


    @Query("""
    SELECT c
    FROM Contrato c
    LEFT JOIN FETCH c.garantes
    LEFT JOIN FETCH c.recibos
    LEFT JOIN FETCH c.notas
    WHERE c.id = :id
""")
    Optional<Contrato> findByIdConFetchCompleto(@Param("id") Long id);

    @Query("""
SELECT c
FROM Contrato c
LEFT JOIN FETCH c.garantes
WHERE c.id = :id
""")
    Contrato findConGarantes(Long id);

    @Query("""
SELECT c
FROM Contrato c
LEFT JOIN FETCH c.notas
WHERE c.id = :id
""")
    Contrato findConNotas(@Param("id") Long id);

    @Query("SELECT c.id FROM Contrato c WHERE c.usuario.id = :userId")
    List<Long> findIdsByUsuarioId(@Param("userId") Long userId);

    @Query("SELECT c.id FROM Contrato c")
    List<Long> findAllIds();

/// calendario eventos

    @Query("""
select new com.backend.crmInmobiliario.DTO.entrada.contrato.ContratoEventoRow(
  c.id, c.nombreContrato, c.fecha_inicio, c.fecha_fin, c.actualizacion
)
from Contrato c
where c.usuario.id = :usuarioId
  and c.fecha_fin >= :from
  and c.fecha_inicio <= :to
""")
    List<ContratoEventoRow> findEventosRow(Long usuarioId, LocalDate from, LocalDate to);


    @Query("""
    SELECT c
    FROM Contrato c
    WHERE c.estado = :estado
      AND c.fecha_fin < :fecha
""")
    List<Contrato> findVencidosAntesDe(
            @Param("estado") EstadoContrato estado,
            @Param("fecha") LocalDate fecha
    );


    @Query("""
    SELECT c
    FROM Contrato c
    WHERE c.estado = :estado
      AND c.fecha_fin BETWEEN :desde AND :hasta
""")
    List<Contrato> findVencidosEntreFechas(
            @Param("estado") EstadoContrato estado,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );

    List<Contrato> findByActivoTrue();

    interface ContratoAlertaRow {
        Long getContratoId();
        String getNombreContrato();
        java.time.LocalDate getFechaInicio();
        java.time.LocalDate getFechaFin();
        Integer getDuracion();
        Boolean getActivo();
        String getEstado();
        Long getUserId();
    }

    @Query("""
        select c.id as contratoId,
               c.nombreContrato as nombreContrato,
               c.fecha_inicio as fechaInicio,
               c.fecha_fin as fechaFin,
               c.duracion as duracion,
               c.activo as activo,
               c.estado as estado,
               u.id as userId
        from Contrato c
        join c.usuario u
        where c.activo = true
        """)
    List<ContratoAlertaRow> findAlertasVencimientoActivos();
}

