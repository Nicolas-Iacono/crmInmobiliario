package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Recibo;
import com.backend.crmInmobiliario.entity.TransferStatus;
import com.backend.crmInmobiliario.repository.projections.ReciboSyncProjection;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReciboRepository extends JpaRepository<Recibo,Long> {

    @Query("SELECT r FROM Recibo r LEFT JOIN FETCH r.impuestos WHERE r.id = :id")
    Optional<Recibo> findReciboByIdWithImpuestos(@Param("id") Long id);


    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Recibo r where r.contrato.id = :idContrato")
    int deleteByContratoId(@Param("idContrato") Long idContrato);

//    @Query("""
//           SELECT r FROM Recibo r
//           JOIN FETCH r.contrato c
//           WHERE c.usuario.id = :userId
//           """)
//    List<Recibo> findByContratoUsuarioId(@Param("userId") Long userId);

    @Query("""
           SELECT r FROM Recibo r
           JOIN FETCH r.contrato c
           WHERE c.usuario.id = :userId
             AND r.estado = :estado
           """)
    List<Recibo> findByContratoUsuarioIdAndEstado(@Param("userId") Long userId,
                                                  @Param("estado") Boolean estado);


    @Query("""
     SELECT r FROM Recibo r
     JOIN FETCH r.contrato c
     WHERE c.usuario.id = :userId
       AND (:estado IS NULL OR r.estado = :estado)
       AND (:contratoId IS NULL OR c.id = :contratoId)
       AND (:q IS NULL OR LOWER(c.nombreContrato) LIKE LOWER(CONCAT('%', :q, '%')))
     ORDER BY r.fechaEmision DESC
  """)
    List<Recibo> search(
            @Param("userId") Long userId,
            @Param("estado") Boolean estado,          // true, false o null
            @Param("contratoId") Long contratoId,     // exacto o null
            @Param("q") String q                      // nombre contiene o null
    );


    @Query("""
        SELECT r
        FROM Recibo r
        WHERE r.contrato.inquilino.usuario.id = :userId
    """)
    List<Recibo> findByInquilinoUsuarioId(@Param("userId") Long userId);


    @Query("""
    SELECT r FROM Recibo r
    JOIN r.contrato c
    JOIN c.inquilino i
    WHERE i.id = :inquilinoId
    """)
    List<Recibo> findByInquilinoId(@Param("inquilinoId") Long inquilinoId);

        @Query("""
    SELECT r FROM Recibo r
    LEFT JOIN FETCH r.contrato c
    LEFT JOIN FETCH c.inquilino i
    LEFT JOIN FETCH r.impuestos
    WHERE i.id = :inquilinoId
    """)
    List<Recibo> findByInquilinoIdConTodo(@Param("inquilinoId") Long inquilinoId);

    // conteos útiles
    long countByContratoUsuarioId(Long userId);

    long countByContratoUsuarioIdAndEstado(Long userId, Boolean estado);

    @Query("""
    SELECT DISTINCT r
    FROM Recibo r
    JOIN FETCH r.contrato c
    LEFT JOIN FETCH r.impuestos i
    LEFT JOIN FETCH c.inquilino i2
    LEFT JOIN FETCH c.propietario p
    LEFT JOIN FETCH c.propiedad pr
    LEFT JOIN FETCH c.usuario u
    WHERE c.id = :contratoId
""")
    List<Recibo> findByContratoIdConTodo(@Param("contratoId") Long contratoId);


    List<Recibo> findByContratoUsuarioId(Long contratoId);

    @Query("""
    SELECT COALESCE(SUM(r.montoTotal), 0)
    FROM Recibo r
    JOIN r.contrato c
    JOIN c.inquilino i
    WHERE c.usuario.id = :userId
    AND (
        LOWER(i.nombre) LIKE LOWER(CONCAT('%', :nombrePersona, '%'))
        OR LOWER(i.apellido) LIKE LOWER(CONCAT('%', :nombrePersona, '%'))
    )
    AND r.estado = false
""")
    Double findSaldoPendiente(@Param("userId") Long userId,
                              @Param("nombrePersona") String nombrePersona);


    @Query("""
    SELECT r FROM Recibo r
    JOIN r.contrato c
    WHERE c.usuario.id = :userId
""")
    List<Recibo> findByUsuarioId(@Param("userId") Long userId);

    @Query("""
    SELECT r
    FROM Recibo r
    JOIN FETCH r.contrato c
    LEFT JOIN FETCH c.inquilino
    WHERE c.usuario.id = :userId
""")
    List<Recibo> findByUsuarioIdConContrato(@Param("userId") Long userId);

    @Query("""
    select r
    from Recibo r
    where r.contrato.usuario.id = :userId
      and lower(concat(r.contrato.inquilino.nombre, ' ', r.contrato.inquilino.apellido)) like lower(concat('%', :nombrePersona, '%'))
      and r.estado = false
""")
    List<Recibo> findPendientesByPersona(@Param("userId") Long userId,
                                         @Param("nombrePersona") String nombrePersona);


    @Query("""
    select coalesce(sum(r.montoTotal), 0)
    from Recibo r
    join r.contrato c
    where c.usuario.id = :userId
      and r.estado = false
""")
    BigDecimal findSaldoPendienteGlobal(@Param("userId") Long userId);

    @Query("""
    select count(r)
    from Recibo r
    join r.contrato c
    where c.usuario.id = :userId
      and r.estado = false
""")
    long countPendientesGlobal(@Param("userId") Long userId);


    @Query("""
    SELECT r FROM Recibo r
    LEFT JOIN FETCH r.impuestos
    WHERE r.contrato.id = :id
""")
    List<Recibo> findRecibosByContratoId(Long id);


    @Query("select r.contrato.id from Recibo r where r.id = :reciboId")
    Long findContratoIdByReciboId(@Param("reciboId") Long reciboId);

    @Modifying
    @Query("update Recibo r set r.estado = :pagado where r.id = :reciboId")
    int updateEstado(@Param("reciboId") Long reciboId, @Param("pagado") boolean pagado);

    @Query("""
    SELECT r FROM Recibo r
    JOIN FETCH r.contrato c
    LEFT JOIN FETCH c.inquilino
    LEFT JOIN FETCH c.propiedad
    LEFT JOIN FETCH c.propietario
    WHERE r.id = :reciboId
""")
    Recibo findByIdConContrato(@Param("reciboId") Long reciboId);


    public interface ReciboEstadoProjection {
        Long getId();
        Boolean getEstado();
        Long getContratoId();
        String getNombreContrato();
    }

    @Query("""
        SELECT
            r.id as idRecibo,
            c.id as contratoId,
            u.id as userId,
            c.nombreContrato as nombreContrato,
            r.numeroRecibo as numeroRecibo,
            r.montoTotal as montoTotal,
            r.periodo as periodo,
            r.concepto as concepto,
            CASE WHEN r.fechaEmision IS NOT NULL THEN CAST(r.fechaEmision as string) ELSE NULL END as fechaEmision,
            CASE WHEN r.fechaVencimiento IS NOT NULL THEN CAST(r.fechaVencimiento as string) ELSE NULL END as fechaVencimiento,
            r.estado as estado
        FROM Recibo r
        JOIN r.contrato c
        JOIN c.usuario u
        WHERE r.id = :reciboId
    """)
    ReciboSyncProjection findSyncData(@Param("reciboId") Long reciboId);

    @Query("""
        select r.id as id,
               r.estado as estado,
               c.id as contratoId,
               c.nombreContrato as nombreContrato
        from Recibo r
        join r.contrato c
        where r.id = :reciboId
    """)
    Optional<ReciboEstadoProjection> findEstadoProjectionById(@Param("reciboId") Long reciboId);


    Optional<Recibo> findByMpExternalReference(String mpExternalReference);


    public interface ReciboPagoProjection {
        Long getId();
        Boolean getEstado();
        Long getContratoId();
        TransferStatus getTransferStatus(); // "NONE", "PENDING", "APPROVED", "REJECTED"
    }

    @Query("""
select r.id as id,
       r.estado as estado,
       c.id as contratoId,
       r.transferStatus as transferStatus
from Recibo r
join r.contrato c
where r.id = :reciboId
""")
    Optional<ReciboPagoProjection> findPagoProjection(@Param("reciboId") Long reciboId);


    @Modifying
    @Query("""
update Recibo r
set r.transferStatus = :status
where r.id = :reciboId
""")
    int updateTransferStatus(@Param("reciboId") Long reciboId,
                             @Param("status") TransferStatus status);



    @Modifying
    @Query("""
update Recibo r
set r.estado = :nuevoEstado,
    r.paidAt = :paidAt
where r.id = :reciboId
""")
    int updateEstadoYPaidAt(@Param("reciboId") Long reciboId,
                            @Param("nuevoEstado") boolean nuevoEstado,
                            @Param("paidAt") LocalDateTime paidAt);



    @Modifying
    @Query("""
update Recibo r
set r.transferStatus = :status,
    r.transferAlias = null,
    r.transferAmount = null,
    r.transferNotifiedAt = null,
    r.transferReference = null,
    r.transferComprobanteUrl = null,
    r.transferNote = null
where r.id = :reciboId
""")
    int resetTransferencia(@Param("reciboId") Long reciboId,
                           @Param("status") TransferStatus status);


    @Modifying
    @Query("""
update Recibo r
set r.transferStatus = :approved,
    r.estado = true
where r.id = :reciboId
  and r.transferStatus = :pending
""")
    int approveTransferAndMarkPaid(@Param("reciboId") Long reciboId,
                                   @Param("pending") TransferStatus pending,
                                   @Param("approved") TransferStatus approved);


    @Modifying
    @Query("""
update Recibo r
set r.transferStatus = :rejected
where r.id = :reciboId
  and r.transferStatus = :pending
""")
    int rejectTransfer(@Param("reciboId") Long reciboId,
                       @Param("pending") TransferStatus pending,
                       @Param("rejected") TransferStatus rejected);

    @Query("""
select c.usuario.id
from Recibo r
join r.contrato c
where r.id = :reciboId
""")
    Long findOwnerUserIdByReciboId(@Param("reciboId") Long reciboId);

}
