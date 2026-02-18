package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.ReciboAlerta;
import com.backend.crmInmobiliario.entity.TipoAlertaRecibo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReciboAlertaRepository extends JpaRepository<ReciboAlerta, Long> {
    Optional<ReciboAlerta> findByReciboIdAndUsuarioIdAndTipo(Long reciboId, Long usuarioId, TipoAlertaRecibo tipo);

    List<ReciboAlerta> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    @Query("""
            select ra
            from ReciboAlerta ra
            where ra.tipo = :tipo
              and ra.visto = false
              and ra.noMostrar = false
              and (ra.ultimaNotificacion is null or ra.ultimaNotificacion < :hoy)
            """)
    List<ReciboAlerta> findPendientesParaNotificar(@Param("tipo") TipoAlertaRecibo tipo, @Param("hoy") LocalDate hoy);



    @Modifying
    @Query("delete from ReciboAlerta ra where ra.recibo.id = :reciboId")
    int deleteByReciboId(@Param("reciboId") Long reciboId);


    @Modifying
    @Query("""
        delete from ReciboAlerta ra
        where ra.recibo.contrato.id = :contratoId
    """)
    int deleteByContratoId(@Param("contratoId") Long contratoId);
}
