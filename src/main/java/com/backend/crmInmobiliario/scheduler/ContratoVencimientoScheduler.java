package com.backend.crmInmobiliario.scheduler;

import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.repository.notificacionesPush.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContratoVencimientoScheduler {


    private final ContratoRepository contratoRepository;
    private final NotificacionRepository notificacionRepository;

    // corre todos los días 08:00 (hora servidor)
    @Scheduled(cron = "0 0 * * * *", zone = "America/Argentina/Buenos_Aires")
    @Transactional
    public void generarNotificaciones() {

        LocalDate hoy = LocalDate.now();

        List<Contrato> vencidos = contratoRepository.findVencidosEntreFechas(
                EstadoContrato.ACTIVO,
                hoy.minusDays(1), // o lo que tenga sentido
                hoy
        );

        for (Contrato c : vencidos) {

            if (!notificacionRepository.existsByContrato_IdAndTipo(
                    c.getId(),
                    TipoNotificacion.CONTRATO_VENCIDO
            )) {

                Notificacion n = new Notificacion();
                n.setUsuario(c.getUsuario());
                n.setContrato(c);
                n.setTipo(TipoNotificacion.CONTRATO_VENCIDO);
                n.setMensaje(
                        "El contrato " + c.getNombreContrato()
                                + " venció el " + c.getFecha_fin()
                                + ". ¿Querés finalizarlo o renovarlo?"
                );

                notificacionRepository.save(n);
            }
        }
    }

}
