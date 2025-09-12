package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Recibo;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface ReciboRepository extends JpaRepository<Recibo,Long> {

    @Query("SELECT r FROM Recibo r LEFT JOIN FETCH r.impuestos WHERE r.id = :id")
    Optional<Recibo> findReciboByIdWithImpuestos(@Param("id") Long id);


    @Modifying
    @Transactional
    @Query("DELETE FROM Recibo r WHERE r.contrato.id = :idContrato")
    void deleteByContratoId(@Param("idContrato") Long idContrato);

    @Query("""
           SELECT r FROM Recibo r
           JOIN FETCH r.contrato c
           WHERE c.usuario.id = :userId
           """)
    List<Recibo> findByContratoUsuarioId(@Param("userId") Long userId);

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

    // conteos útiles
    long countByContratoUsuarioId(Long userId);

    long countByContratoUsuarioIdAndEstado(Long userId, Boolean estado);




//    List<Recibo> findByContratoId(Long contratoId);
}
