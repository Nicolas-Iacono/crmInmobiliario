package com.backend.crmInmobiliario.utils;


import com.backend.crmInmobiliario.DTO.salida.NotaCreadaEvent;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.PushSubscription;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.repository.notificacionesPush.PushSubscriptionRepository;
import com.backend.crmInmobiliario.service.impl.notificacionesPush.PushNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class NotaNotificacionListener { private final ContratoRepository contratoRepository;
    private final PushSubscriptionRepository pushRepo;
    private final PushNotificationService webPushService; // tu servicio real de push

    public NotaNotificacionListener(
            ContratoRepository contratoRepository,
            PushSubscriptionRepository pushRepo,
            PushNotificationService webPushService
    ) {
        this.contratoRepository = contratoRepository;
        this.pushRepo = pushRepo;
        this.webPushService = webPushService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotaCreada(NotaCreadaEvent event) {

        Contrato contrato = contratoRepository.findById(event.contratoId())
                .orElse(null);
        if (contrato == null) return;

        Set<Long> destinatarios = new HashSet<>();

        // Inmobiliaria
        if (contrato.getUsuario() != null) destinatarios.add(contrato.getUsuario().getId());

        // Inquilino (usuario cuenta)
        if (contrato.getInquilino() != null
                && contrato.getInquilino().getUsuarioCuentaInquilino() != null) {
            destinatarios.add(contrato.getInquilino().getUsuarioCuentaInquilino().getId());
        }

        // Propietario (usuario cuenta)
        if (contrato.getPropietario() != null
                && contrato.getPropietario().getUsuarioCuentaPropietario() != null) {
            destinatarios.add(contrato.getPropietario().getUsuarioCuentaPropietario().getId());
        }

        // (Opcional) si no querés notificar al autor, lo sacás:
        // destinatarios.remove(event.autorUserId());

        String title = "Nuevo reporte";
        String body = "Se creó un nuevo reporte en el contrato - " + contrato.getNombreContrato();
        Map<String, Object> data = Map.of(
                "type", "NOTA_CREADA",
                "contratoId", contrato.getId(),
                "notaId", event.notaId()
        );

        for (Long userId : destinatarios) {
            List<PushSubscription> subs = pushRepo.findByUserId(userId);

            for (PushSubscription sub : subs) {
                webPushService.enviarNotificacion(
                        sub,
                        title,
                        body,
                        Map.of(
                                "type", "NOTA_CREADA",
                                "contratoId", contrato.getId(),
                                "notaId", event.notaId()
                        )
                );
            }
        }
    }
}