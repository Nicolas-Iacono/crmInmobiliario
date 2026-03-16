package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.salida.recibo.ReciboAlertaDto;
import com.backend.crmInmobiliario.entity.ReciboAlerta;
import com.backend.crmInmobiliario.entity.TipoAlertaRecibo;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.ReciboAlertaRepository;
import com.backend.crmInmobiliario.entity.PushSubscription;
import com.backend.crmInmobiliario.repository.notificacionesPush.PushSubscriptionRepository;
import com.backend.crmInmobiliario.service.impl.notificacionesPush.PushNotificationService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReciboAlertaService {
    private static final ZoneId ZONA_BUENOS_AIRES = ZoneId.of("America/Argentina/Buenos_Aires");
    private final Logger logger = LoggerFactory.getLogger(ReciboAlertaService.class);
    private final ReciboAlertaRepository reciboAlertaRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final PushNotificationService pushNotificationService;

    public ReciboAlertaService(
            ReciboAlertaRepository reciboAlertaRepository,
            PushSubscriptionRepository pushSubscriptionRepository,
            PushNotificationService pushNotificationService
    ) {
        this.reciboAlertaRepository = reciboAlertaRepository;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.pushNotificationService = pushNotificationService;
    }

    @Transactional
    public List<ReciboAlertaDto> listarAlertas(Long usuarioId) {
        return reciboAlertaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ReciboAlertaDto marcarVisto(Long alertaId, Long usuarioId) {
        ReciboAlerta alerta = obtenerAlertaUsuario(alertaId, usuarioId);
        alerta.setVisto(true);
        return toDto(reciboAlertaRepository.save(alerta));
    }

    @Transactional
    public ReciboAlertaDto marcarNoMostrar(Long alertaId, Long usuarioId) {
        ReciboAlerta alerta = obtenerAlertaUsuario(alertaId, usuarioId);
        alerta.setNoMostrar(true);
        return toDto(reciboAlertaRepository.save(alerta));
    }

    @Scheduled(cron = "0 0 10 * * ?", zone = "America/Argentina/Buenos_Aires")
    @Transactional
    public void recordarTransferenciasPendientes() {
        LocalDate hoy = LocalDate.now(ZONA_BUENOS_AIRES);
        List<ReciboAlerta> pendientes = reciboAlertaRepository
                .findPendientesParaNotificar(TipoAlertaRecibo.TRANSFERENCIA_PENDIENTE, hoy);

        Map<Long, List<ReciboAlerta>> porUsuario = pendientes.stream()
                .collect(Collectors.groupingBy(a -> a.getUsuario().getId()));

        for (Map.Entry<Long, List<ReciboAlerta>> entry : porUsuario.entrySet()) {
            Long usuarioId = entry.getKey();
            List<ReciboAlerta> alertasUsuario = entry.getValue();
            int cantidad = alertasUsuario.size();

            try {
                List<PushSubscription> subs = pushSubscriptionRepository.findByUserId(usuarioId);
                if (!subs.isEmpty()) {
                    String titulo = "🔔 Transferencias pendientes";
                    String cuerpo = cantidad == 1
                            ? "Tenés 1 transferencia pendiente por revisar."
                            : String.format("Tenés %d transferencias pendientes por revisar.", cantidad);
                    Map<String, Object> data = new HashMap<>();
                    data.put("type", "TRANSFERENCIA_PENDIENTE");
                    data.put("cantidad", cantidad);

                    for (PushSubscription sub : subs) {
                        pushNotificationService.enviarNotificacion(sub, titulo, cuerpo, data);
                    }
                    logger.info("✅ Recordatorio de transferencias enviado a usuario {}.", usuarioId);
                } else {
                    logger.info("ℹ️ Sin suscripciones push para usuario {}.", usuarioId);
                }

                alertasUsuario.forEach(a -> a.setUltimaNotificacion(hoy));
                reciboAlertaRepository.saveAll(alertasUsuario);
            } catch (Exception e) {
                logger.error("❌ Error al enviar recordatorio de transferencias al usuario {}.", usuarioId, e);
            }
        }
    }

    private ReciboAlerta obtenerAlertaUsuario(Long alertaId, Long usuarioId) {
        ReciboAlerta alerta = reciboAlertaRepository.findById(alertaId)
                .orElseThrow(() -> new IllegalArgumentException("Alerta no encontrada"));
        Usuario usuario = alerta.getUsuario();
        if (usuario == null || !usuario.getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tenés permisos para modificar esta alerta");
        }
        return alerta;
    }

    private ReciboAlertaDto toDto(ReciboAlerta alerta) {
        return new ReciboAlertaDto(
                alerta.getId(),
                alerta.getRecibo() != null ? alerta.getRecibo().getId() : null,
                alerta.getRecibo() != null && alerta.getRecibo().getContrato() != null
                        ? alerta.getRecibo().getContrato().getId()
                        : null,
                alerta.getUsuario() != null ? alerta.getUsuario().getId() : null,
                alerta.getTipo(),
                alerta.isVisto(),
                alerta.isNoMostrar(),
                alerta.getUltimaNotificacion(),
                alerta.getFechaCreacion()
        );
    }
}
