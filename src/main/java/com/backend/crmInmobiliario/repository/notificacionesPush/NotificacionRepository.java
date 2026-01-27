package com.backend.crmInmobiliario.repository.notificacionesPush;

import com.backend.crmInmobiliario.entity.EstadoNotificacion;
import com.backend.crmInmobiliario.entity.Notificacion;
import com.backend.crmInmobiliario.entity.TipoNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    boolean existsByContrato_IdAndTipo(
            Long contratoId,
            TipoNotificacion tipo
    );
    List<Notificacion> findByUsuarioIdAndEstadoOrderByIdDesc(Long usuarioId, EstadoNotificacion estado);
}
